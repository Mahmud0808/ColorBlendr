package com.drdisagree.colorblendr.theme.override

import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.token.Token
import org.json.JSONObject

object ColorOverrideCodec {

    private const val LIGHT = "light"
    private const val DARK = "dark"
    private const val CUSTOM_PREFIX = "#"
    private const val ALPHA_SEPARATOR = "@"
    private const val MAX_NAME_LENGTH = 255

    private val NAME_PATTERN = Regex("[A-Za-z0-9_.]+")

    private val tokensByName: Map<String, Token> = Token.entries.associateBy { it.resourceName }

    private val shadesByName: Map<String, ColorOverride.Shade> by lazy {
        PaletteRole.entries.flatMap { role ->
            ShadeScale.indices.map { index -> role.shadeName(index) to ColorOverride.Shade(role, index) }
        }.toMap()
    }

    fun encodeTokens(overrides: TokenOverrides): String =
        encodeModes(overrides) { it.resourceName }.toString()

    fun decodeTokens(json: String?): TokenOverrides =
        decodeModes(parse(json)) { tokensByName[it] }

    fun tokenModesToJson(overrides: TokenOverrides): JSONObject =
        encodeModes(overrides) { it.resourceName }

    fun tokenModesFromJson(json: JSONObject): TokenOverrides? {
        if (json.keys().asSequence().any { it != LIGHT && it != DARK }) return null
        val light = strictMode(json, LIGHT) ?: return null
        val dark = strictMode(json, DARK) ?: return null
        return TokenOverrides(light = light, dark = dark)
    }

    fun encodeApps(overrides: Map<String, ResourceOverrides>): String = JSONObject().apply {
        overrides.forEach { (packageName, modes) ->
            if (!modes.isEmpty && isValidName(packageName)) {
                put(packageName, encodeModes(modes) { it })
            }
        }
    }.toString()

    fun decodeApps(json: String?): Map<String, ResourceOverrides> {
        val root = parse(json) ?: return emptyMap()
        return buildMap {
            root.keys().forEach { packageName ->
                if (!isValidName(packageName)) return@forEach
                val modes = decodeModes(root.optJSONObject(packageName)) { name ->
                    name.takeIf(::isValidName)
                }
                if (!modes.isEmpty) put(packageName, modes)
            }
        }
    }

    fun encodeValue(override: ColorOverride): String = when (override) {
        is ColorOverride.Shade -> override.role.shadeName(override.index) +
                override.alpha?.let { ALPHA_SEPARATOR + "%02X".format(it) }.orEmpty()
        is ColorOverride.Custom -> CUSTOM_PREFIX + "%08X".format(override.color)
    }

    fun decodeValue(value: String): ColorOverride? = when {
        value.startsWith(CUSTOM_PREFIX) -> value.removePrefix(CUSTOM_PREFIX)
            .takeIf { it.length == 8 }
            ?.toLongOrNull(16)
            ?.let { ColorOverride.Custom(it.toInt()) }

        else -> {
            val shade = shadesByName[value.substringBefore(ALPHA_SEPARATOR)]
            val alpha = value.substringAfter(ALPHA_SEPARATOR, "")
            when {
                shade == null -> null
                alpha.isEmpty() -> shade
                else -> alpha.takeIf { it.length == 2 }?.toIntOrNull(16)?.let { shade.copy(alpha = it) }
            }
        }
    }

    private fun strictMode(json: JSONObject, key: String): Map<Token, ColorOverride>? {
        if (!json.has(key)) return emptyMap()
        val mode = json.optJSONObject(key) ?: return null
        val overrides = HashMap<Token, ColorOverride>()
        for (name in mode.keys()) {
            val value = mode.opt(name) as? String ?: return null
            val override = decodeValue(value) ?: return null
            tokensByName[name]?.let { overrides[it] = override }
        }
        return overrides
    }

    private fun isValidName(name: String): Boolean =
        name.length <= MAX_NAME_LENGTH && NAME_PATTERN.matches(name)

    private fun parse(json: String?): JSONObject? =
        if (json.isNullOrBlank()) null else runCatching { JSONObject(json) }.getOrNull()

    private fun <K> encodeModes(overrides: ModeOverrides<K>, nameOf: (K) -> String): JSONObject =
        JSONObject().apply {
            put(LIGHT, encodeMode(overrides.light, nameOf))
            put(DARK, encodeMode(overrides.dark, nameOf))
        }

    private fun <K> encodeMode(overrides: Map<K, ColorOverride>, nameOf: (K) -> String): JSONObject =
        JSONObject().apply {
            overrides.forEach { (key, override) -> put(nameOf(key), encodeValue(override)) }
        }

    private fun <K> decodeModes(json: JSONObject?, keyOf: (String) -> K?): ModeOverrides<K> {
        if (json == null) return ModeOverrides()
        return ModeOverrides(
            light = decodeMode(json.optJSONObject(LIGHT), keyOf),
            dark = decodeMode(json.optJSONObject(DARK), keyOf)
        )
    }

    private fun <K> decodeMode(json: JSONObject?, keyOf: (String) -> K?): Map<K, ColorOverride> {
        if (json == null) return emptyMap()
        return buildMap {
            json.keys().forEach { name ->
                val key = keyOf(name) ?: return@forEach
                val override = decodeValue(json.optString(name)) ?: return@forEach
                put(key, override)
            }
        }
    }
}

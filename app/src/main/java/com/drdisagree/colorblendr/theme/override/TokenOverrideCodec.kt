package com.drdisagree.colorblendr.theme.override

import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.token.Token
import org.json.JSONObject

object TokenOverrideCodec {

    private const val LIGHT = "light"
    private const val DARK = "dark"
    private const val CUSTOM_PREFIX = "#"

    private val tokensByName: Map<String, Token> = Token.entries.associateBy { it.resourceName }

    private val shadesByName: Map<String, TokenOverride.Shade> by lazy {
        PaletteRole.entries.flatMap { role ->
            ShadeScale.indices.map { index -> role.shadeName(index) to TokenOverride.Shade(role, index) }
        }.toMap()
    }

    fun encode(overrides: TokenOverrides): String = JSONObject().apply {
        put(LIGHT, encodeMode(overrides.light))
        put(DARK, encodeMode(overrides.dark))
    }.toString()

    fun decode(json: String?): TokenOverrides {
        if (json.isNullOrBlank()) return TokenOverrides()
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return TokenOverrides()
        return TokenOverrides(
            light = decodeMode(root.optJSONObject(LIGHT)),
            dark = decodeMode(root.optJSONObject(DARK))
        )
    }

    fun encodeValue(override: TokenOverride): String = when (override) {
        is TokenOverride.Shade -> override.role.shadeName(override.index)
        is TokenOverride.Custom -> CUSTOM_PREFIX + "%08X".format(override.color)
    }

    fun decodeValue(value: String): TokenOverride? = when {
        value.startsWith(CUSTOM_PREFIX) -> value.removePrefix(CUSTOM_PREFIX)
            .takeIf { it.length == 8 }
            ?.toLongOrNull(16)
            ?.let { TokenOverride.Custom(it.toInt()) }

        else -> shadesByName[value]
    }

    private fun encodeMode(overrides: Map<Token, TokenOverride>): JSONObject = JSONObject().apply {
        overrides.forEach { (token, override) -> put(token.resourceName, encodeValue(override)) }
    }

    private fun decodeMode(json: JSONObject?): Map<Token, TokenOverride> {
        if (json == null) return emptyMap()
        return buildMap {
            json.keys().forEach { name ->
                val token = tokensByName[name] ?: return@forEach
                val override = json.optString(name).let(::decodeValue) ?: return@forEach
                put(token, override)
            }
        }
    }
}

package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.modeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getColorSpecVersion
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.getSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.secondaryColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.tertiaryColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isColorOverriddenFor
import com.drdisagree.colorblendr.data.common.Utilities.getOverriddenColorFor
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import org.json.JSONArray
import org.json.JSONObject

// Strict codec for community theme payloads. Everything fetched is untrusted:
// unknown/malformed input -> null, never a partial theme. Payload = data only.
object CommunityThemeCodec {

    const val SCHEMA_VERSION = 1
    const val MAX_PAYLOAD_BYTES = 8 * 1024
    const val MAX_INDEX_THEMES = 2000

    private const val MAX_NAME_LENGTH = 40
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MAX_AUTHOR_LENGTH = 40
    private const val SLIDER_MIN = 0
    private const val SLIDER_MAX = 200

    private val ID_REGEX = Regex("^[a-z0-9][a-z0-9-]{0,63}$")
    private val HEX_COLOR_REGEX = Regex("^#[0-9a-fA-F]{6}$")
    private val CONTROL_CHARS = Regex("[\\p{Cntrl}]")
    private val CONTROL_CHARS_EXCEPT_NEWLINE = Regex("[\\p{Cntrl}&&[^\n]]")
    private val EXCESS_NEWLINES = Regex("\n{3,}")

    // Raw slider pref keys: dark/default vs light variants (the Constant
    // getters are mode-dependent — uploads must read both explicitly).
    internal const val KEY_ACCENT_SATURATION = "monetAccentSaturationValue"
    internal const val KEY_ACCENT_SATURATION_LIGHT = "monetAccentSaturationValueLight"
    internal const val KEY_BACKGROUND_SATURATION = "monetBackgroundSaturationValue"
    internal const val KEY_BACKGROUND_SATURATION_LIGHT = "monetBackgroundSaturationValueLight"
    internal const val KEY_BACKGROUND_LIGHTNESS = "monetBackgroundLightnessValue"
    internal const val KEY_BACKGROUND_LIGHTNESS_LIGHT = "monetBackgroundLightnessValueLight"

    private val validShadeNames: Set<String> by lazy {
        systemPaletteNames.flatMap { it.asIterable() }.toSet()
    }

    // Index = JSON array of theme objects; invalid entries skipped, duplicate
    // ids dropped, hard cap on count.
    fun parseIndex(json: String): List<CommunityTheme> {
        val array = try {
            JSONArray(json)
        } catch (_: Exception) {
            return emptyList()
        }

        val themes = LinkedHashMap<String, CommunityTheme>()
        for (i in 0 until minOf(array.length(), MAX_INDEX_THEMES)) {
            val theme = array.optJSONObject(i)?.let(::parseTheme) ?: continue
            themes.putIfAbsent(theme.id, theme)
        }
        return themes.values.toList()
    }

    fun parseTheme(json: JSONObject): CommunityTheme? {
        if (json.toString().toByteArray().size > MAX_PAYLOAD_BYTES) return null
        if (json.optInt("schemaVersion", -1) != SCHEMA_VERSION) return null

        val id = json.optString("id")
        if (!ID_REGEX.matches(id)) return null

        val name = sanitizeText(json.optString("name"), MAX_NAME_LENGTH)
        if (name.isEmpty()) return null
        val description = sanitizeMultiline(json.optString("description"), MAX_DESCRIPTION_LENGTH)
        if (description.isEmpty()) return null
        val author = sanitizeText(json.optString("author"), MAX_AUTHOR_LENGTH)

        val style = MONET.entries.find { it.name == json.optString("style") } ?: return null
        val seedColor = parseHexColor(json.optString("seedColor")) ?: return null
        val secondaryColor = parseOptionalColor(json, "secondaryColor") ?: return null
        val tertiaryColor = parseOptionalColor(json, "tertiaryColor") ?: return null

        val accentSaturation = parseSlider(json, "accentSaturation") ?: return null
        val backgroundSaturation = parseSlider(json, "backgroundSaturation") ?: return null
        val backgroundLightness = parseSlider(json, "backgroundLightness") ?: return null

        // Absent = 0 (2021 spec) so old payloads reproduce deterministically.
        val colorSpecVersion = json.optInt("colorSpecVersion", 0)
        if (colorSpecVersion !in 0..2) return null

        // Light-mode slider variants; absent = same as the main values.
        val modeSpecificThemes = json.optBoolean("modeSpecificThemes", false)
        val accentSaturationLight =
            parseSliderOr(json, "accentSaturationLight", accentSaturation) ?: return null
        val backgroundSaturationLight =
            parseSliderOr(json, "backgroundSaturationLight", backgroundSaturation) ?: return null
        val backgroundLightnessLight =
            parseSliderOr(json, "backgroundLightnessLight", backgroundLightness) ?: return null

        val colorOverrides = parseColorOverrides(json.optJSONObject("colorOverrides"))
            ?: return null

        return CommunityTheme(
            id = id,
            name = name,
            description = description,
            author = author,
            style = style,
            seedColor = seedColor,
            secondaryColor = secondaryColor.value,
            tertiaryColor = tertiaryColor.value,
            accentSaturation = accentSaturation,
            backgroundSaturation = backgroundSaturation,
            backgroundLightness = backgroundLightness,
            modeSpecificThemes = modeSpecificThemes,
            accentSaturationLight = accentSaturationLight,
            backgroundSaturationLight = backgroundSaturationLight,
            backgroundLightnessLight = backgroundLightnessLight,
            accurateShades = json.optBoolean("accurateShades", true),
            colorSpecVersion = colorSpecVersion,
            pitchBlack = json.optBoolean("pitchBlack", false),
            tintText = json.optBoolean("tintText", true),
            colorOverrides = colorOverrides,
            upvotes = json.optInt("upvotes").coerceAtLeast(0),
            downloads = json.optInt("downloads").coerceAtLeast(0),
            createdAt = json.optLong("createdAt").coerceAtLeast(0L)
        )
    }

    // Upload payload from current selections. No id/counters — CI assigns id,
    // server owns counters. Seed passed in: caller knows the effective seed
    // (custom color or wallpaper-derived).
    fun currentSettingsToUploadJson(
        name: String,
        description: String,
        author: String,
        seedColor: Int
    ): JSONObject = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("name", sanitizeText(name, MAX_NAME_LENGTH))
        put("description", sanitizeMultiline(description, MAX_DESCRIPTION_LENGTH))
        put("author", sanitizeText(author, MAX_AUTHOR_LENGTH))
        put("style", getCurrentMonetStyle().name)
        put("seedColor", toHex(seedColor))
        if (secondaryColorEnabled()) put("secondaryColor", toHex(getSecondaryColorValue()))
        if (tertiaryColorEnabled()) put("tertiaryColor", toHex(getTertiaryColorValue()))
        // Raw dark/default keys — the getters return the CURRENT mode's value.
        put("accentSaturation", Prefs.getInt(KEY_ACCENT_SATURATION, 100))
        put("backgroundSaturation", Prefs.getInt(KEY_BACKGROUND_SATURATION, 100))
        put("backgroundLightness", Prefs.getInt(KEY_BACKGROUND_LIGHTNESS, 100))
        if (modeSpecificThemesEnabled()) {
            put("modeSpecificThemes", true)
            put("accentSaturationLight", Prefs.getInt(KEY_ACCENT_SATURATION_LIGHT, 100))
            put("backgroundSaturationLight", Prefs.getInt(KEY_BACKGROUND_SATURATION_LIGHT, 100))
            put("backgroundLightnessLight", Prefs.getInt(KEY_BACKGROUND_LIGHTNESS_LIGHT, 100))
        }
        put("accurateShades", accurateShadesEnabled())
        put("colorSpecVersion", getColorSpecVersion())
        put("pitchBlack", pitchBlackThemeEnabled())
        put("tintText", tintedTextEnabled())

        val overrides = JSONObject()
        validShadeNames.forEach { shadeName ->
            if (isColorOverriddenFor(shadeName)) {
                overrides.put(shadeName, toHex(getOverriddenColorFor(shadeName)))
            }
        }
        if (overrides.length() > 0) put("colorOverrides", overrides)
    }

    // Full round-trip serialization (id + counters) for the Room cache.
    fun themeToJson(theme: CommunityTheme): JSONObject = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("id", theme.id)
        put("name", theme.name)
        put("description", theme.description)
        put("author", theme.author)
        put("style", theme.style.name)
        put("seedColor", toHex(theme.seedColor))
        theme.secondaryColor?.let { put("secondaryColor", toHex(it)) }
        theme.tertiaryColor?.let { put("tertiaryColor", toHex(it)) }
        put("accentSaturation", theme.accentSaturation)
        put("backgroundSaturation", theme.backgroundSaturation)
        put("backgroundLightness", theme.backgroundLightness)
        if (theme.modeSpecificThemes) {
            put("modeSpecificThemes", true)
            put("accentSaturationLight", theme.accentSaturationLight)
            put("backgroundSaturationLight", theme.backgroundSaturationLight)
            put("backgroundLightnessLight", theme.backgroundLightnessLight)
        }
        put("accurateShades", theme.accurateShades)
        put("colorSpecVersion", theme.colorSpecVersion)
        put("pitchBlack", theme.pitchBlack)
        put("tintText", theme.tintText)
        if (theme.colorOverrides.isNotEmpty()) {
            put("colorOverrides", JSONObject(theme.colorOverrides.mapValues { toHex(it.value) }))
        }
        put("upvotes", theme.upvotes)
        put("downloads", theme.downloads)
        put("createdAt", theme.createdAt)
    }

    // Single-line fields: every control char stripped. JSON escaping itself
    // is handled by the serializer; this only normalizes content.
    private fun sanitizeText(raw: String, maxLength: Int): String =
        raw.replace(CONTROL_CHARS, "").trim().take(maxLength)

    // Description keeps newlines (capped at one blank line), drops the rest.
    private fun sanitizeMultiline(raw: String, maxLength: Int): String =
        raw.replace("\r\n", "\n")
            .replace(CONTROL_CHARS_EXCEPT_NEWLINE, "")
            .replace(EXCESS_NEWLINES, "\n\n")
            .trim()
            .take(maxLength)

    private fun parseHexColor(raw: String): Int? {
        if (!HEX_COLOR_REGEX.matches(raw)) return null
        return 0xFF000000.toInt() or raw.substring(1).toInt(16)
    }

    private fun toHex(color: Int): String =
        String.format("#%06X", color and 0xFFFFFF)

    // Distinguishes "absent/null" (valid, no color) from "present but garbage"
    // (reject whole theme).
    private class OptionalColor(val value: Int?)

    private fun parseOptionalColor(json: JSONObject, key: String): OptionalColor? {
        if (!json.has(key) || json.isNull(key)) return OptionalColor(null)
        val color = parseHexColor(json.optString(key)) ?: return null
        return OptionalColor(color)
    }

    private fun parseSlider(json: JSONObject, key: String): Int? {
        val value = json.optInt(key, Int.MIN_VALUE)
        if (value == Int.MIN_VALUE && !json.has(key)) return 100
        return value.takeIf { it in SLIDER_MIN..SLIDER_MAX }
    }

    private fun parseSliderOr(json: JSONObject, key: String, fallback: Int): Int? {
        if (!json.has(key)) return fallback
        return json.optInt(key, Int.MIN_VALUE).takeIf { it in SLIDER_MIN..SLIDER_MAX }
    }

    // Keys must be known palette shade names, values strict hex. Anything
    // unknown rejects the whole theme.
    private fun parseColorOverrides(json: JSONObject?): Map<String, Int>? {
        if (json == null) return emptyMap()

        val overrides = HashMap<String, Int>()
        for (key in json.keys()) {
            if (key !in validShadeNames) return null
            overrides[key] = parseHexColor(json.optString(key)) ?: return null
        }
        return overrides
    }
}

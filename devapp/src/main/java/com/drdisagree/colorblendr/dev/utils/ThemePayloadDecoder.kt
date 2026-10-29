package com.drdisagree.colorblendr.dev.utils

import androidx.core.graphics.toColorInt
import com.drdisagree.colorblendr.dev.data.models.ThemePayload
import org.json.JSONObject

object ThemePayloadDecoder {

    fun decode(payloadJson: String): ThemePayload? {
        val json = try {
            JSONObject(payloadJson)
        } catch (_: Exception) {
            return null
        }

        val accent = json.optInt("accentSaturation", 100)
        val bgSat = json.optInt("backgroundSaturation", 100)
        val bgLight = json.optInt("backgroundLightness", 100)
        val modeSpecific = json.optBoolean("modeSpecificThemes", false)

        val overridesMap = mutableMapOf<String, Int>()
        val overridesObj = json.optJSONObject("colorOverrides")
        if (overridesObj != null) {
            val keys = overridesObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val colorVal = overridesObj.optString(key)
                if (colorVal.isNotEmpty()) {
                    val colorInt = runCatching { colorVal.toColorInt() }.getOrNull()
                        ?: overridesObj.optInt(key).takeIf { it != 0 }
                    if (colorInt != null) {
                        overridesMap[key] = colorInt
                    }
                }
            }
        }

        return ThemePayload(
            description = json.optString("description").trim(),
            style = json.optString("style"),
            colorSpecVersion = json.optInt("colorSpecVersion", 0),
            accentSaturation = accent,
            backgroundSaturation = bgSat,
            backgroundLightness = bgLight,
            modeSpecificThemes = modeSpecific,
            accentSaturationLight = json.optInt("accentSaturationLight", accent),
            backgroundSaturationLight = json.optInt("backgroundSaturationLight", bgSat),
            backgroundLightnessLight = json.optInt("backgroundLightnessLight", bgLight),
            accurateShades = json.optBoolean("accurateShades", true),
            pitchBlack = json.optBoolean("pitchBlack", false),
            tintText = json.optBoolean("tintText", true),
            colorOverrides = overridesMap,
            overrideCount = overridesMap.size,
            schemaVersion = json.optInt("schemaVersion", 1),
            engineVersion = json.optInt("engineVersion", 1),
            pitchBlackContainers = json.optBoolean("pitchBlackContainers", false),
            tokenOverrides = tokenOverrides(json.optJSONObject("tokenOverrides"))
        )
    }

    fun tokenOverrides(json: JSONObject?): Map<String, String> {
        if (json == null) return emptyMap()
        return buildMap {
            for (mode in TOKEN_MODES) {
                val tokens = json.optJSONObject(mode) ?: continue
                val keys = tokens.keys()
                while (keys.hasNext()) {
                    val token = keys.next()
                    tokens.optString(token).takeIf { it.isNotEmpty() }?.let { put("$mode/$token", it) }
                }
            }
        }
    }

    fun tokenOverrideCount(overrides: Map<String, String>, mode: String): Int =
        overrides.keys.count { it.startsWith("$mode/") }

    const val LIGHT = "light"
    const val DARK = "dark"
    private val TOKEN_MODES = listOf(LIGHT, DARK)
}

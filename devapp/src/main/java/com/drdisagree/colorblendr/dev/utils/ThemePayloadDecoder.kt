package com.drdisagree.colorblendr.dev.utils

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
            overrideCount = json.optJSONObject("colorOverrides")?.length() ?: 0
        )
    }
}

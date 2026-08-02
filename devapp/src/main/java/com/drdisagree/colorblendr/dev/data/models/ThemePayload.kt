package com.drdisagree.colorblendr.dev.data.models

data class ThemePayload(
    val description: String,
    val style: String,
    val colorSpecVersion: Int,
    val accentSaturation: Int,
    val backgroundSaturation: Int,
    val backgroundLightness: Int,
    val modeSpecificThemes: Boolean,
    val accentSaturationLight: Int,
    val backgroundSaturationLight: Int,
    val backgroundLightnessLight: Int,
    val accurateShades: Boolean,
    val pitchBlack: Boolean,
    val tintText: Boolean,
    val overrideCount: Int
)

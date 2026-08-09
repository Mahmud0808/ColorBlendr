package com.drdisagree.colorblendr.dev.data.models

data class ComparableTheme(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val seedColor: Int?,
    val secondaryColor: Int?,
    val tertiaryColor: Int?,
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
    val colorOverrides: Map<String, Int> = emptyMap(),
    val payloadJson: String,
    val isPublished: Boolean
)

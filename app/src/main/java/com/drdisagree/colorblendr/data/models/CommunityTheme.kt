package com.drdisagree.colorblendr.data.models

import com.drdisagree.colorblendr.data.enums.MONET

// Validated community theme. Data only — colors, style, modifiers; nothing
// executable. Counters/createdAt come from server index, 0 when absent.
data class CommunityTheme(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val style: MONET,
    val seedColor: Int,
    val secondaryColor: Int?,
    val tertiaryColor: Int?,
    val accentSaturation: Int,
    val backgroundSaturation: Int,
    val backgroundLightness: Int,
    val modeSpecificThemes: Boolean,
    val accentSaturationLight: Int,
    val backgroundSaturationLight: Int,
    val backgroundLightnessLight: Int,
    val accurateShades: Boolean,
    val colorSpecVersion: Int,
    val pitchBlack: Boolean,
    val tintText: Boolean,
    val colorOverrides: Map<String, Int>,
    val upvotes: Int,
    val downloads: Int,
    val createdAt: Long
)

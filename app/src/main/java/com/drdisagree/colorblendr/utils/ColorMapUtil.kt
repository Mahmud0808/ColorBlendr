package com.drdisagree.colorblendr.utils

import androidx.core.util.Pair
import com.drdisagree.colorblendr.utils.ColorUtil.modifyBrightness

data class ColorMapping(
    val resourceName: String,
    val tonalPalette: TonalPalette? = null,
    val colorIndex: Int? = null,
    val lightModeColorIndex: Int? = null,
    val darkModeColorIndex: Int? = null,
    val lightModeColorCode: Int? = null,
    val colorCode: Int? = null,
    val darkModeColorCode: Int? = null,
    val lightnessAdjustment: Int? = null,
    val lightModeLightnessAdjustment: Int? = null,
    val darkModeLightnessAdjustment: Int? = null
)

fun ColorMapping.extractResourceFromColorMap(
    prefix: String = "",
    suffix: String = "",
    palette: ArrayList<ArrayList<Int>>,
    isDark: Boolean = false
): Pair<String, Int> {
    val resourceName = prefix + resourceName + suffix

    val colorValue: Int = if (tonalPalette != null) {
        if (colorIndex != null) {
            palette[tonalPalette.index][colorIndex]
        } else {
            if (isDark) {
                palette[tonalPalette.index][darkModeColorIndex!!]
            } else {
                palette[tonalPalette.index][lightModeColorIndex!!]
            }
        }
    } else {
        colorCode ?: if (isDark) {
            darkModeColorCode
        } else {
            lightModeColorCode
        }
    }!!

    return Pair(resourceName, colorValue)
}

fun ColorMapping.adjustColorBrightnessIfRequired(
    colorValue: Int,
    isDark: Boolean
): Int {
    return if (lightnessAdjustment != null) {
        modifyBrightness(colorValue, lightnessAdjustment)
    } else if (darkModeLightnessAdjustment != null && isDark) {
        modifyBrightness(colorValue, darkModeLightnessAdjustment)
    } else if (lightModeLightnessAdjustment != null && !isDark) {
        modifyBrightness(colorValue, lightModeLightnessAdjustment)
    } else {
        colorValue
    }
}
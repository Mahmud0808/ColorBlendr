package com.drdisagree.colorblendr.utils.colors

import androidx.core.util.Pair
import com.drdisagree.colorblendr.utils.colors.ColorUtil.adjustColorLightness
import com.drdisagree.colorblendr.utils.colors.ColorUtil.adjustLightness

data class ColorMapping(
    val resourceName: String,
    val tonalPalette: TonalPalette? = null,
    val lightModeTonalPalette: TonalPalette? = null,
    val darkModeTonalPalette: TonalPalette? = null,
    val colorIndex: Int? = null,
    val lightModeColorIndex: Int? = null,
    val darkModeColorIndex: Int? = null,
    val colorCode: Int? = null,
    val lightModeColorCode: Int? = null,
    val darkModeColorCode: Int? = null,
    val lightnessAdjustment: Int? = null,
    val lightModeLightnessAdjustment: Int? = null,
    val darkModeLightnessAdjustment: Int? = null,
    val lStarAdjustment: Double? = null,
    val lightModeLStarAdjustment: Double? = null,
    val darkModeLStarAdjustment: Double? = null
)

fun ColorMapping.extractResourceFromColorMap(
    prefix: String = "",
    suffix: String = "",
    palette: ArrayList<ArrayList<Int>>,
    isDark: Boolean
): Pair<String, Int> {
    val resourceName = prefix + resourceName + suffix

    val colorValue: Int = if (tonalPalette != null ||
        (lightModeTonalPalette != null && darkModeTonalPalette != null)
    ) {
        val tonalPaletteIndex =
            (tonalPalette ?: if (isDark) darkModeTonalPalette else lightModeTonalPalette)!!.index
        val colorIndex = colorIndex ?: if (isDark) darkModeColorIndex!! else lightModeColorIndex!!

        palette[tonalPaletteIndex][colorIndex]
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
        adjustLightness(colorValue, lightnessAdjustment)
    } else if (darkModeLightnessAdjustment != null && isDark) {
        adjustLightness(colorValue, darkModeLightnessAdjustment)
    } else if (lightModeLightnessAdjustment != null && !isDark) {
        adjustLightness(colorValue, lightModeLightnessAdjustment)
    } else {
        colorValue
    }
}

fun ColorMapping.adjustLStarIfRequired(
    colorValue: Int,
    isDark: Boolean
): Int {
    // LAB doesn't work for black and white, so we use CAM instead
    return if (lStarAdjustment != null) {
        adjustColorLightness(colorValue, lStarAdjustment.toFloat())
    } else if (darkModeLStarAdjustment != null && isDark) {
        adjustColorLightness(colorValue, darkModeLStarAdjustment.toFloat())
    } else if (lightModeLStarAdjustment != null && !isDark) {
        adjustColorLightness(colorValue, lightModeLStarAdjustment.toFloat())
    } else {
        colorValue
    }
}
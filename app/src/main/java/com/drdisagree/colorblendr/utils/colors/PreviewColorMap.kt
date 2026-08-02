package com.drdisagree.colorblendr.utils.colors

import android.graphics.Color
import android.os.Build
import androidx.core.util.component1
import androidx.core.util.component2
import com.drdisagree.colorblendr.data.common.Utilities.forcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.adjustLightness
import com.drdisagree.colorblendr.utils.colors.DynamicColors.ALL_DYNAMIC_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.colors.DynamicColors.FIXED_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.applyColorAdjustments
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.computeTintlessFrameworkTextColor

// Computes the final app-visible role colors from the modified palettes by
// reusing the same mapping pipeline the fabricated overlay is built with, so
// the in-app theme can preview colors before the overlay is applied.
fun computeSystemColorMap(
    paletteLight: ArrayList<ArrayList<Int>>,
    paletteDark: ArrayList<ArrayList<Int>>,
    isDark: Boolean
): Map<String, Int> {
    val palette = if (isDark) paletteDark else paletteLight
    val suffix = if (isDark) "_dark" else "_light"
    val tintTextColor = tintedTextEnabled()
    val pitchBlackTheme = pitchBlackThemeEnabled()
    val textColorResources = setOf(
        "on_surface",
        "on_surface_variant",
        "on_background",
        "on_primary_container",
        "on_secondary_container",
        "on_tertiary_container",
        "on_error"
    )
    val map = HashMap<String, Int>()

    ALL_DYNAMIC_COLORS_MAPPED.forEach { colorMapping ->
        val (resourceName, baseValue) = colorMapping.extractResourceFromColorMap(
            prefix = "system_",
            suffix = suffix,
            palette = palette,
            isDark = isDark
        )
        val colorValue = applyColorAdjustments(
            colorMapping,
            resourceName,
            baseValue,
            isDark,
            pitchBlackTheme
        )

        map[resourceName] =
            if (!tintTextColor && textColorResources.any { resourceName.contains(it) }) {
                computeTintlessFrameworkTextColor(resourceName, colorValue)
            } else {
                colorValue
            }
    }

    FIXED_COLORS_MAPPED.forEach { colorMapping ->
        val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
            prefix = "system_",
            palette = paletteLight,
            isDark = false
        )
        map[resourceName] = colorValue
    }

    map.putAll(
        computeFinalColorOverrides(
            paletteLight = paletteLight,
            paletteDark = paletteDark,
            currentSurfaceContainerDark = map["system_surface_container_dark"]
        )
    )

    return map
}

// Single source for the role overrides applied on top of the dynamic mapping;
// consumed by both OverlayManager (overlay build) and the in-app preview.
fun computeFinalColorOverrides(
    paletteLight: ArrayList<ArrayList<Int>>,
    paletteDark: ArrayList<ArrayList<Int>>,
    currentSurfaceContainerDark: Int?
): Map<String, Int> {
    val map = HashMap<String, Int>()
    val tintTextColor = tintedTextEnabled()
    val pitchBlackTheme = pitchBlackThemeEnabled()

    // Material error colors; the overlay writes error shades from the
    // device's current ui mode palette.
    val errorShades = (if (SystemUtil.isDarkMode) paletteDark else paletteLight)[5]
    map["system_error_light"] = errorShades[8]
    map["system_error_container_light"] = errorShades[3]
    map["system_error_dark"] = errorShades[4]
    map["system_error_container_dark"] = errorShades[9]
    if (tintTextColor) {
        map["system_on_error_light"] = errorShades[3]
        map["system_on_error_container_light"] = errorShades[8]
        map["system_on_error_dark"] = errorShades[9]
        map["system_on_error_container_dark"] = errorShades[4]
    } else {
        map["system_on_error_light"] = Color.WHITE
        map["system_on_error_container_light"] = Color.BLACK
        map["system_on_error_dark"] = Color.BLACK
        map["system_on_error_container_dark"] = Color.WHITE
    }

    if (pitchBlackTheme) {
        // Notification scrim color A14+
        map["system_surface_dim_dark"] = Color.BLACK
    }

    // Temporary workaround for Android 15 QPR1 beta 3 background color issue
    // in settings; Currently, we set the status bar color to match the background color
    // to achieve a uniform appearance when the background lightness is reduced.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
        pitchBlackTheme && SystemUtil.isDarkMode && forcePitchBlackSettingsEnabled()
    ) {
        currentSurfaceContainerDark?.let {
            map["system_surface_container_dark"] = adjustLightness(it, -58)
        }
    }

    return map
}

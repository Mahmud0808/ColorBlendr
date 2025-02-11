package com.drdisagree.colorblendr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.util.component1
import androidx.core.util.component2
import com.drdisagree.colorblendr.data.common.Const
import com.drdisagree.colorblendr.data.common.Const.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.data.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Const.TINT_TEXT_COLOR
import com.drdisagree.colorblendr.data.common.Const.saveSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Const.workingMethod
import com.drdisagree.colorblendr.data.config.Prefs.getBoolean
import com.drdisagree.colorblendr.utils.ColorUtil.adjustLightness
import com.drdisagree.colorblendr.utils.ColorUtil.getColorNamesM3
import com.drdisagree.colorblendr.utils.DynamicColors.ALL_DYNAMIC_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.DynamicColors.FIXED_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.DynamicColors.M3_REF_PALETTE
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

object FabricatedUtil {

    private val colorNamesM3Variants = listOf(
        getColorNamesM3(isDynamic = false, prefixG = false),
        getColorNamesM3(isDynamic = true, prefixG = false),
        getColorNamesM3(isDynamic = true, prefixG = true),
        getColorNamesM3(isDynamic = false, prefixG = true)
    )

    fun FabricatedOverlayResource.createDynamicOverlay(
        paletteLight: ArrayList<ArrayList<Int>>,
        paletteDark: ArrayList<ArrayList<Int>>
    ) {
        assignDynamicPaletteToOverlay(true,  /* isDark */paletteDark)
        assignDynamicPaletteToOverlay(false,  /* isDark */paletteLight)
        assignFixedColorsToOverlay(paletteLight)
    }

    private fun FabricatedOverlayResource.assignDynamicPaletteToOverlay(
        isDark: Boolean,
        palette: ArrayList<ArrayList<Int>>
    ) {
        val suffix = if (isDark) "dark" else "light"
        val isPitchBlackTheme = getBoolean(MONET_PITCH_BLACK_THEME, false)
        val prefixSuffix = arrayOf(
            "system_" to "_${suffix}",
            "m3_sys_color_${suffix}_" to "",
            "m3_sys_color_dynamic_${suffix}_" to "",
            "gm3_sys_color_${suffix}_" to "",
            "gm3_sys_color_dynamic_${suffix}_" to ""
        )

        ALL_DYNAMIC_COLORS_MAPPED.forEach { colorMapping ->
            for ((tempPrefix, tempSuffix) in prefixSuffix) {
                val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                    prefix = tempPrefix,
                    suffix = tempSuffix,
                    palette = palette,
                    isDark = isDark
                ).let { (name, value) ->
                    name to applyColorAdjustments(
                        colorMapping,
                        name,
                        value,
                        isDark,
                        isPitchBlackTheme
                    )
                }

                setColor(resourceName, colorValue)
            }
        }
    }

    private fun FabricatedOverlayResource.assignFixedColorsToOverlay(
        paletteLight: ArrayList<ArrayList<Int>>
    ) {
        FIXED_COLORS_MAPPED.forEach { colorMapping ->
            val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                prefix = "system_",
                palette = paletteLight
            )

            setColor(resourceName, colorValue)
        }
    }

    fun FabricatedOverlayResource.assignPerAppColorsToOverlay(
        palette: ArrayList<ArrayList<Int>>
    ) {
        val isPitchBlackTheme = getBoolean(MONET_PITCH_BLACK_THEME, false)
        val tintTextColor = getBoolean(TINT_TEXT_COLOR, true)

        M3_REF_PALETTE.forEach { colorMapping ->
            val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                palette = palette
            ).let { (name, value) ->
                name to applyColorAdjustments(
                    colorMapping,
                    name,
                    value,
                    isDark = false,
                    isPitchBlackTheme
                )
            }

            setColor(resourceName, colorValue)
            setColor("g$resourceName", colorValue)
        }

        colorNamesM3Variants.forEach { variant ->
            variant.forEachIndexed { i, row ->
                row.forEachIndexed { j, name ->
                    setColor(name, palette[i][j])
                }
            }
        }

        replaceColorsPerPackageName(palette, isPitchBlackTheme)

        if (!tintTextColor) {
            addTintlessTextColors()
        }
    }

    fun updateFabricatedAppList(context: Context) {
        if (workingMethod != Const.WorkMethod.ROOT ||
            !getBoolean(THEMING_ENABLED, true)
        ) return

        val packageManager = context.packageManager
        val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val selectedApps = HashMap<String, Boolean>().apply {
            applications.forEach { appInfo ->
                val packageName = appInfo.packageName
                val isSelected = OverlayManager.isOverlayEnabled(
                    String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)
                )

                if (isSelected) {
                    put(packageName, true)
                }
            }
        }

        //        selectedApps.put(BuildConfig.APPLICATION_ID, true);

        saveSelectedFabricatedApps(selectedApps)
    }

    private fun applyColorAdjustments(
        colorMapping: ColorMapping,
        resourceName: String,
        colorValue: Int,
        isDark: Boolean,
        pitchBlackTheme: Boolean
    ): Int {
        return adjustColorForPitchBlackThemeIfRequired(
            pitchBlackTheme,
            resourceName,
            colorValue
        ).let { adjustedValue ->
            colorMapping.adjustColorBrightnessIfRequired(adjustedValue, isDark)
        }
    }

    @ColorInt
    fun adjustColorForPitchBlackThemeIfRequired(
        pitchBlackTheme: Boolean,
        resourceName: String,
        colorValue: Int
    ): Int {
        if (!pitchBlackTheme) return colorValue

        return when (resourceName) {
            "m3_ref_palette_dynamic_neutral_variant6",
            "gm3_ref_palette_dynamic_neutral_variant6",
            "system_background_dark",
            "system_surface_dark" -> {
                Color.BLACK
            }

            "m3_ref_palette_dynamic_neutral_variant12",
            "gm3_ref_palette_dynamic_neutral_variant12" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -56)
            }

            "m3_ref_palette_dynamic_neutral_variant17",
            "gm3_ref_palette_dynamic_neutral_variant17",
            "gm3_system_bar_color_night" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -74)
            }

            "system_surface_container_lowest_dark" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -54)
            }

            "system_surface_container_low_dark" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -45)
            }

            "system_surface_container_dark" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -36)
            }

            "system_surface_container_high_dark",
            "system_surface_dim_dark" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -25)
            }

            "system_surface_container_highest_dark",
            "system_surface_bright_dark" -> {
                adjustLightness(color = colorValue, brightnessPercentage = -16)
            }

            else -> {
                colorValue
            }
        }
    }

    private fun FabricatedOverlayResource.addTintlessTextColors() {
        val prefixes = arrayOf("m3_sys_color_", "m3_sys_color_dynamic_")
        val variants = arrayOf("dark_", "light_")
        val suffixes = arrayOf("on_surface", "on_surface_variant", "on_background")

        prefixes.forEach { prefix ->
            variants.forEach { variant ->
                suffixes.forEach { suffix ->
                    setColor(
                        "$prefix$variant$suffix",
                        if (variant.contains("dark")) Color.WHITE else Color.BLACK
                    )
                }
            }
        }

        // Resources for dark and light modes
        val resources = mapOf(
            "dark" to listOf(
                "m3_ref_palette_dynamic_neutral90" to Color.WHITE,
                "m3_ref_palette_dynamic_neutral95" to Color.WHITE,
                "m3_ref_palette_dynamic_neutral_variant70" to -0x333334,
                "m3_ref_palette_dynamic_neutral_variant80" to Color.WHITE,
                "text_color_primary_dark" to Color.WHITE,
                "text_color_secondary_dark" to -0x4c000001,
                "text_color_tertiary_dark" to -0x7f000001,
                "google_dark_default_color_on_background" to Color.WHITE,
                "gm_ref_palette_grey500" to Color.WHITE
            ),
            "light" to listOf(
                "m3_ref_palette_dynamic_neutral10" to Color.BLACK,
                "m3_ref_palette_dynamic_neutral_variant30" to -0x4d000000,
                "text_color_primary_light" to Color.BLACK,
                "text_color_secondary_light" to -0x4d000000,
                "text_color_tertiary_light" to -0x80000000,
                "google_default_color_on_background" to Color.BLACK,
                "gm_ref_palette_grey700" to Color.BLACK
            )
        )

        resources.forEach { (_, pairs) ->
            pairs.forEach { (name, color) ->
                setColor(name, color)
                setColor("g$name", color)
            }
        }

        when {
            targetPackage == "com.android.settings" -> {
                when {
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        setColor(
                            "settingslib_text_color_primary_device_default",
                            Color.WHITE,
                            "night"
                        )
                        setColor(
                            "settingslib_text_color_secondary_device_default",
                            -0x4c000001,
                            "night"
                        )
                    }

                    Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        setColor(
                            "settingslib_materialColorOnSurface",
                            Color.WHITE,
                            "night"
                        )
                        setColor(
                            "settingslib_materialColorOnSurfaceVariant",
                            -0x4c000001,
                            "night"
                        )
                    }
                }
            }
        }
    }
}

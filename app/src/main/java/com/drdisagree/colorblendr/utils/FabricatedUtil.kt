package com.drdisagree.colorblendr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.util.Pair
import androidx.core.util.component1
import androidx.core.util.component2
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

object FabricatedUtil {
    private val colorNames: Array<Array<String>> = ColorUtil.colorNames
    private val colorNamesM3var1: Array<Array<String>> = ColorUtil.getColorNamesM3(
        isDynamic = false,
        prefixG = false
    )
    private val colorNamesM3var2: Array<Array<String>> = ColorUtil.getColorNamesM3(
        isDynamic = true,
        prefixG = false
    )
    private val colorNamesM3var3: Array<Array<String>> = ColorUtil.getColorNamesM3(
        isDynamic = true,
        prefixG = true
    )
    private val colorNamesM3var4: Array<Array<String>> = ColorUtil.getColorNamesM3(
        isDynamic = false,
        prefixG = true
    )

    fun createDynamicOverlay(
        overlay: FabricatedOverlayResource,
        paletteLight: ArrayList<ArrayList<Int>>,
        paletteDark: ArrayList<ArrayList<Int>>
    ) {
        assignDynamicPaletteToOverlay(overlay, true,  /* isDark */paletteDark)
        assignDynamicPaletteToOverlay(overlay, false,  /* isDark */paletteLight)
        assignFixedColorsToOverlay(overlay, paletteLight)
    }

    private fun assignDynamicPaletteToOverlay(
        overlay: FabricatedOverlayResource,
        isDark: Boolean,
        palette: ArrayList<ArrayList<Int>>
    ) {
        val suffix = if (isDark) "dark" else "light"
        val isPitchBlackTheme = getBoolean(Const.MONET_PITCH_BLACK_THEME, false)
        val prefixSuffix = arrayOf(
            "system_" to "_${suffix}",
            "m3_sys_color_${suffix}_" to "",
            "m3_sys_color_dynamic_${suffix}_" to "",
            "gm3_sys_color_${suffix}_" to "",
            "gm3_sys_color_dynamic_${suffix}_" to ""
        )

        DynamicColors.ALL_DYNAMIC_COLORS_MAPPED.forEach { colorMapping ->
            for ((tempPrefix, tempSuffix) in prefixSuffix) {
                val (resourceName, colorValue) = extractResourceFromColorMapping(
                    colorMapping = colorMapping,
                    prefix = tempPrefix,
                    suffix = tempSuffix,
                    palette = palette,
                    isDark = isDark
                )

                val adjustedColorValue = adjustColorForPitchBlackThemeIfRequired(
                    pitchBlackTheme = isPitchBlackTheme,
                    resourceName = resourceName,
                    colorValue = colorValue
                ).let { value ->
                    adjustColorBrightnessIfRequired(
                        colorValue = value,
                        colorMapping = colorMapping,
                        isDark = isDark
                    )
                }

                overlay.setColor(resourceName, adjustedColorValue)
            }
        }
    }

    private fun assignFixedColorsToOverlay(
        overlay: FabricatedOverlayResource,
        paletteLight: ArrayList<ArrayList<Int>>
    ) {
        DynamicColors.FIXED_COLORS_MAPPED.forEach { colorMapping ->
            val (resourceName, colorValue) = extractResourceFromColorMapping(
                colorMapping = colorMapping,
                prefix = "system_",
                palette = paletteLight
            )

            overlay.setColor(resourceName, colorValue)
        }
    }

    fun assignPerAppColorsToOverlay(
        overlay: FabricatedOverlayResource,
        palette: ArrayList<ArrayList<Int>>
    ) {
        val pitchBlackTheme = getBoolean(Const.MONET_PITCH_BLACK_THEME, false)

        DynamicColors.M3_REF_PALETTE.forEach { colorMapping ->
            val (resourceName, colorValue) = extractResourceFromColorMapping(
                colorMapping = colorMapping,
                palette = palette
            )

            val adjustedColorValue = adjustColorForPitchBlackThemeIfRequired(
                pitchBlackTheme = pitchBlackTheme,
                resourceName = resourceName,
                colorValue = colorValue
            ).let { value ->
                adjustColorBrightnessIfRequired(
                    colorValue = value,
                    colorMapping = colorMapping,
                    isDark = false
                )
            }

            overlay.setColor(resourceName, adjustedColorValue)
            overlay.setColor("g$resourceName", adjustedColorValue)
        }

        for (i in 0..4) {
            for (j in 0..12) {
                overlay.setColor(colorNamesM3var1[i][j], palette[i][j])
                overlay.setColor(colorNamesM3var2[i][j], palette[i][j])
                overlay.setColor(colorNamesM3var3[i][j], palette[i][j])
                overlay.setColor(colorNamesM3var4[i][j], palette[i][j])
            }
        }

        replaceColorsPerPackageName(overlay, palette, pitchBlackTheme)

        if (!getBoolean(Const.TINT_TEXT_COLOR, true)) {
            addTintlessTextColors(overlay)
        }
    }

    fun updateFabricatedAppList(context: Context) {
        if (Const.workingMethod != Const.WorkMethod.ROOT ||
            !getBoolean(Const.THEMING_ENABLED, true)
        ) return

        val packageManager = context.packageManager
        val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val selectedApps = HashMap<String, Boolean>()

        //        selectedApps.put(BuildConfig.APPLICATION_ID, true);
        for (appInfo in applications) {
            val packageName = appInfo.packageName
            val isSelected = OverlayManager.isOverlayEnabled(
                String.format(Const.FABRICATED_OVERLAY_NAME_APPS, packageName)
            )

            if (isSelected) {
                selectedApps[packageName] = true
            }
        }

        Const.saveSelectedFabricatedApps(selectedApps)
    }

    @ColorInt
    fun adjustColorForPitchBlackThemeIfRequired(
        pitchBlackTheme: Boolean,
        resourceName: String,
        colorValue: Int
    ): Int {
        if (pitchBlackTheme) {
            return when (resourceName) {
                "m3_ref_palette_dynamic_neutral_variant6",
                "gm3_ref_palette_dynamic_neutral_variant6",
                "system_background_dark",
                "system_surface_dark" -> {
                    Color.BLACK
                }

                "m3_ref_palette_dynamic_neutral_variant12",
                "gm3_ref_palette_dynamic_neutral_variant12" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -40
                    )
                }

                "m3_ref_palette_dynamic_neutral_variant17",
                "gm3_ref_palette_dynamic_neutral_variant17",
                "gm3_system_bar_color_night" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -60
                    )
                }

                "system_surface_container_lowest_dark" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -36
                    )
                }

                "system_surface_container_low_dark" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -28
                    )
                }

                "system_surface_container_dark" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -20
                    )
                }

                "system_surface_container_high_dark" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -12
                    )
                }

                "system_surface_container_highest_dark" -> {
                    ColorUtil.modifyBrightness(
                        color = colorValue,
                        brightnessPercentage = -4
                    )
                }

                else -> {
                    colorValue
                }
            }
        }

        return colorValue
    }

    private fun addTintlessTextColors(overlay: FabricatedOverlayResource) {
        val prefixes = arrayOf(
            "m3_sys_color_",
            "m3_sys_color_dynamic_"
        )
        val variants = arrayOf(
            "dark_",
            "light_",
        )
        val suffixes = arrayOf(
            "on_surface",
            "on_surface_variant",
            "on_background"
        )

        for (prefix in prefixes) {
            for (variant in variants) {
                for (suffix in suffixes) {
                    val resourceName = prefix + variant + suffix
                    overlay.setColor(
                        resourceName,
                        if (variant.contains("dark")) Color.WHITE else Color.BLACK
                    )
                }
            }
        }

        // Dark mode
        val resourcesDark = ArrayList<Pair<String, Int>>()
        resourcesDark.add(Pair("m3_ref_palette_dynamic_neutral90", Color.WHITE))
        resourcesDark.add(Pair("m3_ref_palette_dynamic_neutral95", Color.WHITE))
        resourcesDark.add(Pair("m3_ref_palette_dynamic_neutral_variant70", -0x333334))
        resourcesDark.add(Pair("m3_ref_palette_dynamic_neutral_variant80", Color.WHITE))
        resourcesDark.add(Pair("text_color_primary_dark", Color.WHITE))
        resourcesDark.add(Pair("text_color_secondary_dark", -0x4c000001))
        resourcesDark.add(Pair("text_color_tertiary_dark", -0x7f000001))
        resourcesDark.add(Pair("google_dark_default_color_on_background", Color.WHITE))
        resourcesDark.add(Pair("gm_ref_palette_grey500", Color.WHITE))

        // Light mode
        val resourcesLight = ArrayList<Pair<String, Int>>()
        resourcesLight.add(Pair("m3_ref_palette_dynamic_neutral10", Color.BLACK))
        resourcesLight.add(Pair("m3_ref_palette_dynamic_neutral_variant30", -0x4d000000))
        resourcesLight.add(Pair("text_color_primary_light", Color.BLACK))
        resourcesLight.add(Pair("text_color_secondary_light", -0x4d000000))
        resourcesLight.add(Pair("text_color_tertiary_light", -0x80000000))
        resourcesDark.add(Pair("google_default_color_on_background", Color.BLACK))
        resourcesDark.add(Pair("gm_ref_palette_grey700", Color.BLACK))

        for (pair in resourcesDark) {
            overlay.setColor(pair.first, pair.second)
            overlay.setColor("g" + pair.first, pair.second)
        }

        for (pair in resourcesLight) {
            overlay.setColor(pair.first, pair.second)
            overlay.setColor("g" + pair.first, pair.second)
        }

        // For settings text color on android 14
        overlay.setColor("settingslib_text_color_primary_device_default", Color.WHITE, "night")
        overlay.setColor("settingslib_text_color_secondary_device_default", -0x4c000001, "night")
    }

    private fun replaceColorsPerPackageName(
        overlay: FabricatedOverlayResource,
        palette: ArrayList<ArrayList<Int>>,
        pitchBlackTheme: Boolean
    ) {
        if (overlay.targetPackage.startsWith("com.android.systemui.clocks.")) { // Android 14 clocks
            for (i in 0..4) {
                for (j in 0..12) {
                    overlay.setColor(colorNames[i][j], palette[i][j])
                }
            }
        } else if (overlay.targetPackage == "com.google.android.googlequicksearchbox") { // Google Feeds
            if (pitchBlackTheme) {
                overlay.setColor("gm3_ref_palette_dynamic_neutral_variant20", Color.BLACK)
            }
        } else if (overlay.targetPackage == "com.google.android.apps.magazines") { // Google News
            overlay.setColor("cluster_divider_bg", Color.TRANSPARENT)
            overlay.setColor("cluster_divider_border", Color.TRANSPARENT)

            overlay.setColor("appwidget_background_day", palette[3][2])
            overlay.setColor("home_background_day", palette[3][2])
            overlay.setColor("google_default_color_background", palette[3][2])
            overlay.setColor("gm3_system_bar_color_day", palette[4][3])
            overlay.setColor("google_default_color_on_background", palette[3][11])
            overlay.setColor("google_dark_default_color_on_background", palette[3][1])
            overlay.setColor("google_default_color_on_background", palette[3][11])
            overlay.setColor("gm3_system_bar_color_night", palette[4][10])

            if (pitchBlackTheme) {
                overlay.setColor("appwidget_background_night", Color.BLACK)
                overlay.setColor("home_background_night", Color.BLACK)
                overlay.setColor("google_dark_default_color_background", Color.BLACK)
            } else {
                overlay.setColor("appwidget_background_night", palette[3][11])
                overlay.setColor("home_background_night", palette[3][11])
                overlay.setColor("google_dark_default_color_background", palette[3][11])
            }
        } else if (overlay.targetPackage == "com.google.android.play.games") { // Google Play Games
            // Light mode
            overlay.setColor("google_white", palette[3][2])
            overlay.setColor("gm_ref_palette_grey300", palette[4][4])
            overlay.setColor("gm_ref_palette_grey700", palette[3][11])
            overlay.setColor("replay__pal_games_light_600", palette[0][8])

            // Dark mode
            overlay.setColor(
                "gm_ref_palette_grey900",
                if (pitchBlackTheme) Color.BLACK else palette[3][11]
            )
            overlay.setColor("gm_ref_palette_grey600", palette[4][8])
            overlay.setColor("gm_ref_palette_grey500", palette[3][1])
            overlay.setColor("replay__pal_games_dark_300", palette[0][5])
        } else if (overlay.targetPackage == "com.google.android.settings.intelligence" &&
            pitchBlackTheme
        ) { // Settings search
            overlay.setColor("m3_sys_color_dark_surface_container_lowest", Color.BLACK)
            overlay.setColor("gm3_sys_color_dark_surface_container_lowest", Color.BLACK)
            overlay.setColor("m3_sys_color_dynamic_dark_surface_container_lowest", Color.BLACK)
            overlay.setColor("gm3_sys_color_dynamic_dark_surface_container_lowest", Color.BLACK)
            overlay.setColor("m3_sys_color_dark_surface_container_low", Color.BLACK)
            overlay.setColor("gm3_sys_color_dark_surface_container_low", Color.BLACK)
            overlay.setColor("m3_sys_color_dynamic_dark_surface_container_low", Color.BLACK)
            overlay.setColor("gm3_sys_color_dynamic_dark_surface_container_low", Color.BLACK)
            overlay.setColor("m3_sys_color_dark_surface_container", Color.BLACK)
            overlay.setColor("gm3_sys_color_dark_surface_container", Color.BLACK)
            overlay.setColor("m3_sys_color_dynamic_dark_surface_container", Color.BLACK)
            overlay.setColor("gm3_sys_color_dynamic_dark_surface_container", Color.BLACK)
            overlay.setColor("m3_sys_color_dark_surface_container_high", Color.BLACK)
            overlay.setColor("gm3_sys_color_dark_surface_container_high", Color.BLACK)
            overlay.setColor("m3_sys_color_dynamic_dark_surface_container_high", Color.BLACK)
            overlay.setColor("gm3_sys_color_dynamic_dark_surface_container_high", Color.BLACK)
            overlay.setColor("m3_sys_color_dark_surface_container_highest", Color.BLACK)
            overlay.setColor("gm3_sys_color_dark_surface_container_highest", Color.BLACK)
            overlay.setColor("m3_sys_color_dynamic_dark_surface_container_highest", Color.BLACK)
            overlay.setColor("gm3_sys_color_dynamic_dark_surface_container_highest", Color.BLACK)
        }
    }

    private fun extractResourceFromColorMapping(
        colorMapping: ColorMapping,
        prefix: String = "",
        suffix: String = "",
        palette: ArrayList<ArrayList<Int>>,
        isDark: Boolean = false
    ): Pair<String, Int> {
        val resourceName = prefix + colorMapping.resourceName + suffix

        val colorValue: Int = if (colorMapping.tonalPalette != null) {
            if (colorMapping.colorIndex != null) {
                palette[colorMapping.tonalPalette.index][colorMapping.colorIndex]
            } else {
                if (isDark) {
                    palette[colorMapping.tonalPalette.index][colorMapping.darkModeColorIndex!!]
                } else {
                    palette[colorMapping.tonalPalette.index][colorMapping.lightModeColorIndex!!]
                }
            }
        } else {
            colorMapping.colorCode ?: if (isDark) {
                colorMapping.darkModeColorCode
            } else {
                colorMapping.lightModeColorCode
            }
        }!!

        return Pair(resourceName, colorValue)
    }

    private fun adjustColorBrightnessIfRequired(
        colorValue: Int,
        colorMapping: ColorMapping,
        isDark: Boolean
    ): Int {
        return if (colorMapping.lightnessAdjustment != null) {
            ColorUtil.modifyBrightness(
                colorValue,
                colorMapping.lightnessAdjustment
            )
        } else if (colorMapping.darkModeLightnessAdjustment != null && isDark) {
            ColorUtil.modifyBrightness(
                colorValue,
                colorMapping.darkModeLightnessAdjustment
            )
        } else if (colorMapping.lightModeLightnessAdjustment != null && !isDark) {
            ColorUtil.modifyBrightness(
                colorValue,
                colorMapping.lightModeLightnessAdjustment
            )
        } else {
            colorValue
        }
    }
}

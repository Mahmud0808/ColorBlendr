package com.drdisagree.colorblendr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.util.Pair
import androidx.core.util.component1
import androidx.core.util.component2
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.common.Const.TINT_TEXT_COLOR
import com.drdisagree.colorblendr.common.Const.saveSelectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.utils.ColorUtil.getColorNamesM3
import com.drdisagree.colorblendr.utils.ColorUtil.modifyBrightness
import com.drdisagree.colorblendr.utils.DynamicColors.ALL_DYNAMIC_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.DynamicColors.FIXED_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.DynamicColors.M3_REF_PALETTE
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

object FabricatedUtil {
    private val colorNames: Array<Array<String>> = ColorUtil.colorNames
    private val colorNamesM3var1: Array<Array<String>> = getColorNamesM3(
        isDynamic = false,
        prefixG = false
    )
    private val colorNamesM3var2: Array<Array<String>> = getColorNamesM3(
        isDynamic = true,
        prefixG = false
    )
    private val colorNamesM3var3: Array<Array<String>> = getColorNamesM3(
        isDynamic = true,
        prefixG = true
    )
    private val colorNamesM3var4: Array<Array<String>> = getColorNamesM3(
        isDynamic = false,
        prefixG = true
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
                )

                val adjustedColorValue = adjustColorForPitchBlackThemeIfRequired(
                    pitchBlackTheme = isPitchBlackTheme,
                    resourceName = resourceName,
                    colorValue = colorValue
                ).let { value ->
                    colorMapping.adjustColorBrightnessIfRequired(
                        colorValue = value,
                        isDark = isDark
                    )
                }

                setColor(resourceName, adjustedColorValue)
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
        val pitchBlackTheme = getBoolean(MONET_PITCH_BLACK_THEME, false)

        M3_REF_PALETTE.forEach { colorMapping ->
            val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                palette = palette
            )

            val adjustedColorValue = adjustColorForPitchBlackThemeIfRequired(
                pitchBlackTheme = pitchBlackTheme,
                resourceName = resourceName,
                colorValue = colorValue
            ).let { value ->
                colorMapping.adjustColorBrightnessIfRequired(
                    colorValue = value,
                    isDark = false
                )
            }

            setColor(resourceName, adjustedColorValue)
            setColor("g$resourceName", adjustedColorValue)
        }

        for (i in 0..4) {
            for (j in 0..12) {
                setColor(colorNamesM3var1[i][j], palette[i][j])
                setColor(colorNamesM3var2[i][j], palette[i][j])
                setColor(colorNamesM3var3[i][j], palette[i][j])
                setColor(colorNamesM3var4[i][j], palette[i][j])
            }
        }

        replaceColorsPerPackageName(palette, pitchBlackTheme)

        if (!getBoolean(TINT_TEXT_COLOR, true)) {
            addTintlessTextColors()
        }
    }

    fun updateFabricatedAppList(context: Context) {
        if (workingMethod != Const.WorkMethod.ROOT ||
            !getBoolean(THEMING_ENABLED, true)
        ) return

        val packageManager = context.packageManager
        val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val selectedApps = HashMap<String, Boolean>()

        //        selectedApps.put(BuildConfig.APPLICATION_ID, true);
        for (appInfo in applications) {
            val packageName = appInfo.packageName
            val isSelected = OverlayManager.isOverlayEnabled(
                String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)
            )

            if (isSelected) {
                selectedApps[packageName] = true
            }
        }

        saveSelectedFabricatedApps(selectedApps)
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
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -40
                )
            }

            "m3_ref_palette_dynamic_neutral_variant17",
            "gm3_ref_palette_dynamic_neutral_variant17",
            "gm3_system_bar_color_night" -> {
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -60
                )
            }

            "system_surface_container_lowest_dark" -> {
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -44
                )
            }

            "system_surface_container_low_dark" -> {
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -36
                )
            }

            "system_surface_container_dark" -> {
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -28
                )
            }

            "system_surface_container_high_dark",
            "system_surface_dim_dark" -> {
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -20
                )
            }

            "system_surface_container_highest_dark",
            "system_surface_bright_dark" -> {
                modifyBrightness(
                    color = colorValue,
                    brightnessPercentage = -12
                )
            }

            else -> {
                colorValue
            }
        }
    }

    private fun FabricatedOverlayResource.addTintlessTextColors() {
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
                    setColor(
                        resourceName,
                        if (variant.contains("dark")) Color.WHITE else Color.BLACK
                    )
                }
            }
        }

        // Dark mode
        val resourcesDark = ArrayList<Pair<String, Int>>().apply {
            add(Pair("m3_ref_palette_dynamic_neutral90", Color.WHITE))
            add(Pair("m3_ref_palette_dynamic_neutral95", Color.WHITE))
            add(Pair("m3_ref_palette_dynamic_neutral_variant70", -0x333334))
            add(Pair("m3_ref_palette_dynamic_neutral_variant80", Color.WHITE))
            add(Pair("text_color_primary_dark", Color.WHITE))
            add(Pair("text_color_secondary_dark", -0x4c000001))
            add(Pair("text_color_tertiary_dark", -0x7f000001))
            add(Pair("google_dark_default_color_on_background", Color.WHITE))
            add(Pair("gm_ref_palette_grey500", Color.WHITE))
        }

        // Light mode
        val resourcesLight = ArrayList<Pair<String, Int>>().apply {
            add(Pair("m3_ref_palette_dynamic_neutral10", Color.BLACK))
            add(Pair("m3_ref_palette_dynamic_neutral_variant30", -0x4d000000))
            add(Pair("text_color_primary_light", Color.BLACK))
            add(Pair("text_color_secondary_light", -0x4d000000))
            add(Pair("text_color_tertiary_light", -0x80000000))
            add(Pair("google_default_color_on_background", Color.BLACK))
            add(Pair("gm_ref_palette_grey700", Color.BLACK))
        }

        for (pair in resourcesDark) {
            setColor(pair.first, pair.second)
            setColor("g" + pair.first, pair.second)
        }

        for (pair in resourcesLight) {
            setColor(pair.first, pair.second)
            setColor("g" + pair.first, pair.second)
        }

        if (targetPackage == "com.android.settings") {
            // For settings text color on android 14
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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

            // For settings text color on android 15
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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

    private fun FabricatedOverlayResource.replaceColorsPerPackageName(
        palette: ArrayList<ArrayList<Int>>,
        pitchBlackTheme: Boolean
    ) {
        if (targetPackage.startsWith("com.android.systemui.clocks.")) { // Android 14 clocks
            for (i in 0..4) {
                for (j in 0..12) {
                    setColor(colorNames[i][j], palette[i][j])
                }
            }
        } else if (targetPackage == "com.google.android.googlequicksearchbox") { // Google Feeds
            if (pitchBlackTheme) {
                setColor("gm3_ref_palette_dynamic_neutral_variant20", Color.BLACK)
            }
        } else if (targetPackage == "com.google.android.apps.magazines") { // Google News
            setColor("cluster_divider_bg", Color.TRANSPARENT)
            setColor("cluster_divider_border", Color.TRANSPARENT)

            setColor("appwidget_background_day", palette[3][2])
            setColor("home_background_day", palette[3][2])
            setColor("google_default_color_background", palette[3][2])
            setColor("gm3_system_bar_color_day", palette[4][3])
            setColor("google_default_color_on_background", palette[3][11])
            setColor("google_dark_default_color_on_background", palette[3][1])
            setColor("google_default_color_on_background", palette[3][11])
            setColor("gm3_system_bar_color_night", palette[4][10])

            if (pitchBlackTheme) {
                setColor("appwidget_background_night", Color.BLACK)
                setColor("home_background_night", Color.BLACK)
                setColor("google_dark_default_color_background", Color.BLACK)
            } else {
                setColor("appwidget_background_night", palette[3][11])
                setColor("home_background_night", palette[3][11])
                setColor("google_dark_default_color_background", palette[3][11])
            }
        } else if (targetPackage == "com.google.android.play.games") { // Google Play Games
            // Light mode
            setColor("google_white", palette[3][2])
            setColor("gm_ref_palette_grey300", palette[4][4])
            setColor("gm_ref_palette_grey700", palette[3][11])
            setColor("replay__pal_games_light_600", palette[0][8])

            // Dark mode
            setColor(
                "gm_ref_palette_grey900",
                if (pitchBlackTheme) Color.BLACK else palette[3][11]
            )
            setColor("gm_ref_palette_grey600", palette[4][8])
            setColor("gm_ref_palette_grey500", palette[3][1])
            setColor("replay__pal_games_dark_300", palette[0][5])
        } else if (targetPackage == "com.android.settings") { // Settings app
            if (Build.VERSION.SDK_INT >= 35 && pitchBlackTheme) { // Android 15 pitch black settings
                setColor(
                    "settingslib_materialColorSurfaceContainer",
                    Color.BLACK
                ) // inner page background
                setColor("settingslib_materialColorSurfaceVariant", palette[3][11]) // app bar
                setColor("settingslib_colorSurfaceHeader", palette[3][11]) // app bar
            }
        } else if (targetPackage == "com.google.android.settings.intelligence" && pitchBlackTheme) { // Settings search
            setColor("m3_sys_color_dark_surface_container_lowest", Color.BLACK)
            setColor("gm3_sys_color_dark_surface_container_lowest", Color.BLACK)
            setColor("m3_sys_color_dynamic_dark_surface_container_lowest", Color.BLACK)
            setColor("gm3_sys_color_dynamic_dark_surface_container_lowest", Color.BLACK)
            setColor("m3_sys_color_dark_surface_container_low", Color.BLACK)
            setColor("gm3_sys_color_dark_surface_container_low", Color.BLACK)
            setColor("m3_sys_color_dynamic_dark_surface_container_low", Color.BLACK)
            setColor("gm3_sys_color_dynamic_dark_surface_container_low", Color.BLACK)
            setColor("m3_sys_color_dark_surface_container", Color.BLACK)
            setColor("gm3_sys_color_dark_surface_container", Color.BLACK)
            setColor("m3_sys_color_dynamic_dark_surface_container", Color.BLACK)
            setColor("gm3_sys_color_dynamic_dark_surface_container", Color.BLACK)
            setColor("m3_sys_color_dark_surface_container_high", Color.BLACK)
            setColor("gm3_sys_color_dark_surface_container_high", Color.BLACK)
            setColor("m3_sys_color_dynamic_dark_surface_container_high", Color.BLACK)
            setColor("gm3_sys_color_dynamic_dark_surface_container_high", Color.BLACK)
            setColor("m3_sys_color_dark_surface_container_highest", Color.BLACK)
            setColor("gm3_sys_color_dark_surface_container_highest", Color.BLACK)
            setColor("m3_sys_color_dynamic_dark_surface_container_highest", Color.BLACK)
            setColor("gm3_sys_color_dynamic_dark_surface_container_highest", Color.BLACK)
        }
    }

    private fun ColorMapping.extractResourceFromColorMap(
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

    private fun ColorMapping.adjustColorBrightnessIfRequired(
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
}

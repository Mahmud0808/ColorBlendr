package com.drdisagree.colorblendr.utils.fabricated

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.util.component1
import androidx.core.util.component2
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.LINEAGE_PARTS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_LINEAGEOS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH_AOSP
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER_GOOGLE
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.utils.app.SystemUtil.isAppInstalled
import com.drdisagree.colorblendr.utils.app.SystemUtil.isDarkMode
import com.drdisagree.colorblendr.utils.colors.ColorMapping
import com.drdisagree.colorblendr.utils.colors.ColorUtil.adjustLightness
import com.drdisagree.colorblendr.utils.colors.ColorUtil.convertToMonochrome
import com.drdisagree.colorblendr.utils.colors.ColorUtil.getColorNamesM3
import com.drdisagree.colorblendr.utils.colors.ColorUtil.withLStarAndAlpha
import com.drdisagree.colorblendr.utils.colors.DynamicColors.ALL_DYNAMIC_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.colors.DynamicColors.CUSTOM_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.colors.DynamicColors.FIXED_COLORS_MAPPED
import com.drdisagree.colorblendr.utils.colors.DynamicColors.M3_REF_PALETTE
import com.drdisagree.colorblendr.utils.colors.adjustColorBrightnessIfRequired
import com.drdisagree.colorblendr.utils.colors.adjustLStarIfRequired
import com.drdisagree.colorblendr.utils.colors.extractResourceFromColorMap
import com.drdisagree.colorblendr.utils.colors.replaceColorsPerPackageName
import com.drdisagree.colorblendr.utils.manager.OverlayManager

object FabricatedUtil {

    private val colorNamesM3Variants = listOf(
        getColorNamesM3(isDynamic = false, prefixG = false),
        getColorNamesM3(isDynamic = true, prefixG = false),
        getColorNamesM3(isDynamic = true, prefixG = true),
        getColorNamesM3(isDynamic = false, prefixG = true)
    )

    fun FabricatedOverlayResource.assignFullPaletteToOverlay(
        paletteLight: ArrayList<ArrayList<Int>>,
        paletteDark: ArrayList<ArrayList<Int>>,
        isDarkMode: Boolean
    ) {
        val palette = if (isDarkMode) paletteDark else paletteLight
        val pitchBlack = pitchBlackThemeEnabled()

        // 1. M3 Tonal Palette (Reference Palette)
        val m3Shades = mapOf(
            0 to 12, 10 to 11, 20 to 10, 30 to 9, 40 to 8, 50 to 7,
            60 to 6, 70 to 5, 80 to 4, 90 to 3, 95 to 2, 98 to 1, 100 to 0
        )

        val tonalPalettes = listOf(
            "primary" to 0, "secondary" to 1, "tertiary" to 2,
            "neutral" to 3, "neutral_variant" to 4, "error" to 5
        )

        tonalPalettes.forEach { (name, paletteIndex) ->
            m3Shades.forEach { (m3Shade, monetIndex) ->
                val lightColor = paletteLight[paletteIndex][monetIndex]
                val darkColor = paletteDark[paletteIndex][monetIndex]

                val resName = "m3_ref_palette_${name}${m3Shade}"
                val resNameDynamic = "m3_ref_palette_dynamic_${name}${m3Shade}"

                val adjustedLight = adjustColorForPitchBlackThemeIfRequired(pitchBlack, resName, lightColor)
                val adjustedDark = adjustColorForPitchBlackThemeIfRequired(pitchBlack, resName, darkColor)
                val defaultColor = if (isDarkMode) adjustedDark else adjustedLight

                setColor(resName, defaultColor, true)
                setColor(resName, adjustedDark, "night", true)
                setColor(resNameDynamic, defaultColor, true)
                setColor(resNameDynamic, adjustedDark, "night", true)

                setColor("g$resName", defaultColor, true)
                setColor("g$resName", adjustedDark, "night", true)
                setColor("g$resNameDynamic", defaultColor, true)
                setColor("g$resNameDynamic", adjustedDark, "night", true)
            }
        }

        // 2. Dynamic Palette Roles (system_*, m3_sys_color_*, etc.)
        assignDynamicPaletteToOverlay(true, paletteDark, true)
        assignDynamicPaletteToOverlay(false, paletteLight, true)
        assignDynamicPalettePascalRolesToOverlay(isDarkMode, paletteLight, paletteDark, true)

        // 3. Fixed Colors
        assignFixedColorsToOverlay(paletteLight, true)

        // 4. Custom Colors
        assignCustomColorsToOverlay(true, paletteDark, true)
        assignCustomColorsToOverlay(false, paletteLight, true)
        // Mode-aware for /e/OS
        assignCustomColorsToEOS(isDarkMode, paletteLight, paletteDark, true)

        // 5. M3 Variants
        colorNamesM3Variants.forEach { variant ->
            variant.forEachIndexed { i, row ->
                row.forEachIndexed { j, name ->
                    setColor(name, palette[i][j], true)
                }
            }
        }

        // 6. Tintless text colors
        if (!tintedTextEnabled()) {
            addTintlessTextColors(true)
        }
    }

    private fun FabricatedOverlayResource.assignDynamicPaletteToOverlay(
        isDark: Boolean,
        palette: ArrayList<ArrayList<Int>>,
        useIfExists: Boolean = false
    ) {
        val suffix = if (isDark) "dark" else "light"
        val tintTextColor = tintedTextEnabled()
        val isPitchBlackTheme = pitchBlackThemeEnabled()
        val prefixSuffix = arrayOf(
            "system_" to "_${suffix}",
            "m3_sys_color_${suffix}_" to "",
            "m3_sys_color_dynamic_${suffix}_" to "",
            "gm3_sys_color_${suffix}_" to "",
            "gm3_sys_color_dynamic_${suffix}_" to ""
        )
        val textColorResources = setOf(
            "on_surface",
            "on_surface_variant",
            "on_background",
            "on_primary_container",
            "on_secondary_container",
            "on_tertiary_container",
            "on_error"
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

                if (!tintTextColor && textColorResources.any { resourceName.contains(it) }) {
                    val tintlessColor = computeTintlessFrameworkTextColor(resourceName, colorValue)
                    setColor(resourceName, tintlessColor, useIfExists)
                } else {
                    setColor(resourceName, colorValue, useIfExists)
                }
            }
        }
    }

    private fun FabricatedOverlayResource.assignDynamicPalettePascalRolesToOverlay(
        isDark: Boolean,
        paletteLight: ArrayList<ArrayList<Int>>,
        paletteDark: ArrayList<ArrayList<Int>>,
        useIfExists: Boolean = false
    ) {
        val palette = if (isDark) paletteDark else paletteLight
        val tintTextColor = tintedTextEnabled()
        val isPitchBlackTheme = pitchBlackThemeEnabled()
        val prefixSuffix = arrayOf(
            "media_dialog_" to "",
            "media_" to ""
        )
        val textColorResources = setOf(
            "on_surface",
            "on_surface_variant",
            "on_background",
            "on_primary_container",
            "on_secondary_container",
            "on_tertiary_container",
            "on_error"
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

                if (!tintTextColor && textColorResources.any { resourceName.contains(it) }) {
                    val tintlessColor = computeTintlessFrameworkTextColor(resourceName, colorValue)
                    setColor(resourceName, tintlessColor, useIfExists)
                } else {
                    setColor(resourceName, colorValue, useIfExists)
                }
            }

            // Handle PascalCase roles for SettingsLib and modern framework
            val pascalRole = colorMapping.resourceName.split("_").joinToString("") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            val pascalPrefixes = arrayOf("settingslib_materialColor", "materialColor")

            pascalPrefixes.forEach { prefix ->
                val resName = "$prefix$pascalRole"
                val (_, colorValue) = colorMapping.extractResourceFromColorMap(
                    palette = palette,
                    isDark = isDark
                ).let { (_, value) ->
                    resName to applyColorAdjustments(
                        colorMapping,
                        resName,
                        value,
                        isDark,
                        isPitchBlackTheme
                    )
                }

                if (!tintTextColor && textColorResources.any {
                        resName.contains(
                            it,
                            ignoreCase = true
                        )
                    }) {
                    val tintlessColor = computeTintlessFrameworkTextColor(resName, colorValue)
                    setColor(resName, tintlessColor, useIfExists)
                } else {
                    setColor(resName, colorValue, useIfExists)
                }
            }
        }
    }

    fun computeTintlessFrameworkTextColor(resourceName: String, colorValue: Int): Int {
        val isDark = resourceName.contains("_dark", ignoreCase = true)
                || resourceName.contains("_night", ignoreCase = true)
        val isLight = resourceName.contains("light", ignoreCase = true)
        val isError = resourceName.contains("on_error", ignoreCase = true)
        val isErrorContainer = resourceName.contains("on_error_container", ignoreCase = true)

        return when {
            isDark && (!isError || isErrorContainer) -> Color.WHITE
            isLight && (!isError || isErrorContainer) -> Color.BLACK
            isDark -> Color.BLACK
            isLight -> Color.WHITE
            else -> convertToMonochrome(colorValue)
        }
    }

    private fun FabricatedOverlayResource.assignFixedColorsToOverlay(
        paletteLight: ArrayList<ArrayList<Int>>,
        useIfExists: Boolean = false
    ) {
        FIXED_COLORS_MAPPED.forEach { colorMapping ->
            val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                prefix = "system_",
                palette = paletteLight,
                isDark = false
            )

            setColor(resourceName, colorValue, useIfExists)
        }
    }

    fun FabricatedOverlayResource.assignPerAppColorsToOverlay(
        paletteLight: ArrayList<ArrayList<Int>>,
        paletteDark: ArrayList<ArrayList<Int>>,
    ) {
        val isDark = isDarkMode
        val palette = if (isDark) paletteDark else paletteLight
        val isPitchBlackTheme = pitchBlackThemeEnabled()
        val tintTextColor = tintedTextEnabled()

        M3_REF_PALETTE.forEach { colorMapping ->
            val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                palette = palette,
                isDark = false
            ).let { (name, value) ->
                name to applyColorAdjustments(
                    colorMapping,
                    name,
                    value,
                    isDark = false,
                    isPitchBlackTheme
                )
            }

            setColor(resourceName, colorValue, true)
            setColor("g$resourceName", colorValue, true)
        }

        assignFullPaletteToOverlay(palette, palette, isDarkMode)

        replaceColorsPerPackageName(palette, isPitchBlackTheme)

        if (!tintTextColor) {
            addTintlessTextColors()
        }
    }

    private fun FabricatedOverlayResource.assignCustomColorsToOverlay(
        isDark: Boolean,
        palette: ArrayList<ArrayList<Int>>,
        useIfExists: Boolean = false
    ) {
        val suffix = if (isDark) "_dark" else "_light"

        CUSTOM_COLORS_MAPPED.forEach { colorMapping ->
            val (resourceName, colorValue) = colorMapping.extractResourceFromColorMap(
                prefix = "system_",
                suffix = suffix,
                palette = palette,
                isDark = isDark
            ).let { (name, value) ->
                name to applyColorAdjustments(
                    colorMapping,
                    name,
                    value,
                    isDark,
                    pitchBlackTheme = false
                )
            }

            setColor(resourceName, colorValue, useIfExists)

            // Handle PascalCase roles for /e/OS customColor...
            val pascalRole = colorMapping.resourceName.split("_").joinToString("") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            val customColorResName = "customColor$pascalRole"
            val (_, customColorValue) = colorMapping.extractResourceFromColorMap(
                palette = palette,
                isDark = isDark
            ).let { (_, value) ->
                customColorResName to applyColorAdjustments(
                    colorMapping,
                    customColorResName,
                    value,
                    isDark,
                    pitchBlackTheme = false
                )
            }

            setColor(customColorResName, customColorValue, useIfExists)
        }
    }

    private fun FabricatedOverlayResource.assignCustomColorsToEOS(
        isDarkMode: Boolean,
        paletteLight: ArrayList<ArrayList<Int>>,
        paletteDark: ArrayList<ArrayList<Int>>,
        useIfExists: Boolean = false
    ) {
        val primaryIndex = 0
        val neutralIndex = 3

        // accent_device_default mapping
        val accentLight = paletteLight[primaryIndex][8]
        val accentDark = paletteDark[primaryIndex][4]
        val effAccent = if (isDarkMode) accentDark else accentLight

        setColor("accent_device_default_light", accentLight, useIfExists)
        setColor("accent_device_default_dark", accentDark, useIfExists)
        // Some ROMs might use these unqualified with night qualifier
        setColor("accent_device_default", effAccent, useIfExists)
        setColor("accent_device_default", accentDark, "night", useIfExists)

        val tileInactiveLight = paletteLight[neutralIndex][3]
        val tileInactiveDark = paletteDark[neutralIndex][10]
        val effTileInactive = if (isDarkMode) tileInactiveDark else tileInactiveLight

        setColor("e_qs_tile_inactive_light", tileInactiveLight, useIfExists)
        setColor("e_qs_tile_inactive_dark", tileInactiveDark, useIfExists)
        setColor("e_qs_tile_inactive", effTileInactive, useIfExists)
        setColor("e_qs_tile_inactive", tileInactiveDark, "night", useIfExists)

        // /e/OS Specific Background and Text Colors
        val surfaceLight = paletteLight[neutralIndex][2]
        val onSurfaceLight = paletteLight[neutralIndex][11]
        val onSurfaceVariantLight = paletteLight[neutralIndex][10]
        val surfaceContainerLight = paletteLight[neutralIndex][3]
        val surfaceContainerHighLight = paletteLight[neutralIndex][4]

        val surfaceDark = paletteDark[neutralIndex][12]
        val onSurfaceDark = paletteDark[neutralIndex][2]
        val onSurfaceVariantDark = paletteDark[neutralIndex][3]
        val surfaceContainerDark = paletteDark[neutralIndex][11]
        val surfaceContainerHighDark = paletteDark[neutralIndex][10]

        val effSurface = if (isDarkMode) surfaceDark else surfaceLight
        val effOnSurface = if (isDarkMode) onSurfaceDark else onSurfaceLight
        val effOnSurfaceVariant = if (isDarkMode) onSurfaceVariantDark else onSurfaceVariantLight
        val effSurfaceContainer = if (isDarkMode) surfaceContainerDark else surfaceContainerLight
        val effSurfaceContainerHigh = if (isDarkMode) surfaceContainerHighDark else surfaceContainerHighLight

        val eBackgroundResNames = arrayOf("e_background", "e_action_bar", "e_drawer_background")
        eBackgroundResNames.forEach { name ->
            // Set default (matching current mode) and night qualifier
            setColor(name, effSurface, useIfExists)
            setColor(name, surfaceDark, "night", useIfExists)

            // Explicit _dark and _light versions
            setColor("${name}_dark", surfaceDark, useIfExists)
            setColor("${name}_light", surfaceLight, useIfExists)
        }

        // Background Variant
        setColor("e_background_variant", effSurfaceContainer, useIfExists)
        setColor("e_background_variant", surfaceContainerDark, "night", useIfExists)
        setColor("e_background_variant_dark", surfaceContainerDark, useIfExists)
        setColor("e_background_variant_light", surfaceContainerLight, useIfExists)

        // Notification Background (The cards)
        setColor("e_notification_background", effSurfaceContainerHigh, useIfExists)
        setColor("e_notification_background", surfaceContainerHighDark, "night", useIfExists)
        setColor("e_notification_background_dark", surfaceContainerHighDark, useIfExists)
        setColor("e_notification_background_light", surfaceContainerHighLight, useIfExists)

        // Primary Text
        setColor("e_primary_text_color", effOnSurface, useIfExists)
        setColor("e_primary_text_color", onSurfaceDark, "night", useIfExists)
        setColor("e_primary_text_color_dark", onSurfaceDark, useIfExists)
        setColor("e_primary_text_color_light", onSurfaceLight, useIfExists)

        // Secondary Text
        setColor("e_secondary_text_color", effOnSurfaceVariant, useIfExists)
        setColor("e_secondary_text_color", onSurfaceVariantDark, "night", useIfExists)
        setColor("e_secondary_text_color_dark", onSurfaceVariantDark, useIfExists)
        setColor("e_secondary_text_color_light", onSurfaceVariantLight, useIfExists)
        setColor("e_secondary_text_color_variant", effOnSurfaceVariant, useIfExists)
        setColor("e_secondary_text_color_variant", onSurfaceVariantDark, "night", useIfExists)
        setColor("e_secondary_text_color_variant_dark", onSurfaceVariantDark, useIfExists)
        setColor("e_secondary_text_color_variant_light", onSurfaceVariantLight, useIfExists)

        // QS Background
        setColor("e_qs_background", effSurface, useIfExists)
        setColor("e_qs_background", surfaceDark, "night", useIfExists)
        setColor("e_qs_background_dark", surfaceDark, useIfExists)
        setColor("e_qs_background_light", surfaceLight, useIfExists)

        val eAccentColor = if (isDarkMode) accentDark else accentLight
        val eAccentInverseColor = if (isDarkMode) accentLight else accentDark
        val eAlphaAccentColor = ColorUtils.setAlphaComponent(eAccentColor, 0x14)

        setColor("e_accent", eAccentColor, useIfExists)
        setColor("e_accent", accentDark, "night", useIfExists)
        setColor("e_accent_dark", accentDark, useIfExists)
        setColor("e_accent_light", accentLight, useIfExists)

        setColor("e_accent_inverse", eAccentInverseColor, useIfExists)
        setColor("e_accent_inverse", accentLight, "night", useIfExists)
        setColor("e_accent_inverse_dark", accentLight, useIfExists)
        setColor("e_accent_inverse_light", accentDark, useIfExists)

        setColor("e_alpha_accent", eAlphaAccentColor, useIfExists)
        setColor(
            "e_alpha_accent",
            ColorUtils.setAlphaComponent(accentDark, 0x14),
            "night",
            useIfExists
        )
        setColor("e_alpha_accent_dark", ColorUtils.setAlphaComponent(accentDark, 0x14), useIfExists)
        setColor(
            "e_alpha_accent_light",
            ColorUtils.setAlphaComponent(accentLight, 0x14),
            useIfExists
        )

        if (targetPackage == SYSTEMUI_PACKAGE) {
            setColor("brightness_slider_overlay_color", eAccentColor, useIfExists)
            setColor(
                "brightness_slider_track",
                if (isDarkMode) surfaceContainerDark else surfaceContainerLight,
                useIfExists
            )
        }
    }

    fun updateFabricatedAppList(context: Context) {
        if (!isRootMode() || !isThemingEnabled()) return

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

        //        selectedApps.put(BuildConfig.APPLICATION_ID, true)

        // Themed by default for android 15+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            listOf(
                SETTINGS,
                SETTINGS_LINEAGEOS,
                SETTINGS_SEARCH,
                SETTINGS_SEARCH_AOSP,
                LINEAGE_PARTS,
                THEME_PICKER,
                THEME_PICKER_GOOGLE
            ).forEach { packageName ->
                if (selectedApps[packageName] != java.lang.Boolean.TRUE &&
                    isAppInstalled(packageName)
                ) {
                    selectedApps[packageName] = true
                }
            }
        }

        setSelectedFabricatedApps(selectedApps)
    }

    fun applyColorAdjustments(
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
                .let { adjustedBrightness ->
                    colorMapping.adjustLStarIfRequired(adjustedBrightness, isDark)
                }
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

    private fun FabricatedOverlayResource.addTintlessTextColors(useIfExists: Boolean = false) {
        val prefixes = arrayOf("m3_sys_color_", "m3_sys_color_dynamic_")
        val variants = arrayOf("dark_", "light_")
        val suffixes = arrayOf("on_surface", "on_surface_variant", "on_background")

        prefixes.forEach { prefix ->
            variants.forEach { variant ->
                suffixes.forEach { suffix ->
                    setColor(
                        "$prefix$variant$suffix",
                        if (variant.contains("dark")) Color.WHITE else Color.BLACK,
                        useIfExists
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
                setColor(name, color, useIfExists)
                if (name.startsWith("m3")) {
                    setColor("g$name", color, useIfExists)
                }
            }
        }

        when {
            targetPackage == SETTINGS -> {
                when {
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        val res1 = "settingslib_text_color_primary_device_default"
                        val res2 = "settingslib_text_color_secondary_device_default"

                        setColor(res1, Color.WHITE, "night", useIfExists)
                        setColor(res2, -0x4c000001, "night", useIfExists)
                    }

                    else -> {
                        val res1 = "settingslib_materialColorOnSurface"
                        val res2 = "settingslib_materialColorOnSurfaceVariant"

                        setColor(res1, Color.WHITE, "night", useIfExists)
                        setColor(res2, -0x4c000001, "night", useIfExists)
                    }
                }
            }
        }
    }

    // Source: https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/packages/SystemUI/src/com/android/systemui/common/shared/colors/SurfaceEffectColors.kt
    fun FabricatedOverlayResource.generateSurfaceEffectColors(
        isDark: Boolean,
        useIfExists: Boolean = false
    ) {
        val colorResNames = listOf(
            "surface_effect_0_color",
            "surface_effect_1_color",
            "surface_effect_2_color",
            "surface_effect_3_color"
        )
        val newColorResNames = listOf(
            "system_surface_effect_0_light" to "system_surface_effect_0_dark",
            "system_surface_effect_1_light" to "system_surface_effect_1_dark",
            "system_surface_effect_2_light" to "system_surface_effect_2_dark",
            "system_surface_effect_3_light" to "system_surface_effect_3_dark",
        )
        // Pair of (light mode, dark mode)
        val sourceColorResNames = listOf(
            "system_accent1_100" to "system_accent1_800",
            "system_neutral1_500" to "system_neutral1_500",
            "system_accent1_0" to "system_accent1_100",
            "system_accent1_600" to "system_accent1_100",
        )
        val lStarValue = listOf(
            null to null,
            98.toDouble() to 6.toDouble(),
            null to null,
            null to null
        )
        val alphaValue = listOf(
            0.5f to 0.5f,
            0.54f to 0.54f,
            0.32f to 0.15f,
            0.15f to 0.10f
        )

        colorResNames.forEachIndexed { i, colorResName ->
            val lightColorValue = getColor(sourceColorResNames[i].first)
                .withLStarAndAlpha(lStarValue[i].first, alphaValue[i].first)
            val darkColorValue = getColor(sourceColorResNames[i].second)
                .withLStarAndAlpha(lStarValue[i].second, alphaValue[i].second)
            val colorValue = if (!isDark) lightColorValue else darkColorValue

            setColor(colorResName, colorValue, useIfExists)
            setColor(newColorResNames[i].first, lightColorValue, useIfExists)
            setColor(newColorResNames[i].second, darkColorValue, useIfExists)
        }
    }
}
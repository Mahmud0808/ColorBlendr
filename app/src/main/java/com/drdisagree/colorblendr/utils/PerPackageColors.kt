package com.drdisagree.colorblendr.utils

import android.graphics.Color
import android.os.Build
import com.drdisagree.colorblendr.data.common.Const.DARKER_LAUNCHER_ICONS
import com.drdisagree.colorblendr.data.common.Const.GOOGLE_FEEDS
import com.drdisagree.colorblendr.data.common.Const.GOOGLE_NEWS
import com.drdisagree.colorblendr.data.common.Const.PIXEL_LAUNCHER
import com.drdisagree.colorblendr.data.common.Const.PLAY_GAMES
import com.drdisagree.colorblendr.data.common.Const.SEMI_TRANSPARENT_LAUNCHER_ICONS
import com.drdisagree.colorblendr.data.common.Const.SETTINGS
import com.drdisagree.colorblendr.data.common.Const.SETTINGS_SEARCH
import com.drdisagree.colorblendr.data.common.Const.SYSTEMUI_CLOCKS
import com.drdisagree.colorblendr.data.config.Prefs.getBoolean
import com.drdisagree.colorblendr.utils.ColorUtil.applyAlphaToColor
import com.drdisagree.colorblendr.utils.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.SystemUtil.isDarkMode
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

fun FabricatedOverlayResource.replaceColorsPerPackageName(
    palette: ArrayList<ArrayList<Int>>,
    pitchBlackTheme: Boolean
) {
    when {
        targetPackage.startsWith(SYSTEMUI_CLOCKS) -> applyClockColors(palette)
        else -> when (targetPackage) {
            GOOGLE_FEEDS -> applyGoogleFeedsColors(pitchBlackTheme)
            GOOGLE_NEWS -> applyGoogleNewsColors(palette, pitchBlackTheme)
            PLAY_GAMES -> applyPlayGamesColors(palette, pitchBlackTheme)
            SETTINGS -> applySettingsColors(palette, pitchBlackTheme)
            SETTINGS_SEARCH -> applySettingsSearchColors(pitchBlackTheme)
            PIXEL_LAUNCHER -> applyPixelLauncherColors(palette, pitchBlackTheme)
        }
    }
}

private fun FabricatedOverlayResource.applyClockColors(palette: ArrayList<ArrayList<Int>>) {
    for (i in systemPaletteNames.indices) {
        for (j in systemPaletteNames[i].indices) {
            setColor(systemPaletteNames[i][j], palette[i][j])
        }
    }
}

private fun FabricatedOverlayResource.applyGoogleFeedsColors(pitchBlackTheme: Boolean) {
    if (!pitchBlackTheme || !isDarkMode) return

    setColor("gm3_ref_palette_dynamic_neutral_variant20", Color.BLACK)
}

private fun FabricatedOverlayResource.applyGoogleNewsColors(
    palette: ArrayList<ArrayList<Int>>,
    pitchBlackTheme: Boolean
) {
    // Divider colors
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
}

private fun FabricatedOverlayResource.applyPlayGamesColors(
    palette: ArrayList<ArrayList<Int>>,
    pitchBlackTheme: Boolean
) {
    // Light mode
    setColor("google_white", palette[3][2])
    setColor("gm_ref_palette_grey300", palette[4][4])
    setColor("gm_ref_palette_grey700", palette[3][11])
    setColor("replay__pal_games_light_600", palette[0][8])

    // Dark mode
    setColor("gm_ref_palette_grey900", if (pitchBlackTheme) Color.BLACK else palette[3][11])
    setColor("gm_ref_palette_grey600", palette[4][8])
    setColor("gm_ref_palette_grey500", palette[3][1])
    setColor("replay__pal_games_dark_300", palette[0][5])
}

private fun FabricatedOverlayResource.applySettingsColors(
    palette: ArrayList<ArrayList<Int>>,
    pitchBlackTheme: Boolean
) {
    if (!pitchBlackTheme || !isDarkMode || Build.VERSION.SDK_INT < 35) return

    setColor("settingslib_materialColorSurfaceContainer", Color.BLACK) // inner page background
    setColor("settingslib_materialColorSurfaceVariant", palette[3][11]) // app bar
    setColor("settingslib_colorSurfaceHeader", palette[3][11]) // app bar
}

private fun FabricatedOverlayResource.applySettingsSearchColors(pitchBlackTheme: Boolean) {
    if (!pitchBlackTheme) return

    listOf(
        "m3_sys_color_dark_surface_container_lowest",
        "gm3_sys_color_dark_surface_container_lowest",
        "m3_sys_color_dynamic_dark_surface_container_lowest",
        "gm3_sys_color_dynamic_dark_surface_container_lowest",
        "m3_sys_color_dark_surface_container_low",
        "gm3_sys_color_dark_surface_container_low",
        "m3_sys_color_dynamic_dark_surface_container_low",
        "gm3_sys_color_dynamic_dark_surface_container_low",
        "m3_sys_color_dark_surface_container",
        "gm3_sys_color_dark_surface_container",
        "m3_sys_color_dynamic_dark_surface_container",
        "gm3_sys_color_dynamic_dark_surface_container",
        "m3_sys_color_dark_surface_container_high",
        "gm3_sys_color_dark_surface_container_high",
        "m3_sys_color_dynamic_dark_surface_container_high",
        "gm3_sys_color_dynamic_dark_surface_container_high",
        "m3_sys_color_dark_surface_container_highest",
        "gm3_sys_color_dark_surface_container_highest",
        "m3_sys_color_dynamic_dark_surface_container_highest",
        "gm3_sys_color_dynamic_dark_surface_container_highest"
    ).forEach { setColor(it, Color.BLACK) }
}

private fun FabricatedOverlayResource.applyPixelLauncherColors(
    palette: ArrayList<ArrayList<Int>>,
    pitchBlackTheme: Boolean
) {
    /*
     * qsb_icon_tint_quaternary_mono = monochrome icon color; removed in Android 15 QPR1 beta 3
     * themed_icon_color = monochrome icon color; added in Android 15 QPR1 beta 3
     * themed_icon_background_color = monochrome icon background
     * material_color_surface_container_low = search bar color in homepage before Android 15 QPR1 beta 3
     * system_surface_container_low_dark/light = search bar color in homepage in Android 15 QPR1 beta 3
     * material_color_surface_bright = search bar color in app drawer
     * material_color_surface_dim = app drawer background color
     * folder_preview_dark/light = folder preview background color
     * folder_background_dark/light = expanded folder background color
     */

    val isSemiTransparent = getBoolean(SEMI_TRANSPARENT_LAUNCHER_ICONS, false)

    if (isDarkMode) {
        val darkerIcon = getBoolean(DARKER_LAUNCHER_ICONS, false)

        setColor("qsb_icon_tint_quaternary_mono", palette[0][5])
        setColor("themed_icon_color", palette[0][5])

        if (pitchBlackTheme) {
            val iconBgColor = if (isSemiTransparent) {
                Color.BLACK.applyAlphaToColor(80)
            } else {
                Color.BLACK
            }
            val folderColor = if (isSemiTransparent) {
                Color.BLACK.applyAlphaToColor(80)
            } else {
                Color.BLACK
            }
            val appDrawerColor = if (isSemiTransparent) {
                Color.BLACK.applyAlphaToColor(95)
            } else {
                Color.BLACK
            }

            setColor("themed_icon_background_color", iconBgColor)
            setColor("material_color_surface_container_low", iconBgColor)
            setColor("system_surface_container_low_dark", iconBgColor)
            setColor("material_color_surface_bright", palette[3][11])
            setColor("material_color_surface_dim", appDrawerColor)
            setColor("folder_preview_dark", folderColor)
            setColor("folder_background_dark", folderColor)
        } else {
            val iconBgColor = if (isSemiTransparent) {
                (if (darkerIcon) palette[1][11] else palette[1][10]).applyAlphaToColor(80)
            } else {
                (if (darkerIcon) palette[1][11] else palette[1][10])
            }
            val folderColor = if (isSemiTransparent) {
                palette[3][11].applyAlphaToColor(80)
            } else {
                palette[3][11]
            }
            val appDrawerColor = if (isSemiTransparent) {
                palette[3][11].applyAlphaToColor(95)
            } else {
                palette[3][11]
            }

            setColor("themed_icon_background_color", iconBgColor)
            setColor("material_color_surface_container_low", iconBgColor)
            setColor("system_surface_container_low_dark", iconBgColor)
            setColor("material_color_surface_bright", palette[3][10])
            setColor("material_color_surface_dim", appDrawerColor)
            setColor("folder_preview_dark", folderColor)
            setColor("folder_background_dark", folderColor)
        }
    } else {
        val iconBgColor = if (isSemiTransparent) {
            palette[0][3].applyAlphaToColor(80)
        } else {
            palette[0][3]
        }
        val folderPreviewColor = if (isSemiTransparent) {
            palette[1][4].applyAlphaToColor(80)
        } else {
            palette[1][4]
        }
        val folderBackgroundColor = if (isSemiTransparent) {
            palette[3][1].applyAlphaToColor(80)
        } else {
            palette[3][1]
        }
        val appDrawerColor = if (isSemiTransparent) {
            palette[4][2].applyAlphaToColor(95)
        } else {
            palette[4][2]
        }

        setColor("qsb_icon_tint_quaternary_mono", palette[0][9])
        setColor("themed_icon_color", palette[0][9])
        setColor("themed_icon_background_color", iconBgColor)
        setColor("material_color_surface_container_low", iconBgColor)
        setColor("system_surface_container_low_light", iconBgColor)
        setColor("material_color_surface_bright", palette[3][1])
        setColor("material_color_surface_dim", appDrawerColor)
        setColor("folder_preview_light", folderPreviewColor)
        setColor("folder_background_light", folderBackgroundColor)
    }
}
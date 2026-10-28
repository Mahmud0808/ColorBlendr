package com.drdisagree.colorblendr.theme.overlay.writer

import android.graphics.Color
import android.os.Build
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.data.common.Constant.DOLBY_ATMOS
import com.drdisagree.colorblendr.data.common.Constant.GOOGLE_FEEDS
import com.drdisagree.colorblendr.data.common.Constant.GOOGLE_NEWS
import com.drdisagree.colorblendr.data.common.Constant.LINEAGE_PARTS
import com.drdisagree.colorblendr.data.common.Constant.PIXEL_LAUNCHER
import com.drdisagree.colorblendr.data.common.Constant.PLAY_GAMES
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_LINEAGEOS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH_AOSP
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_CLOCKS
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER_GOOGLE
import com.drdisagree.colorblendr.data.common.Utilities.darkerLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.semiTransparentLauncherIconsEnabled
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.PaletteRole.NEUTRAL
import com.drdisagree.colorblendr.theme.model.PaletteRole.NEUTRAL_VARIANT
import com.drdisagree.colorblendr.theme.model.PaletteRole.PRIMARY
import com.drdisagree.colorblendr.theme.model.PaletteRole.SECONDARY
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.token.Token
import kotlin.math.roundToInt

object PerPackageColorWriter : ThemeColorWriter {

    private const val TRANSLUCENT_ICON_ALPHA = 80
    private const val TRANSLUCENT_DRAWER_ALPHA = 95

    private val SETTINGS_CONTAINER_ALIASES = listOf(
        "materialColorSurfaceContainer",
        "sc_materialColorSurfaceContainer",
        "m3_sys_color_dynamic_dark_surface_container",
        "sud_system_surface_container_dark"
    )

    private val SETTINGS_SEARCH_DARK_SURFACES = listOf(
        "surface_container_lowest",
        "surface_container_low",
        "surface_container",
        "surface_container_high",
        "surface_container_highest"
    )

    private val PRESET_PACKAGES = setOf(
        GOOGLE_FEEDS,
        GOOGLE_NEWS,
        PLAY_GAMES,
        SETTINGS,
        SETTINGS_LINEAGEOS,
        LINEAGE_PARTS,
        DOLBY_ATMOS,
        SETTINGS_SEARCH,
        SETTINGS_SEARCH_AOSP,
        PIXEL_LAUNCHER,
        THEME_PICKER,
        THEME_PICKER_GOOGLE
    )

    fun handles(packageName: String): Boolean =
        packageName.startsWith(SYSTEMUI_CLOCKS) || packageName in PRESET_PACKAGES

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val pitchBlack = theme.adjustments.pitchBlack
        val isDark = target.isDarkMode

        when {
            target.packageName.startsWith(SYSTEMUI_CLOCKS) -> sink.clockColors(theme, isDark)
            else -> when (target.packageName) {
                GOOGLE_FEEDS -> if (pitchBlack) {
                    sink.night("gm3_ref_palette_dynamic_neutral_variant20", Color.BLACK, ifExists = false)
                }

                GOOGLE_NEWS -> sink.googleNewsColors(theme, pitchBlack)
                PLAY_GAMES -> sink.playGamesColors(theme, pitchBlack, isDark)

                SETTINGS,
                SETTINGS_LINEAGEOS,
                LINEAGE_PARTS,
                DOLBY_ATMOS -> if (pitchBlack && isVanillaIceCreamOrLater()) {
                    sink.settingsColors(theme.dark, theme.adjustments.pitchBlackContainers)
                }

                SETTINGS_SEARCH,
                SETTINGS_SEARCH_AOSP -> if (pitchBlack) sink.settingsSearchColors(theme.dark)

                PIXEL_LAUNCHER -> sink.pixelLauncherColors(theme, pitchBlack, isDark)

                THEME_PICKER,
                THEME_PICKER_GOOGLE -> if (pitchBlack && isVanillaIceCreamOrLater()) {
                    listOf(
                        "m3_sys_color_dynamic_dark_surface_container",
                        "settingslib_materialColorSurfaceContainer",
                        "system_surface_container"
                    ).forEach { sink.night(it, Color.BLACK, ifExists = false) }
                }
            }
        }
    }

    private fun ColorSink.clockColors(theme: ResolvedTheme, isDark: Boolean) {
        PaletteRole.entries.forEach { role ->
            ShadeScale.indices.forEach { index ->
                dayNight(
                    role.shadeName(index),
                    theme.light.tone(role, index),
                    theme.dark.tone(role, index),
                    isDark,
                    ifExists = false
                )
            }
        }
    }

    private fun ColorSink.googleNewsColors(theme: ResolvedTheme, pitchBlack: Boolean) {
        val light = theme.light
        val dark = theme.dark
        color("cluster_divider_bg", Color.TRANSPARENT, ifExists = false)
        color("cluster_divider_border", Color.TRANSPARENT, ifExists = false)

        color("appwidget_background_day", light.tone(NEUTRAL, 2), ifExists = false)
        color("home_background_day", light.tone(NEUTRAL, 2), ifExists = false)
        color("google_default_color_background", light.tone(NEUTRAL, 2), ifExists = false)
        color("gm3_system_bar_color_day", light.tone(NEUTRAL_VARIANT, 3), ifExists = false)
        color("google_default_color_on_background", light.tone(NEUTRAL, 11), ifExists = false)
        color("google_dark_default_color_on_background", dark.tone(NEUTRAL, 1), ifExists = false)
        color("gm3_system_bar_color_night", dark.tone(NEUTRAL_VARIANT, 10), ifExists = false)

        val nightBackground = if (pitchBlack) Color.BLACK else dark.tone(NEUTRAL, 11)
        listOf(
            "appwidget_background_night",
            "home_background_night",
            "google_dark_default_color_background"
        ).forEach { color(it, nightBackground, ifExists = false) }
    }

    private fun ColorSink.playGamesColors(theme: ResolvedTheme, pitchBlack: Boolean, isDark: Boolean) {
        fun shade(name: String, role: PaletteRole, index: Int) =
            dayNight(name, theme.light.tone(role, index), theme.dark.tone(role, index), isDark, ifExists = false)

        shade("google_white", NEUTRAL, 2)
        shade("gm_ref_palette_grey300", NEUTRAL_VARIANT, 4)
        shade("gm_ref_palette_grey700", NEUTRAL, 11)
        shade("replay__pal_games_light_600", PRIMARY, 8)
        shade("gm_ref_palette_grey600", NEUTRAL_VARIANT, 8)
        shade("gm_ref_palette_grey500", NEUTRAL, 1)
        shade("replay__pal_games_dark_300", PRIMARY, 5)

        if (pitchBlack) {
            color("gm_ref_palette_grey900", Color.BLACK, ifExists = false)
        } else {
            shade("gm_ref_palette_grey900", NEUTRAL, 11)
        }
    }

    private fun ColorSink.settingsColors(scheme: ResolvedScheme, blackContainers: Boolean) {
        listOf(
            "settingslib_materialColorSurface",
            "settingslib_materialColorSurfaceVariant",
            "settingslib_materialColorSurfaceDim",
            "settingslib_materialColorSurfaceContainerLowest",
            "settingslib_materialColorSurfaceContainerLow",
            "settingslib_materialColorSurfaceContainer",
            "config_tab_color"
        ).forEach { night(it, Color.BLACK, ifExists = false) }

        val containerHigh = scheme[Token.SURFACE_CONTAINER_HIGH]
        night("settingslib_materialColorSurfaceBright", containerHigh, ifExists = false)
        night("settingslib_materialColorSurfaceContainerHigh", containerHigh, ifExists = false)
        night("settingslib_materialColorSurfaceContainerHighest", scheme[Token.SURFACE_CONTAINER_HIGHEST], ifExists = false)

        if (blackContainers) SETTINGS_CONTAINER_ALIASES.forEach { night(it, Color.BLACK) }
    }

    private fun ColorSink.settingsSearchColors(scheme: ResolvedScheme) {
        if (isVanillaIceCreamOrLater()) {
            night("search_bar_background", scheme[NEUTRAL].colorAt(11.6), ifExists = false)
        }

        SETTINGS_SEARCH_DARK_SURFACES.forEach { token ->
            listOf(
                "m3_sys_color_dark_",
                "gm3_sys_color_dark_",
                "m3_sys_color_dynamic_dark_",
                "gm3_sys_color_dynamic_dark_"
            ).forEach { prefix -> color("$prefix$token", Color.BLACK, ifExists = false) }
        }
    }

    private fun ColorSink.pixelLauncherColors(theme: ResolvedTheme, pitchBlack: Boolean, isDark: Boolean) {
        val translucent = semiTransparentLauncherIconsEnabled()
        fun Int.icon() = if (translucent) withAlpha(TRANSLUCENT_ICON_ALPHA) else this
        fun Int.drawer() = if (translucent) withAlpha(TRANSLUCENT_DRAWER_ALPHA) else this
        fun both(name: String, light: Int, dark: Int) = dayNight(name, light, dark, isDark, ifExists = false)

        val light = theme.light
        val dark = theme.dark

        val lightIconTint = light.tone(PRIMARY, 9)
        val lightIconBackground = light.tone(PRIMARY, 3).icon()
        val darkIconTint = dark.tone(PRIMARY, 5)
        val darkIconBackground: Int
        val darkFolder: Int
        val darkDrawer: Int
        val darkSurfaceBright: Int
        if (pitchBlack) {
            darkIconBackground = Color.BLACK.icon()
            darkFolder = Color.BLACK.icon()
            darkDrawer = Color.BLACK.drawer()
            darkSurfaceBright = dark.tone(NEUTRAL, 11)
        } else {
            val iconShade = if (darkerLauncherIconsEnabled()) 11 else 10
            darkIconBackground = dark.tone(SECONDARY, iconShade).icon()
            darkFolder = dark.tone(NEUTRAL, 11).icon()
            darkDrawer = dark.tone(NEUTRAL, 11).drawer()
            darkSurfaceBright = dark.tone(NEUTRAL, 10)
        }

        both("qsb_icon_tint_quaternary_mono", lightIconTint, darkIconTint)
        both("themed_icon_color", lightIconTint, darkIconTint)
        both("themed_icon_background_color", lightIconBackground, darkIconBackground)
        both("material_color_surface_container_low", lightIconBackground, darkIconBackground)
        both("material_color_surface_bright", light.tone(NEUTRAL, 1), darkSurfaceBright)
        both("material_color_surface_dim", light.tone(NEUTRAL_VARIANT, 2).drawer(), darkDrawer)

        color("system_surface_container_low_light", lightIconBackground, ifExists = false)
        color("system_surface_container_low_dark", darkIconBackground, ifExists = false)
        color("folder_preview_light", light.tone(SECONDARY, 4).icon(), ifExists = false)
        color("folder_background_light", light.tone(NEUTRAL, 1).icon(), ifExists = false)
        color("folder_preview_dark", darkFolder, ifExists = false)
        color("folder_background_dark", darkFolder, ifExists = false)
    }

    private fun ResolvedScheme.tone(role: PaletteRole, index: Int): Int = get(role).shades[index]

    private fun Int.withAlpha(percentage: Int): Int =
        ColorUtils.setAlphaComponent(this, (percentage * 255 / 100f).roundToInt())

    private fun isVanillaIceCreamOrLater() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}

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
        val scheme = theme.scheme(target.isDarkMode)
        val pitchBlack = theme.adjustments.pitchBlack
        val isDark = target.isDarkMode

        when {
            target.packageName.startsWith(SYSTEMUI_CLOCKS) -> sink.clockColors(scheme)
            else -> when (target.packageName) {
                GOOGLE_FEEDS -> if (pitchBlack && isDark) {
                    sink.color("gm3_ref_palette_dynamic_neutral_variant20", Color.BLACK, ifExists = false)
                }

                GOOGLE_NEWS -> sink.googleNewsColors(scheme, pitchBlack)
                PLAY_GAMES -> sink.playGamesColors(scheme, pitchBlack)

                SETTINGS,
                SETTINGS_LINEAGEOS,
                LINEAGE_PARTS,
                DOLBY_ATMOS -> if (pitchBlack && isDark && isVanillaIceCreamOrLater()) {
                    sink.settingsColors(scheme, theme.adjustments.pitchBlackContainers)
                }

                SETTINGS_SEARCH,
                SETTINGS_SEARCH_AOSP -> if (pitchBlack) sink.settingsSearchColors(scheme, isDark)

                PIXEL_LAUNCHER -> sink.pixelLauncherColors(scheme, pitchBlack, isDark)

                THEME_PICKER,
                THEME_PICKER_GOOGLE -> if (pitchBlack && isDark && isVanillaIceCreamOrLater()) {
                    listOf(
                        "m3_sys_color_dynamic_dark_surface_container",
                        "settingslib_materialColorSurfaceContainer",
                        "system_surface_container"
                    ).forEach { sink.color(it, Color.BLACK, ifExists = false) }
                }
            }
        }
    }

    private fun ColorSink.clockColors(scheme: ResolvedScheme) {
        PaletteRole.entries.forEach { role ->
            ShadeScale.indices.forEach { index ->
                color(role.shadeName(index), scheme.tone(role, index), ifExists = false)
            }
        }
    }

    private fun ColorSink.googleNewsColors(scheme: ResolvedScheme, pitchBlack: Boolean) {
        color("cluster_divider_bg", Color.TRANSPARENT, ifExists = false)
        color("cluster_divider_border", Color.TRANSPARENT, ifExists = false)

        color("appwidget_background_day", scheme.tone(NEUTRAL, 2), ifExists = false)
        color("home_background_day", scheme.tone(NEUTRAL, 2), ifExists = false)
        color("google_default_color_background", scheme.tone(NEUTRAL, 2), ifExists = false)
        color("gm3_system_bar_color_day", scheme.tone(NEUTRAL_VARIANT, 3), ifExists = false)
        color("google_default_color_on_background", scheme.tone(NEUTRAL, 11), ifExists = false)
        color("google_dark_default_color_on_background", scheme.tone(NEUTRAL, 1), ifExists = false)
        color("gm3_system_bar_color_night", scheme.tone(NEUTRAL_VARIANT, 10), ifExists = false)

        val nightBackground = if (pitchBlack) Color.BLACK else scheme.tone(NEUTRAL, 11)
        listOf(
            "appwidget_background_night",
            "home_background_night",
            "google_dark_default_color_background"
        ).forEach { color(it, nightBackground, ifExists = false) }
    }

    private fun ColorSink.playGamesColors(scheme: ResolvedScheme, pitchBlack: Boolean) {
        color("google_white", scheme.tone(NEUTRAL, 2), ifExists = false)
        color("gm_ref_palette_grey300", scheme.tone(NEUTRAL_VARIANT, 4), ifExists = false)
        color("gm_ref_palette_grey700", scheme.tone(NEUTRAL, 11), ifExists = false)
        color("replay__pal_games_light_600", scheme.tone(PRIMARY, 8), ifExists = false)

        color(
            "gm_ref_palette_grey900",
            if (pitchBlack) Color.BLACK else scheme.tone(NEUTRAL, 11),
            ifExists = false
        )
        color("gm_ref_palette_grey600", scheme.tone(NEUTRAL_VARIANT, 8), ifExists = false)
        color("gm_ref_palette_grey500", scheme.tone(NEUTRAL, 1), ifExists = false)
        color("replay__pal_games_dark_300", scheme.tone(PRIMARY, 5), ifExists = false)
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
        ).forEach { color(it, Color.BLACK, ifExists = false) }

        val containerHigh = scheme[Token.SURFACE_CONTAINER_HIGH]
        color("settingslib_materialColorSurfaceBright", containerHigh, ifExists = false)
        color("settingslib_materialColorSurfaceContainerHigh", containerHigh, ifExists = false)
        color("settingslib_materialColorSurfaceContainerHighest", scheme[Token.SURFACE_CONTAINER_HIGHEST], ifExists = false)

        if (blackContainers) SETTINGS_CONTAINER_ALIASES.forEach { color(it, Color.BLACK) }
    }

    private fun ColorSink.settingsSearchColors(scheme: ResolvedScheme, isDark: Boolean) {
        if (isDark && isVanillaIceCreamOrLater()) {
            color("search_bar_background", scheme[NEUTRAL].colorAt(11.6), ifExists = false)
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

    private fun ColorSink.pixelLauncherColors(scheme: ResolvedScheme, pitchBlack: Boolean, isDark: Boolean) {
        val translucent = semiTransparentLauncherIconsEnabled()
        fun Int.icon() = if (translucent) withAlpha(TRANSLUCENT_ICON_ALPHA) else this
        fun Int.drawer() = if (translucent) withAlpha(TRANSLUCENT_DRAWER_ALPHA) else this

        if (isDark) {
            val iconTint = scheme.tone(PRIMARY, 5)
            color("qsb_icon_tint_quaternary_mono", iconTint, ifExists = false)
            color("themed_icon_color", iconTint, ifExists = false)

            val iconBackground: Int
            val folder: Int
            val drawer: Int
            val surfaceBright: Int
            if (pitchBlack) {
                iconBackground = Color.BLACK.icon()
                folder = Color.BLACK.icon()
                drawer = Color.BLACK.drawer()
                surfaceBright = scheme.tone(NEUTRAL, 11)
            } else {
                val iconShade = if (darkerLauncherIconsEnabled()) 11 else 10
                iconBackground = scheme.tone(SECONDARY, iconShade).icon()
                folder = scheme.tone(NEUTRAL, 11).icon()
                drawer = scheme.tone(NEUTRAL, 11).drawer()
                surfaceBright = scheme.tone(NEUTRAL, 10)
            }

            color("themed_icon_background_color", iconBackground, ifExists = false)
            color("material_color_surface_container_low", iconBackground, ifExists = false)
            color("system_surface_container_low_dark", iconBackground, ifExists = false)
            color("material_color_surface_bright", surfaceBright, ifExists = false)
            color("material_color_surface_dim", drawer, ifExists = false)
            color("folder_preview_dark", folder, ifExists = false)
            color("folder_background_dark", folder, ifExists = false)
        } else {
            val iconTint = scheme.tone(PRIMARY, 9)
            val iconBackground = scheme.tone(PRIMARY, 3).icon()

            color("qsb_icon_tint_quaternary_mono", iconTint, ifExists = false)
            color("themed_icon_color", iconTint, ifExists = false)
            color("themed_icon_background_color", iconBackground, ifExists = false)
            color("material_color_surface_container_low", iconBackground, ifExists = false)
            color("system_surface_container_low_light", iconBackground, ifExists = false)
            color("material_color_surface_bright", scheme.tone(NEUTRAL, 1), ifExists = false)
            color("material_color_surface_dim", scheme.tone(NEUTRAL_VARIANT, 2).drawer(), ifExists = false)
            color("folder_preview_light", scheme.tone(SECONDARY, 4).icon(), ifExists = false)
            color("folder_background_light", scheme.tone(NEUTRAL, 1).icon(), ifExists = false)
        }
    }

    private fun ResolvedScheme.tone(role: PaletteRole, index: Int): Int = get(role).shades[index]

    private fun Int.withAlpha(percentage: Int): Int =
        ColorUtils.setAlphaComponent(this, (percentage * 255 / 100f).roundToInt())

    private fun isVanillaIceCreamOrLater() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}

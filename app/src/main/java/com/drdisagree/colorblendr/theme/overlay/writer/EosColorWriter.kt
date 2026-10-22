package com.drdisagree.colorblendr.theme.overlay.writer

import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_PACKAGE
import com.drdisagree.colorblendr.theme.model.PaletteRole.NEUTRAL
import com.drdisagree.colorblendr.theme.model.PaletteRole.PRIMARY
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object EosColorWriter : ThemeColorWriter {

    private const val ALPHA_ACCENT = 0x14

    private class LightDark(val light: Int, val dark: Int) {
        fun current(isDark: Boolean) = if (isDark) dark else light
    }

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val isDark = target.isDarkMode
        fun ColorSink.qualified(name: String, colors: LightDark) {
            dayNight(name, colors.current(isDark), colors.dark)
            color("${name}_dark", colors.dark)
            color("${name}_light", colors.light)
        }

        val accent = LightDark(theme.light[PRIMARY].shades[8], theme.dark[PRIMARY].shades[4])
        val tileInactive = LightDark(theme.light[NEUTRAL].shades[3], theme.dark[NEUTRAL].shades[10])
        val surface = LightDark(theme.light[NEUTRAL].shades[2], theme.dark[NEUTRAL].shades[12])
        val onSurface = LightDark(theme.light[NEUTRAL].shades[11], theme.dark[NEUTRAL].shades[2])
        val onSurfaceVariant = LightDark(theme.light[NEUTRAL].shades[10], theme.dark[NEUTRAL].shades[3])
        val surfaceContainer = LightDark(theme.light[NEUTRAL].shades[3], theme.dark[NEUTRAL].shades[11])
        val surfaceContainerHigh = LightDark(theme.light[NEUTRAL].shades[4], theme.dark[NEUTRAL].shades[10])

        sink.color("accent_device_default_light", accent.light)
        sink.color("accent_device_default_dark", accent.dark)
        sink.dayNight("accent_device_default", accent.current(isDark), accent.dark)
        sink.qualified("e_qs_tile_inactive", tileInactive)

        listOf("e_background", "e_action_bar", "e_drawer_background", "e_qs_background").forEach {
            sink.qualified(it, surface)
        }
        sink.qualified("e_background_variant", surfaceContainer)
        sink.qualified("e_notification_background", surfaceContainerHigh)
        sink.qualified("e_primary_text_color", onSurface)
        sink.qualified("e_secondary_text_color", onSurfaceVariant)
        sink.qualified("e_secondary_text_color_variant", onSurfaceVariant)

        sink.qualified("e_accent", accent)
        sink.qualified("e_accent_inverse", LightDark(accent.dark, accent.light))
        sink.qualified(
            "e_alpha_accent",
            LightDark(
                ColorUtils.setAlphaComponent(accent.light, ALPHA_ACCENT),
                ColorUtils.setAlphaComponent(accent.dark, ALPHA_ACCENT)
            )
        )

        if (target.packageName == SYSTEMUI_PACKAGE) {
            sink.color("brightness_slider_overlay_color", accent.current(isDark))
            sink.color("brightness_slider_track", surfaceContainer.current(isDark))
        }
    }
}

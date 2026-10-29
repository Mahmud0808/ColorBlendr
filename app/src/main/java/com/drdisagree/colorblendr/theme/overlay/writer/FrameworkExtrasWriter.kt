package com.drdisagree.colorblendr.theme.overlay.writer

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object FrameworkExtrasWriter : ThemeColorWriter {

    private const val BLURRED_PANEL_ALPHA = 0.32f
    private const val SECONDARY_TEXT_DARK = -0x4c000001
    private const val SECONDARY_TEXT_LIGHT = -0x4d000000

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        if (theme.adjustments.pitchBlack) {
            sink.color("background_dark", Color.BLACK, ifExists = false)
            sink.color("surface_header_dark_sysui", Color.BLACK)
            if (target.isDarkMode) {
                sink.color(
                    "shade_panel_fg_color",
                    ColorUtils.setAlphaComponent(Color.BLACK, (BLURRED_PANEL_ALPHA * 255).toInt()),
                    ifExists = false
                )
            }
        }

        if (!theme.adjustments.tintedText) {
            sink.color("text_color_primary_device_default_dark", Color.WHITE, ifExists = false)
            sink.color("text_color_secondary_device_default_dark", SECONDARY_TEXT_DARK, ifExists = false)
            sink.color("text_color_primary_device_default_light", Color.BLACK, ifExists = false)
            sink.color("text_color_secondary_device_default_light", SECONDARY_TEXT_LIGHT, ifExists = false)
        }
    }
}

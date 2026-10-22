package com.drdisagree.colorblendr.theme.overlay.writer

import android.graphics.Color
import android.os.Build
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object TintlessTextWriter : ThemeColorWriter {

    private const val SECONDARY_TEXT_DARK = -0x4c000001
    private const val SECONDARY_TEXT_LIGHT = -0x4d000000

    private val TEXT_COLORS = mapOf(
        "m3_ref_palette_dynamic_neutral90" to Color.WHITE,
        "m3_ref_palette_dynamic_neutral95" to Color.WHITE,
        "m3_ref_palette_dynamic_neutral_variant70" to -0x333334,
        "m3_ref_palette_dynamic_neutral_variant80" to Color.WHITE,
        "text_color_primary_dark" to Color.WHITE,
        "text_color_secondary_dark" to SECONDARY_TEXT_DARK,
        "text_color_tertiary_dark" to -0x7f000001,
        "google_dark_default_color_on_background" to Color.WHITE,
        "gm_ref_palette_grey500" to Color.WHITE,
        "m3_ref_palette_dynamic_neutral10" to Color.BLACK,
        "m3_ref_palette_dynamic_neutral_variant30" to SECONDARY_TEXT_LIGHT,
        "text_color_primary_light" to Color.BLACK,
        "text_color_secondary_light" to SECONDARY_TEXT_LIGHT,
        "text_color_tertiary_light" to -0x80000000,
        "google_default_color_on_background" to Color.BLACK,
        "gm_ref_palette_grey700" to Color.BLACK
    )

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        if (theme.adjustments.tintedText) return

        TEXT_COLORS.forEach { (name, color) ->
            sink.color(name, color)
            if (name.startsWith("m3")) sink.color("g$name", color)
        }

        if (target.packageName == SETTINGS) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                sink.night("settingslib_text_color_primary_device_default", Color.WHITE)
                sink.night("settingslib_text_color_secondary_device_default", SECONDARY_TEXT_DARK)
            } else {
                sink.night("settingslib_materialColorOnSurface", Color.WHITE)
                sink.night("settingslib_materialColorOnSurfaceVariant", SECONDARY_TEXT_DARK)
            }
        }
    }
}

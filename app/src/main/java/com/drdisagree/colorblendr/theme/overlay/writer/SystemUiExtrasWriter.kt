package com.drdisagree.colorblendr.theme.overlay.writer

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object SystemUiExtrasWriter : ThemeColorWriter {

    private const val BLURRED_PANEL_ALPHA = 0.32f
    private const val BLURRED_SCRIM_ALPHA = 0.5f

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        sink.color("shade_scrim_background_dark", Color.BLACK)
        sink.color("notification_ripple_tinted_color", Color.WHITE)
        sink.color("system_bar_background_opaque", Color.BLACK)

        if (target.isDarkMode && theme.adjustments.pitchBlack) {
            sink.color("shade_panel_base", Color.BLACK.withAlpha(BLURRED_PANEL_ALPHA))
            sink.color("shade_panel_fallback", Color.BLACK)
            sink.color("notification_scrim_base", Color.BLACK.withAlpha(BLURRED_SCRIM_ALPHA))
            sink.color("notification_scrim_fallback", Color.BLACK)
        }
    }

    private fun Int.withAlpha(alpha: Float): Int =
        ColorUtils.setAlphaComponent(this, (alpha * 255).toInt())
}

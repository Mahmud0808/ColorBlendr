package com.drdisagree.colorblendr.theme.overlay.writer

import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import kotlin.math.roundToInt

object SurfaceEffectWriter : ThemeColorWriter {

    private class Effect(
        val role: PaletteRole,
        val lightTone: Double,
        val darkTone: Double,
        val lightAlpha: Double = 1.0,
        val darkAlpha: Double = 1.0
    ) {
        fun color(scheme: ResolvedScheme): Int {
            val tone = if (scheme.isDark) darkTone else lightTone
            val alpha = if (scheme.isDark) darkAlpha else lightAlpha
            return ColorUtils.setAlphaComponent(scheme[role].colorAt(tone), (alpha * 255).roundToInt())
        }
    }

    private val EFFECTS = listOf(
        Effect(PaletteRole.PRIMARY, lightTone = 90.0, darkTone = 20.0, lightAlpha = 0.50, darkAlpha = 0.50),
        Effect(PaletteRole.NEUTRAL, lightTone = 98.0, darkTone = 6.0, lightAlpha = 0.54, darkAlpha = 0.54),
        Effect(PaletteRole.PRIMARY, lightTone = 100.0, darkTone = 90.0, lightAlpha = 0.32, darkAlpha = 0.15),
        Effect(PaletteRole.PRIMARY, lightTone = 40.0, darkTone = 90.0, lightAlpha = 0.15, darkAlpha = 0.10),
        Effect(PaletteRole.PRIMARY, lightTone = 16.0, darkTone = 85.0, lightAlpha = 0.70, darkAlpha = 0.85)
    )

    private val FALLBACK = Effect(PaletteRole.SECONDARY, lightTone = 80.0, darkTone = 20.0)

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        sink.color("system_surface_effect_0_fallback_light", FALLBACK.color(theme.light))
        sink.color("system_surface_effect_0_fallback_dark", FALLBACK.color(theme.dark))

        EFFECTS.forEachIndexed { index, effect ->
            val light = effect.color(theme.light)
            val dark = effect.color(theme.dark)
            sink.color("surface_effect_${index}_color", if (target.isDarkMode) dark else light)
            sink.color("system_surface_effect_${index}_light", light)
            sink.color("system_surface_effect_${index}_dark", dark)
        }
    }
}

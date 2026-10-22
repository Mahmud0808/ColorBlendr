package com.drdisagree.colorblendr.theme.overlay.writer

import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object ReferencePaletteWriter : ThemeColorWriter {

    private val PREFIXES = listOf(
        "m3_ref_palette_",
        "m3_ref_palette_dynamic_",
        "gm3_ref_palette_",
        "gm3_ref_palette_dynamic_"
    )

    private val TONES = listOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 98, 99, 100)
    private val NEUTRAL_SURFACE_TONES = listOf(4, 6, 12, 17, 22, 24, 87, 92, 94, 96)

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val current = theme.scheme(target.isDarkMode)

        PaletteRole.entries.forEach { role ->
            val tones = if (role.isNeutral) TONES + NEUTRAL_SURFACE_TONES else TONES
            tones.forEach { tone ->
                val currentColor = current.reference(role, tone.toDouble())
                val darkColor = theme.dark.reference(role, tone.toDouble())
                PREFIXES.forEach { prefix ->
                    sink.dayNight("$prefix${role.referenceName}$tone", currentColor, darkColor)
                }
            }
        }
    }
}

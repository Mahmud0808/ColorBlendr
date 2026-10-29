package com.drdisagree.colorblendr.theme.overlay.writer

import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object SystemPaletteWriter : ThemeColorWriter {

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val current = theme.scheme(target.isDarkMode)

        PaletteRole.entries.forEach { role ->
            ShadeScale.indices.forEach { index ->
                val name = role.shadeName(index)
                sink.color(name, current[role, index], ifExists = false)
                sink.color("${name}_light", theme.light[role, index])
                sink.color("${name}_dark", theme.dark[role, index])
            }
        }
    }
}

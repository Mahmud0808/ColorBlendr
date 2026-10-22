package com.drdisagree.colorblendr.theme.overlay.writer

import android.os.Build
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.token.TokenGroup

object SystemTokenWriter : ThemeColorWriter {

    private const val PREFIX = "system_"

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val groups = buildList {
            add(TokenGroup.SYSTEM)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) add(TokenGroup.CUSTOM)
        }

        groups.flatMap { theme.light.tokens(it) }.forEach { token ->
            sink.color("$PREFIX${token.resourceName}_light", theme.light[token])
            sink.color("$PREFIX${token.resourceName}_dark", theme.dark[token])
        }

        val current = theme.scheme(target.isDarkMode)
        theme.light.tokens(TokenGroup.FIXED).forEach { token ->
            sink.dayNight("$PREFIX${token.resourceName}", current[token], theme.dark[token])
            sink.color("$PREFIX${token.resourceName}_light", theme.light[token])
            sink.color("$PREFIX${token.resourceName}_dark", theme.dark[token])
        }
    }
}

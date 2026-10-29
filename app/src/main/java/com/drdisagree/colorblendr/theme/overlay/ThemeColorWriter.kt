package com.drdisagree.colorblendr.theme.overlay

import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

fun interface ThemeColorWriter {
    fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink)
}

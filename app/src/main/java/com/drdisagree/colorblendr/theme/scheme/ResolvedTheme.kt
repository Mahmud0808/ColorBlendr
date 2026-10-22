package com.drdisagree.colorblendr.theme.scheme

import com.drdisagree.colorblendr.theme.model.ThemeAdjustments

class ResolvedTheme(
    val light: ResolvedScheme,
    val dark: ResolvedScheme,
    val adjustments: ThemeAdjustments
) {
    fun scheme(isDark: Boolean): ResolvedScheme = if (isDark) dark else light
}

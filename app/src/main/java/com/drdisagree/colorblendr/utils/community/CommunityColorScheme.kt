package com.drdisagree.colorblendr.utils.community

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.drdisagree.colorblendr.data.models.CommunityTheme

// ColorScheme built from a theme's derived palette so the details screen can
// render fully in the theme's colors without touching prefs or resources.
// Rows: 0 accent1, 1 accent2, 2 accent3, 3 neutral1, 4 neutral2, 5 error.
// Shade indices map tones 100..0 as [0]=100 .. [12]=0.
fun communityColorScheme(
    theme: CommunityTheme,
    isDark: Boolean,
    base: ColorScheme
): ColorScheme {
    val palette = CommunityThemePalette.derive(theme, isDark)

    fun shade(row: Int, dark: Int, light: Int): Color =
        Color(palette[row][if (isDark) dark else light])

    return base.copy(
        primary = shade(0, 4, 8),
        onPrimary = shade(0, 10, 0),
        primaryContainer = shade(0, 9, 3),
        onPrimaryContainer = shade(0, 3, 11),
        inversePrimary = shade(0, 8, 4),
        secondary = shade(1, 4, 8),
        onSecondary = shade(1, 10, 0),
        secondaryContainer = shade(1, 9, 3),
        onSecondaryContainer = shade(1, 3, 11),
        tertiary = shade(2, 4, 8),
        onTertiary = shade(2, 10, 0),
        tertiaryContainer = shade(2, 9, 3),
        onTertiaryContainer = shade(2, 3, 11),
        background = shade(3, 12, 1),
        onBackground = shade(3, 3, 11),
        surface = shade(3, 12, 1),
        onSurface = shade(3, 3, 11),
        surfaceVariant = shade(4, 9, 3),
        onSurfaceVariant = shade(4, 4, 9),
        surfaceTint = shade(0, 4, 8),
        inverseSurface = shade(3, 3, 10),
        inverseOnSurface = shade(3, 10, 2),
        error = shade(5, 4, 8),
        onError = shade(5, 10, 0),
        errorContainer = shade(5, 9, 3),
        onErrorContainer = shade(5, 3, 11),
        outline = shade(4, 6, 7),
        outlineVariant = shade(4, 9, 4),
        surfaceBright = shade(3, 10, 1),
        surfaceDim = shade(3, 12, 3),
        surfaceContainerLowest = shade(3, 12, 0),
        surfaceContainerLow = shade(3, 11, 1),
        surfaceContainer = shade(3, 11, 2),
        surfaceContainerHigh = shade(3, 10, 2),
        surfaceContainerHighest = shade(3, 10, 3)
    )
}

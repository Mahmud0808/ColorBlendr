package com.drdisagree.colorblendr.utils.community

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.utils.colors.DynamicColors
import com.drdisagree.colorblendr.utils.colors.adjustColorBrightnessIfRequired
import com.drdisagree.colorblendr.utils.colors.adjustLStarIfRequired
import com.drdisagree.colorblendr.utils.colors.extractResourceFromColorMap

// ColorScheme from theme's derived palette; pref-free. Roles resolve via
// DynamicColors system mapping = matches applied look.
// Rows: 0-2 accents, 3-4 neutrals, 5 error. Shades [0]=tone100 .. [12]=tone0.
fun communityColorScheme(
    theme: CommunityTheme,
    isDark: Boolean,
    base: ColorScheme
): ColorScheme = communityColorScheme(
    CommunityThemePalette.derive(theme, isDark),
    isDark,
    base
)

// Overload for callers that already derived the palette.
fun communityColorScheme(
    palette: ArrayList<ArrayList<Int>>,
    isDark: Boolean,
    base: ColorScheme
): ColorScheme {
    val roles = buildMap {
        DynamicColors.ALL_DYNAMIC_COLORS_MAPPED.forEach { mapping ->
            val extracted = mapping.extractResourceFromColorMap(
                palette = palette,
                isDark = isDark
            )
            put(
                extracted.first,
                mapping.adjustLStarIfRequired(
                    mapping.adjustColorBrightnessIfRequired(extracted.second, isDark),
                    isDark
                )
            )
        }
    }

    fun role(name: String): Color = Color(roles.getValue(name))

    fun shade(row: Int, dark: Int, light: Int): Color =
        Color(palette[row][if (isDark) dark else light])

    return base.copy(
        primary = role("primary"),
        onPrimary = role("on_primary"),
        primaryContainer = role("primary_container"),
        onPrimaryContainer = role("on_primary_container"),
        inversePrimary = shade(0, 8, 4),
        secondary = role("secondary"),
        onSecondary = role("on_secondary"),
        secondaryContainer = role("secondary_container"),
        onSecondaryContainer = role("on_secondary_container"),
        tertiary = role("tertiary"),
        onTertiary = role("on_tertiary"),
        tertiaryContainer = role("tertiary_container"),
        onTertiaryContainer = role("on_tertiary_container"),
        background = role("background"),
        onBackground = role("on_background"),
        surface = role("surface"),
        onSurface = role("on_surface"),
        surfaceVariant = role("surface_variant"),
        onSurfaceVariant = role("on_surface_variant"),
        surfaceTint = role("primary"),
        inverseSurface = shade(3, 3, 10),
        inverseOnSurface = shade(3, 10, 2),
        error = shade(5, 4, 8),
        onError = shade(5, 10, 0),
        errorContainer = shade(5, 9, 3),
        onErrorContainer = shade(5, 3, 11),
        outline = role("outline"),
        outlineVariant = role("outline_variant"),
        surfaceBright = role("surface_bright"),
        surfaceDim = role("surface_dim"),
        surfaceContainerLowest = role("surface_container_lowest"),
        surfaceContainerLow = role("surface_container_low"),
        surfaceContainer = role("surface_container"),
        surfaceContainerHigh = role("surface_container_high"),
        surfaceContainerHighest = role("surface_container_highest")
    )
}
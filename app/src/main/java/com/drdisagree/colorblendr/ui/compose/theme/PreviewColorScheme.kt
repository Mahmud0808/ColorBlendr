package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.drdisagree.colorblendr.data.domain.PreviewController.PreviewColors

// Builds the theme from locally computed overlay colors while a preview is
// active; role names match the resources the fabricated overlay would set.
// Inverse roles have no overlay mapping and mirror the framework's static
// shade references instead.
fun previewColorScheme(previewColors: PreviewColors, isDark: Boolean): ColorScheme {
    val map = if (isDark) previewColors.darkMap else previewColors.lightMap
    val palette = if (isDark) previewColors.paletteDark else previewColors.paletteLight
    val suffix = if (isDark) "_dark" else "_light"

    fun role(name: String) = Color(map.getValue("system_$name$suffix"))
    fun fixed(name: String) = Color(map.getValue("system_$name"))
    fun shade(row: Int, index: Int) = Color(palette[row][index])

    val base = if (isDark) darkColorScheme() else lightColorScheme()

    return base.copy(
        primary = role("primary"),
        onPrimary = role("on_primary"),
        primaryContainer = role("primary_container"),
        onPrimaryContainer = role("on_primary_container"),
        inversePrimary = if (isDark) shade(0, 8) else shade(0, 4),
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
        inverseSurface = if (isDark) shade(3, 3) else shade(3, 10),
        inverseOnSurface = if (isDark) shade(3, 10) else shade(3, 2),
        error = role("error"),
        onError = role("on_error"),
        errorContainer = role("error_container"),
        onErrorContainer = role("on_error_container"),
        outline = role("outline"),
        outlineVariant = role("outline_variant"),
        surfaceBright = role("surface_bright"),
        surfaceDim = role("surface_dim"),
        surfaceContainer = role("surface_container"),
        surfaceContainerHigh = role("surface_container_high"),
        surfaceContainerHighest = role("surface_container_highest"),
        surfaceContainerLow = role("surface_container_low"),
        surfaceContainerLowest = role("surface_container_lowest"),
        primaryFixed = fixed("primary_fixed"),
        primaryFixedDim = fixed("primary_fixed_dim"),
        onPrimaryFixed = fixed("on_primary_fixed"),
        onPrimaryFixedVariant = fixed("on_primary_fixed_variant"),
        secondaryFixed = fixed("secondary_fixed"),
        secondaryFixedDim = fixed("secondary_fixed_dim"),
        onSecondaryFixed = fixed("on_secondary_fixed"),
        onSecondaryFixedVariant = fixed("on_secondary_fixed_variant"),
        tertiaryFixed = fixed("tertiary_fixed"),
        tertiaryFixedDim = fixed("tertiary_fixed_dim"),
        onTertiaryFixed = fixed("on_tertiary_fixed"),
        onTertiaryFixedVariant = fixed("on_tertiary_fixed_variant")
    )
}

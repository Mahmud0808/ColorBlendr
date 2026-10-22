package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.token.Token

fun ResolvedScheme.toColorScheme(
    base: ColorScheme = if (isDark) darkColorScheme() else lightColorScheme()
): ColorScheme {
    fun color(token: Token) = Color(get(token))

    return base.copy(
        primary = color(Token.PRIMARY),
        onPrimary = color(Token.ON_PRIMARY),
        primaryContainer = color(Token.PRIMARY_CONTAINER),
        onPrimaryContainer = color(Token.ON_PRIMARY_CONTAINER),
        inversePrimary = color(Token.INVERSE_PRIMARY),
        secondary = color(Token.SECONDARY),
        onSecondary = color(Token.ON_SECONDARY),
        secondaryContainer = color(Token.SECONDARY_CONTAINER),
        onSecondaryContainer = color(Token.ON_SECONDARY_CONTAINER),
        tertiary = color(Token.TERTIARY),
        onTertiary = color(Token.ON_TERTIARY),
        tertiaryContainer = color(Token.TERTIARY_CONTAINER),
        onTertiaryContainer = color(Token.ON_TERTIARY_CONTAINER),
        background = color(Token.BACKGROUND),
        onBackground = color(Token.ON_BACKGROUND),
        surface = color(Token.SURFACE),
        onSurface = color(Token.ON_SURFACE),
        surfaceVariant = color(Token.SURFACE_VARIANT),
        onSurfaceVariant = color(Token.ON_SURFACE_VARIANT),
        surfaceTint = color(Token.SURFACE_TINT),
        inverseSurface = color(Token.INVERSE_SURFACE),
        inverseOnSurface = color(Token.INVERSE_ON_SURFACE),
        error = color(Token.ERROR),
        onError = color(Token.ON_ERROR),
        errorContainer = color(Token.ERROR_CONTAINER),
        onErrorContainer = color(Token.ON_ERROR_CONTAINER),
        outline = color(Token.OUTLINE),
        outlineVariant = color(Token.OUTLINE_VARIANT),
        scrim = color(Token.SCRIM),
        surfaceBright = color(Token.SURFACE_BRIGHT),
        surfaceDim = color(Token.SURFACE_DIM),
        surfaceContainer = color(Token.SURFACE_CONTAINER),
        surfaceContainerHigh = color(Token.SURFACE_CONTAINER_HIGH),
        surfaceContainerHighest = color(Token.SURFACE_CONTAINER_HIGHEST),
        surfaceContainerLow = color(Token.SURFACE_CONTAINER_LOW),
        surfaceContainerLowest = color(Token.SURFACE_CONTAINER_LOWEST),
        primaryFixed = color(Token.PRIMARY_FIXED),
        primaryFixedDim = color(Token.PRIMARY_FIXED_DIM),
        onPrimaryFixed = color(Token.ON_PRIMARY_FIXED),
        onPrimaryFixedVariant = color(Token.ON_PRIMARY_FIXED_VARIANT),
        secondaryFixed = color(Token.SECONDARY_FIXED),
        secondaryFixedDim = color(Token.SECONDARY_FIXED_DIM),
        onSecondaryFixed = color(Token.ON_SECONDARY_FIXED),
        onSecondaryFixedVariant = color(Token.ON_SECONDARY_FIXED_VARIANT),
        tertiaryFixed = color(Token.TERTIARY_FIXED),
        tertiaryFixedDim = color(Token.TERTIARY_FIXED_DIM),
        onTertiaryFixed = color(Token.ON_TERTIARY_FIXED),
        onTertiaryFixedVariant = color(Token.ON_TERTIARY_FIXED_VARIANT)
    )
}

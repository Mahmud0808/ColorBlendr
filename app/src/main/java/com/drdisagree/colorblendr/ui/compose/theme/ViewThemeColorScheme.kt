package com.drdisagree.colorblendr.ui.compose.theme

import android.content.Context
import androidx.annotation.AttrRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR

// Resolves the color scheme from the MDC view theme instead of dynamicColorScheme()
// so Compose and remaining views read identical colors on every API level.
@Composable
fun viewThemeColorScheme(): ColorScheme {
    val context = LocalContext.current
    val base = if (isSystemInDarkTheme()) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }

    return base.copy(
        primary = context.themeColor(AppCompatR.attr.colorPrimary),
        onPrimary = context.themeColor(MaterialR.attr.colorOnPrimary),
        primaryContainer = context.themeColor(MaterialR.attr.colorPrimaryContainer),
        onPrimaryContainer = context.themeColor(MaterialR.attr.colorOnPrimaryContainer),
        inversePrimary = context.themeColor(MaterialR.attr.colorPrimaryInverse),
        secondary = context.themeColor(MaterialR.attr.colorSecondary),
        onSecondary = context.themeColor(MaterialR.attr.colorOnSecondary),
        secondaryContainer = context.themeColor(MaterialR.attr.colorSecondaryContainer),
        onSecondaryContainer = context.themeColor(MaterialR.attr.colorOnSecondaryContainer),
        tertiary = context.themeColor(MaterialR.attr.colorTertiary),
        onTertiary = context.themeColor(MaterialR.attr.colorOnTertiary),
        tertiaryContainer = context.themeColor(MaterialR.attr.colorTertiaryContainer),
        onTertiaryContainer = context.themeColor(MaterialR.attr.colorOnTertiaryContainer),
        background = context.themeColor(android.R.attr.colorBackground),
        onBackground = context.themeColor(MaterialR.attr.colorOnBackground),
        surface = context.themeColor(MaterialR.attr.colorSurface),
        onSurface = context.themeColor(MaterialR.attr.colorOnSurface),
        surfaceVariant = context.themeColor(MaterialR.attr.colorSurfaceVariant),
        onSurfaceVariant = context.themeColor(MaterialR.attr.colorOnSurfaceVariant),
        surfaceTint = context.themeColor(AppCompatR.attr.colorPrimary),
        inverseSurface = context.themeColor(MaterialR.attr.colorSurfaceInverse),
        inverseOnSurface = context.themeColor(MaterialR.attr.colorOnSurfaceInverse),
        error = context.themeColor(AppCompatR.attr.colorError),
        onError = context.themeColor(MaterialR.attr.colorOnError),
        errorContainer = context.themeColor(MaterialR.attr.colorErrorContainer),
        onErrorContainer = context.themeColor(MaterialR.attr.colorOnErrorContainer),
        outline = context.themeColor(MaterialR.attr.colorOutline),
        outlineVariant = context.themeColor(MaterialR.attr.colorOutlineVariant),
        surfaceBright = context.themeColor(MaterialR.attr.colorSurfaceBright),
        surfaceDim = context.themeColor(MaterialR.attr.colorSurfaceDim),
        surfaceContainer = context.themeColor(MaterialR.attr.colorSurfaceContainer),
        surfaceContainerHigh = context.themeColor(MaterialR.attr.colorSurfaceContainerHigh),
        surfaceContainerHighest = context.themeColor(MaterialR.attr.colorSurfaceContainerHighest),
        surfaceContainerLow = context.themeColor(MaterialR.attr.colorSurfaceContainerLow),
        surfaceContainerLowest = context.themeColor(MaterialR.attr.colorSurfaceContainerLowest)
    )
}

private fun Context.themeColor(@AttrRes attr: Int): Color =
    Color(MaterialColors.getColor(this, attr, Color.Magenta.toArgb()))

// For legacy MDC attrs with no ColorScheme slot (e.g. colorPrimaryVariant).
@Composable
fun themeAttrColor(@AttrRes attr: Int): Color = LocalContext.current.themeColor(attr)

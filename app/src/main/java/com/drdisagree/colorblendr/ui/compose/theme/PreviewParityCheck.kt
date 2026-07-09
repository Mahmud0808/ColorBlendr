package com.drdisagree.colorblendr.ui.compose.theme

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.domain.PreviewController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Debug-only: verifies the locally computed preview scheme matches what the
// view theme resolves from the applied overlay. Only meaningful while the
// current prefs equal the applied overlay (i.e. no pending preview).
@Composable
fun PreviewSchemeParityCheck(viewScheme: ColorScheme) {
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        if (isFirstRun() || isWorkMethodUnknown()) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            try {
                val previewScheme = previewColorScheme(
                    PreviewController.buildPreviewColors(),
                    isDark
                )
                val mismatches = compareSchemes(viewScheme, previewScheme)
                if (mismatches.isEmpty()) {
                    Log.d(TAG, "Preview scheme matches view theme")
                } else {
                    mismatches.forEach { Log.w(TAG, it) }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Parity check skipped: ${e.message}")
            }
        }
    }
}

private fun compareSchemes(view: ColorScheme, preview: ColorScheme): List<String> {
    val roles: List<Triple<String, Color, Color>> = listOf(
        Triple("primary", view.primary, preview.primary),
        Triple("onPrimary", view.onPrimary, preview.onPrimary),
        Triple("primaryContainer", view.primaryContainer, preview.primaryContainer),
        Triple("onPrimaryContainer", view.onPrimaryContainer, preview.onPrimaryContainer),
        Triple("inversePrimary", view.inversePrimary, preview.inversePrimary),
        Triple("secondary", view.secondary, preview.secondary),
        Triple("onSecondary", view.onSecondary, preview.onSecondary),
        Triple("secondaryContainer", view.secondaryContainer, preview.secondaryContainer),
        Triple("onSecondaryContainer", view.onSecondaryContainer, preview.onSecondaryContainer),
        Triple("tertiary", view.tertiary, preview.tertiary),
        Triple("onTertiary", view.onTertiary, preview.onTertiary),
        Triple("tertiaryContainer", view.tertiaryContainer, preview.tertiaryContainer),
        Triple("onTertiaryContainer", view.onTertiaryContainer, preview.onTertiaryContainer),
        Triple("background", view.background, preview.background),
        Triple("onBackground", view.onBackground, preview.onBackground),
        Triple("surface", view.surface, preview.surface),
        Triple("onSurface", view.onSurface, preview.onSurface),
        Triple("surfaceVariant", view.surfaceVariant, preview.surfaceVariant),
        Triple("onSurfaceVariant", view.onSurfaceVariant, preview.onSurfaceVariant),
        Triple("surfaceTint", view.surfaceTint, preview.surfaceTint),
        Triple("inverseSurface", view.inverseSurface, preview.inverseSurface),
        Triple("inverseOnSurface", view.inverseOnSurface, preview.inverseOnSurface),
        Triple("error", view.error, preview.error),
        Triple("onError", view.onError, preview.onError),
        Triple("errorContainer", view.errorContainer, preview.errorContainer),
        Triple("onErrorContainer", view.onErrorContainer, preview.onErrorContainer),
        Triple("outline", view.outline, preview.outline),
        Triple("outlineVariant", view.outlineVariant, preview.outlineVariant),
        Triple("surfaceBright", view.surfaceBright, preview.surfaceBright),
        Triple("surfaceDim", view.surfaceDim, preview.surfaceDim),
        Triple("surfaceContainer", view.surfaceContainer, preview.surfaceContainer),
        Triple("surfaceContainerHigh", view.surfaceContainerHigh, preview.surfaceContainerHigh),
        Triple(
            "surfaceContainerHighest",
            view.surfaceContainerHighest,
            preview.surfaceContainerHighest
        ),
        Triple("surfaceContainerLow", view.surfaceContainerLow, preview.surfaceContainerLow),
        Triple(
            "surfaceContainerLowest",
            view.surfaceContainerLowest,
            preview.surfaceContainerLowest
        )
    )

    return roles.filter { (_, viewColor, previewColor) -> viewColor != previewColor }
        .map { (name, viewColor, previewColor) ->
            "Mismatch $name: view=${viewColor.hexString()} preview=${previewColor.hexString()}"
        }
}

private fun Color.hexString(): String =
    "#%08X".format(value.toLong() shr 32)

private const val TAG = "PreviewParity"

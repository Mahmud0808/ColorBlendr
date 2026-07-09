package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.ColorBlendr
import com.drdisagree.colorblendr.data.domain.PreviewController

@Composable
fun ColorBlendrTheme(content: @Composable () -> Unit) {
    if (LocalInspectionMode.current) {
        ColorBlendr.initializeForPreview(LocalContext.current)
    }

    val previewColors by PreviewController.previewColors.collectAsStateWithLifecycle()
    val viewScheme = viewThemeColorScheme()
    val colorScheme = previewColors?.let { previewColorScheme(it, isSystemInDarkTheme()) }
        ?: viewScheme

    if (BuildConfig.DEBUG && !LocalInspectionMode.current && previewColors == null) {
        PreviewSchemeParityCheck(viewScheme)
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

@Preview
@Composable
private fun ColorBlendrThemePreview() {
    ColorBlendrTheme {
        Surface {
            Text(text = "ColorBlendr")
        }
    }
}

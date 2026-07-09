package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ColorBlendrTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = viewThemeColorScheme(),
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

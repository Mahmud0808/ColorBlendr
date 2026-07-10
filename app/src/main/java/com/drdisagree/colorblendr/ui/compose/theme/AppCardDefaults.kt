package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.utils.colors.PreviewResourcesOverride

object AppCardDefaults {

    // Same overridden m3_card_stroke_color resource MDC outlined cards resolve,
    // including the app's per-API and night bucket overrides. Preview-aware:
    // the resource maps to neutral_variant tone 70/20 (palette row 4, shade
    // 300/800), resolved from the locally computed palette while previewing.
    @Composable
    fun outlinedBorder(): BorderStroke {
        // Re-resolve when the preview resources loader is added or removed.
        PreviewResourcesOverride.revision

        val previewColors by PreviewController.previewColors.collectAsStateWithLifecycle()
        val isDark = isSystemInDarkTheme()

        val strokeColor = previewColors?.let {
            val palette = if (isDark) it.paletteDark else it.paletteLight
            Color(palette[4][if (isDark) 10 else 5])
        } ?: colorResource(R.color.m3_card_stroke_color)

        return BorderStroke(1.dp, strokeColor)
    }
}

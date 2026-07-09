package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R

object AppCardDefaults {

    // Same overridden m3_card_stroke_color resource MDC outlined cards resolve,
    // including the app's per-API and night bucket overrides.
    @Composable
    fun outlinedBorder(): BorderStroke =
        BorderStroke(1.dp, colorResource(R.color.m3_card_stroke_color))
}

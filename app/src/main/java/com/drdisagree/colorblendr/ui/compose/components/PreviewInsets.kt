package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.utils.LocalWidthClass
import com.drdisagree.colorblendr.ui.compose.utils.WidthClass

val LocalPreviewBottomInset = compositionLocalOf { 0.dp }

@Composable
fun navBottomInset(): Dp =
    if (LocalWidthClass.current == WidthClass.Compact) {
        0.dp
    } else {
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    }

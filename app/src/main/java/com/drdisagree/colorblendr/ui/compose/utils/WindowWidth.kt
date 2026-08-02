package com.drdisagree.colorblendr.ui.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

val LocalWidthClass = compositionLocalOf { WidthClass.Compact }

@Composable
fun currentWidthClass(): WidthClass {
    val widthDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    return when {
        widthDp < 600.dp -> WidthClass.Compact
        widthDp < 840.dp -> WidthClass.Medium
        else -> WidthClass.Expanded
    }
}
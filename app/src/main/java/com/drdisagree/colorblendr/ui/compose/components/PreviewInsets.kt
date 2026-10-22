package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.utils.LocalWidthClass
import com.drdisagree.colorblendr.ui.compose.utils.WidthClass

val LocalPreviewBottomInset = compositionLocalOf<() -> Dp> { { 0.dp } }

@Composable
fun previewContentPadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues {
    val inset = LocalPreviewBottomInset.current
    return remember(start, top, end, bottom, inset) {
        PreviewContentPadding(start, top, end, bottom, inset)
    }
}

private class PreviewContentPadding(
    private val start: Dp,
    private val top: Dp,
    private val end: Dp,
    private val bottom: Dp,
    private val inset: () -> Dp
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        if (layoutDirection == LayoutDirection.Ltr) start else end

    override fun calculateTopPadding(): Dp = top

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        if (layoutDirection == LayoutDirection.Ltr) end else start

    override fun calculateBottomPadding(): Dp = bottom + inset()
}

@Composable
fun navBottomInset(): Dp =
    if (LocalWidthClass.current == WidthClass.Compact) {
        0.dp
    } else {
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    }

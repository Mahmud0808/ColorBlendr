package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Bottom space reserved for preview apply/discard buttons; scrollable screens
// add it as bottom padding so content can scroll above them.
val LocalPreviewBottomInset = compositionLocalOf<Dp> { 0.dp }

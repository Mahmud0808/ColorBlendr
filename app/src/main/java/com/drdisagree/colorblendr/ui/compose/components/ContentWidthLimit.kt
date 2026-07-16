package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.utils.LocalWidthClass
import com.drdisagree.colorblendr.ui.compose.utils.WidthClass

@Composable
fun Modifier.contentWidthLimit(max: Dp = 640.dp): Modifier =
    if (LocalWidthClass.current == WidthClass.Compact) {
        this
    } else {
        fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = max)
    }
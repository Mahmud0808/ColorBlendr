package com.drdisagree.colorblendr.ui.compose.utils

import androidx.annotation.ColorInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor

private val GRID_LIGHT = Color(0xFFFFFFFF)
private val GRID_DARK = Color(0xFFCCCCCC)

fun Modifier.transparencyGrid(@ColorInt color: Int?, cell: Dp = 6.dp): Modifier =
    if (color == null || AndroidColor.alpha(color) == 0xFF) {
        this
    } else {
        drawBehind {
            val step = cell.toPx()
            drawRect(GRID_LIGHT)
            var row = 0
            var y = 0f
            while (y < size.height) {
                var x = if (row % 2 == 0) 0f else step
                while (x < size.width) {
                    drawRect(GRID_DARK, Offset(x, y), Size(step, step))
                    x += step * 2
                }
                y += step
                row++
            }
        }
    }

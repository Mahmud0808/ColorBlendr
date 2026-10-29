package com.drdisagree.colorblendr.ui.compose.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.horizontalFadingEdges(
    scrollState: ScrollState,
    length: Dp = 32.dp
): Modifier = fadingEdges(scrollState, length, vertical = false)

fun Modifier.verticalFadingEdges(
    scrollState: ScrollState,
    length: Dp = 32.dp
): Modifier = fadingEdges(scrollState, length, vertical = true)

private fun Modifier.fadingEdges(
    scrollState: ScrollState,
    length: Dp,
    vertical: Boolean
): Modifier = composed {
    val startAlpha by animateFloatAsState(
        targetValue = if (scrollState.canScrollBackward) 1f else 0f,
        label = "fadeStart"
    )
    val endAlpha by animateFloatAsState(
        targetValue = if (scrollState.canScrollForward) 1f else 0f,
        label = "fadeEnd"
    )

    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val extent = if (vertical) size.height else size.width
            val edge = length.toPx().coerceAtMost(extent / 2)
            fun point(position: Float) = if (vertical) Offset(0f, position) else Offset(position, 0f)

            if (startAlpha > 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        0f to Color.Black.copy(alpha = 1f - startAlpha),
                        1f to Color.Black,
                        start = point(0f),
                        end = point(edge)
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            if (endAlpha > 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        0f to Color.Black,
                        1f to Color.Black.copy(alpha = 1f - endAlpha),
                        start = point(extent - edge),
                        end = point(extent)
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
}

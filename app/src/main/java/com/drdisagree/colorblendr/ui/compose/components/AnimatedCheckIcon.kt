package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Check icon: selecting fills circle and strokes tick in like being drawn;
// deselecting reverses.
@Composable
fun AnimatedCheckIcon(
    selected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "checkDraw"
    )

    Canvas(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        val clampedProgress = progress.coerceIn(0f, 1f)
        val stroke = size.minDimension / 12f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - stroke

        // Outline ring, strengthens toward filled state.
        drawCircle(
            color = tint.copy(alpha = 0.2f + 0.8f * clampedProgress),
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        // Fill sweeping in while selecting.
        if (clampedProgress > 0f) {
            drawCircle(
                color = tint.copy(alpha = clampedProgress),
                radius = radius * clampedProgress,
                center = center
            )
        }

        // Tick drawn progressively, cut out of fill.
        val tickProgress = ((clampedProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
        if (tickProgress > 0f) {
            val tickPath = Path().apply {
                moveTo(size.width * 0.28f, size.height * 0.53f)
                lineTo(size.width * 0.43f, size.height * 0.68f)
                lineTo(size.width * 0.73f, size.height * 0.36f)
            }
            val measure = PathMeasure().apply { setPath(tickPath, false) }
            val segment = Path()
            measure.getSegment(0f, measure.length * tickProgress, segment, true)

            drawPath(
                path = segment,
                color = Color.Black,
                style = Stroke(
                    width = stroke * 1.2f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = BlendMode.Clear
            )
        }
    }
}

@Preview
@Composable
private fun AnimatedCheckIconPreview() {
    ColorBlendrTheme {
        AnimatedCheckIcon(
            selected = true,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

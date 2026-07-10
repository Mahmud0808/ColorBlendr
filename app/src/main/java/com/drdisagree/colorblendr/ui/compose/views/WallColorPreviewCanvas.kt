package com.drdisagree.colorblendr.ui.compose.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// 12dp rounded square, arcs inset 6dp, 10dp center circle over a clear
// circle, 4px round-cap tick when selected.
@Composable
fun WallColorPreviewCanvas(
    colors: WallColorPreviewColors,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        val haptics = LocalHapticFeedback.current
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.9f else 1f,
            label = "swatchPress"
        )

        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onClick()
            }
    } else {
        modifier
    }

    Canvas(modifier = clickModifier) {
        val cornerRadius = 12.dp.toPx()
        val padding = 6.dp.toPx()
        val circleRadius = 10.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        drawRoundRect(
            color = colors.square,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        val arcSize = Size(size.width - 2 * padding, size.height - 2 * padding)
        val arcOffset = Offset(padding, padding)

        drawArc(
            color = colors.halfCircle,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = arcOffset,
            size = arcSize
        )
        drawArc(
            color = colors.firstQuarterCircle,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = arcOffset,
            size = arcSize
        )
        drawArc(
            color = colors.secondQuarterCircle,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = arcOffset,
            size = arcSize
        )

        drawCircle(color = colors.square, radius = circleRadius, center = center)
        drawCircle(color = colors.centerCircle, radius = circleRadius, center = center)

        if (selected) {
            val tickPath = Path().apply {
                moveTo(center.x - circleRadius / 2, center.y)
                lineTo(center.x - circleRadius / 6, center.y + circleRadius / 3)
                lineTo(center.x + circleRadius / 2, center.y - circleRadius / 3)
            }
            drawPath(
                path = tickPath,
                color = colors.tick,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
    }
}

@Preview
@Composable
private fun WallColorPreviewCanvasPreview() {
    ColorBlendrTheme {
        WallColorPreviewCanvas(
            colors = WallColorPreviewColors(
                halfCircle = Color(0xFFB5C4FF),
                firstQuarterCircle = Color(0xFFC4C6D0),
                secondQuarterCircle = Color(0xFFE2BADB),
                square = Color(0xFFFAF8FF),
                centerCircle = Color(0xFF4285F4),
                tick = Color.White
            ),
            selected = true,
            modifier = Modifier.size(48.dp)
        )
    }
}

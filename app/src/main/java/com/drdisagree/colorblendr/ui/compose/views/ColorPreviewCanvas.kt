package com.drdisagree.colorblendr.ui.compose.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.google.android.material.R as MaterialR

// 12dp rounded square, half circle at 180deg sweep 180, quarter arcs at
// 90/0 sweeping 90, inset by padding.
@Composable
fun ColorPreviewCanvas(
    modifier: Modifier = Modifier,
    squareColor: Color = defaultSquareColor(),
    halfCircleColor: Color = colorResource(MaterialR.color.material_dynamic_primary90),
    firstQuarterCircleColor: Color = colorResource(MaterialR.color.material_dynamic_secondary90),
    secondQuarterCircleColor: Color = colorResource(MaterialR.color.material_dynamic_tertiary90),
    padding: Dp = 10.dp,
    enabled: Boolean = true
) {
    Canvas(modifier = modifier.alpha(if (enabled) 1f else 0.6f)) {
        val cornerRadius = 12.dp.toPx()
        val pad = padding.toPx()

        drawRoundRect(
            color = squareColor,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        val arcSize = Size(size.width - 2 * pad, size.height - 2 * pad)
        val arcOffset = Offset(pad, pad)

        drawArc(
            color = halfCircleColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = arcOffset,
            size = arcSize
        )
        drawArc(
            color = firstQuarterCircleColor,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = arcOffset,
            size = arcSize
        )
        drawArc(
            color = secondQuarterCircleColor,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = arcOffset,
            size = arcSize
        )
    }
}

@Composable
fun defaultSquareColor(): Color = colorResource(
    if (isSystemInDarkTheme()) {
        MaterialR.color.material_dynamic_neutral10
    } else {
        MaterialR.color.material_dynamic_neutral99
    }
)

@Preview
@Composable
private fun ColorPreviewCanvasPreview() {
    ColorBlendrTheme {
        ColorPreviewCanvas(modifier = Modifier.size(80.dp))
    }
}

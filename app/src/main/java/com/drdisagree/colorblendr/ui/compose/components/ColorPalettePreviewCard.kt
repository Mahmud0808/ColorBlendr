package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import android.graphics.Color as AndroidColor

// Large tappable entry to color palette page; previews current
// primary/secondary/tertiary colors as overlapping gradient circles.
@Composable
fun ColorPalettePreviewCard(
    title: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PositionedCard(
        position = WidgetPosition.Single,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                GradientCircle(
                    startColor = MaterialTheme.colorScheme.primaryFixedDim,
                    endColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.offset(x = (-38).dp)
                )
                GradientCircle(
                    startColor = MaterialTheme.colorScheme.secondaryFixedDim,
                    endColor = MaterialTheme.colorScheme.secondary,
                    startShadow = true,
                    modifier = Modifier
                )
                GradientCircle(
                    startColor = MaterialTheme.colorScheme.tertiaryFixedDim,
                    endColor = MaterialTheme.colorScheme.tertiary,
                    startShadow = true,
                    modifier = Modifier.offset(x = 38.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun GradientCircle(
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    startShadow: Boolean = false
) {
    val shadowColor = Color.Black.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .size(64.dp)
            .then(
                if (startShadow) {
                    Modifier.drawBehind {
                        val radius = size.minDimension / 2f
                        drawIntoCanvas { canvas ->
                            val paint = Paint().nativePaint.apply {
                                isAntiAlias = true
                                color = AndroidColor.TRANSPARENT
                                setShadowLayer(
                                    5.dp.toPx(),
                                    -3.dp.toPx(),
                                    0f,
                                    shadowColor.toArgb()
                                )
                            }
                            canvas.nativeCanvas.drawCircle(
                                center.x,
                                center.y,
                                radius,
                                paint
                            )
                        }
                    }
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
    )
}

@Preview
@Composable
private fun ColorPalettePreviewCardPreview() {
    ColorBlendrTheme {
        ColorPalettePreviewCard(
            title = "Color palette",
            summary = "View all the shades of generated colors",
            onClick = {}
        )
    }
}

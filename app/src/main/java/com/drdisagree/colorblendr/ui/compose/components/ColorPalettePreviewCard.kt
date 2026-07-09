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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Large tappable entry to the color palette page, previewing the current
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
                    modifier = Modifier
                )
                GradientCircle(
                    startColor = MaterialTheme.colorScheme.tertiaryFixedDim,
                    endColor = MaterialTheme.colorScheme.tertiary,
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
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

package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Mirrors MiscUtil.setCardCornerRadius positions 0-3 and the
// bg_container_top/mid/bottom grouped-row shapes.
enum class WidgetPosition {
    Single,
    Top,
    Middle,
    Bottom
}

@Composable
fun PositionedCard(
    position: WidgetPosition,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val radius = dimensionResource(R.dimen.container_corner_radius)
    val radiusSmall = dimensionResource(R.dimen.container_corner_radius_small)
    val marginBottom = dimensionResource(R.dimen.container_margin_bottom)
    val marginBottomSmall = dimensionResource(R.dimen.container_margin_bottom_small)

    val (topRadius, bottomRadius) = when (position) {
        WidgetPosition.Single -> radius to radius
        WidgetPosition.Top -> radius to radiusSmall
        WidgetPosition.Middle -> radiusSmall to radiusSmall
        WidgetPosition.Bottom -> radiusSmall to radius
    }
    val bottomMargin = when (position) {
        WidgetPosition.Single, WidgetPosition.Bottom -> marginBottom
        WidgetPosition.Top, WidgetPosition.Middle -> marginBottomSmall
    }

    PositionedCardBase(
        shape = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius),
        bottomMargin = bottomMargin,
        modifier = modifier,
        backgroundColor = backgroundColor,
        onClick = onClick,
        content = content
    )
}

@Composable
fun PositionedCardBase(
    shape: RoundedCornerShape,
    bottomMargin: Dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = shape,
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.container_margin_horizontal),
                end = dimensionResource(R.dimen.container_margin_horizontal),
                bottom = bottomMargin
            )
    ) {
        Column(
            modifier = if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun PositionedCardPreview() {
    ColorBlendrTheme {
        Column {
            PositionedCard(position = WidgetPosition.Top) {
                Text(text = "Top", modifier = Modifier.padding(16.dp))
            }
            PositionedCard(position = WidgetPosition.Middle) {
                Text(text = "Middle", modifier = Modifier.padding(16.dp))
            }
            PositionedCard(position = WidgetPosition.Bottom) {
                Text(text = "Bottom", modifier = Modifier.padding(16.dp))
            }
            PositionedCard(position = WidgetPosition.Single) {
                Text(text = "Single", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

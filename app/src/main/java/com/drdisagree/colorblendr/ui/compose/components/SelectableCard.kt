package com.drdisagree.colorblendr.ui.compose.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Mirrors SelectableViewWidget: outlined card that fills primaryContainer when
// selected, with checked icon fading 0.2 -> 1.0. Margins come from the caller,
// matching view_widget_selectable.xml which has none of its own.
@Composable
fun SelectableCard(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    position: WidgetPosition = WidgetPosition.Single
) {
    val radius = dimensionResource(R.dimen.container_corner_radius)
    val radiusSmall = dimensionResource(R.dimen.container_corner_radius_small)
    val (topRadius, bottomRadius) = when (position) {
        WidgetPosition.Single -> radius to radius
        WidgetPosition.Top -> radius to radiusSmall
        WidgetPosition.Middle -> radiusSmall to radiusSmall
        WidgetPosition.Bottom -> radiusSmall to radius
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSmallHeightDevice =
        isLandscape && configuration.screenWidthDp >= configuration.screenHeightDp * 1.8
    val minHeight = if (isSmallHeightDevice) Dp.Unspecified else 100.dp

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val strokeWidth = with(LocalDensity.current) { 2.toDp() }

    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        label = "selectableCardPress"
    )

    Surface(
        shape = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = if (selected) {
            null
        } else {
            BorderStroke(strokeWidth, AppCardDefaults.outlinedBorder().brush)
        },
        onClick = {
            if (!selected) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onSelect()
            }
        },
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            AnimatedCheckIcon(
                selected = selected,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                if (!isSmallHeightDevice) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SelectableCardPreview() {
    ColorBlendrTheme {
        Column {
            SelectableCard(
                title = "Root",
                description = "Use root access to apply colors",
                selected = true,
                onSelect = {},
                modifier = Modifier.padding(bottom = 2.dp)
            )
            SelectableCard(
                title = "Shizuku",
                description = "Use Shizuku to apply colors",
                selected = false,
                onSelect = {}
            )
        }
    }
}

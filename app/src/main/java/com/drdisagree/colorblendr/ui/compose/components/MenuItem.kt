package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import com.google.android.material.R as MaterialR

@Composable
fun MenuItem(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: Painter? = null,
    iconSpaceReserved: Boolean = false,
    showEndArrow: Boolean = false,
    enabled: Boolean = true,
    disabledReason: String? = null,
    position: WidgetPosition = WidgetPosition.Single,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.98f else 1f,
        label = "menuItemPress"
    )

    PositionedCard(
        position = position,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onLongClick = onLongClick,
                    onClick = onClick
                )
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = themeAttrColor(MaterialR.attr.colorPrimaryVariant)
                        .copy(alpha = if (enabled) 1f else 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else if (iconSpaceReserved) {
                Spacer(modifier = Modifier.width(36.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                TitleWithBadge(
                    title = title,
                    badge = if (enabled) null else disabledReason,
                    textStyle = MaterialTheme.typography.titleSmall,
                    textColor = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = if (enabled) 1f else 0.6f)
                )
                if (summary != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = if (enabled) 0.8f else 0.4f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (showEndArrow) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_end),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = if (enabled) 1f else 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun MenuItemPreview() {
    ColorBlendrTheme {
        Column {
            MenuItem(
                title = "Backup and restore",
                summary = "Save and restore your settings",
                icon = painterResource(R.drawable.ic_backup),
                showEndArrow = true,
                position = WidgetPosition.Top
            )
            MenuItem(
                title = "About",
                summary = "Version and credits",
                icon = painterResource(R.drawable.ic_info),
                enabled = false,
                disabledReason = "Requires root",
                position = WidgetPosition.Bottom
            )
        }
    }
}

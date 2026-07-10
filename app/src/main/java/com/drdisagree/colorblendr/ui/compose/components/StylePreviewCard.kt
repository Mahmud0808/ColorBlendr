package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.views.ColorPreviewCanvas
import com.drdisagree.colorblendr.ui.compose.views.defaultSquareColor
import com.google.android.material.R as MaterialR

// Mirrors StylePreviewWidget: outlined card with 60dp ColorPreview, selected
// state fills primaryContainer with a 2px stroke, long-press opens the
// custom-style popup (edit/update/delete) like custom_style_menu.xml.
@Composable
fun StylePreviewCard(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    colorPalette: List<List<Int>>? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    position: WidgetPosition = WidgetPosition.Single,
    onEdit: (() -> Unit)? = null,
    onUpdate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val radius = dimensionResource(R.dimen.container_corner_radius)
    val radiusSmall = dimensionResource(R.dimen.container_corner_radius_small)
    val (topRadius, bottomRadius) = when (position) {
        WidgetPosition.Single -> radius to radius
        WidgetPosition.Top -> radius to radiusSmall
        WidgetPosition.Middle -> radiusSmall to radiusSmall
        WidgetPosition.Bottom -> radiusSmall to radius
    }

    val isDark = isSystemInDarkTheme()
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val strokeWidth = with(LocalDensity.current) { 2.toDp() }
    val hasMenu = onEdit != null || onUpdate != null || onDelete != null
    var menuExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.98f else 1f,
        label = "styleCardPress"
    )
    var pressX by remember { mutableStateOf(0.dp) }
    var cardWidth by remember { mutableStateOf(0.dp) }
    var menuWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.container_margin_horizontal))
    ) {
        Surface(
            shape = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius),
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
            border = if (selected) {
                BorderStroke(strokeWidth, AppCardDefaults.outlinedBorder().brush)
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { cardWidth = with(density) { it.width.toDp() } }
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(hasMenu) {
                        if (!hasMenu) return@pointerInput

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            pressX = with(density) { down.position.x.toDp() }
                        }
                    }
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        enabled = enabled,
                        onLongClick = if (hasMenu) {
                            { menuExpanded = true }
                        } else {
                            null
                        },
                        onClick = { if (!selected) onSelect() }
                    )
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                ColorPreviewCanvas(
                    squareColor = colorPalette?.let {
                        Color(it[4][if (!isDark) 2 else 9])
                    } ?: defaultSquareColor(),
                    halfCircleColor = colorPalette?.let { Color(it[0][4]) }
                        ?: colorResource(MaterialR.color.material_dynamic_primary90),
                    firstQuarterCircleColor = colorPalette?.let { Color(it[2][5]) }
                        ?: colorResource(MaterialR.color.material_dynamic_secondary90),
                    secondQuarterCircleColor = colorPalette?.let { Color(it[1][6]) }
                        ?: colorResource(MaterialR.color.material_dynamic_tertiary90),
                    enabled = enabled,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    TitleWithBadge(
                        title = title,
                        badge = if (enabled) null else disabledReason,
                        textStyle = MaterialTheme.typography.titleSmall,
                        textColor = contentColor.copy(alpha = if (enabled) 1f else 0.6f)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = if (enabled) 0.8f else 0.4f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (hasMenu) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                // Follow the touch x, but keep the popup within the card.
                offset = DpOffset(
                    pressX.coerceIn(0.dp, (cardWidth - menuWidth).coerceAtLeast(0.dp)),
                    0.dp
                )
            ) {
                Column(
                    modifier = Modifier.onSizeChanged {
                        menuWidth = with(density) { it.width.toDp() }
                    }
                ) {
                    if (onEdit != null) {
                        StyleMenuItem(
                            textResId = R.string.edit,
                            iconResId = R.drawable.ic_edit,
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                    }
                    if (onUpdate != null) {
                        StyleMenuItem(
                            textResId = R.string.update,
                            iconResId = R.drawable.ic_renew,
                            onClick = {
                                menuExpanded = false
                                onUpdate()
                            }
                        )
                    }
                    if (onDelete != null) {
                        StyleMenuItem(
                            textResId = R.string.delete,
                            iconResId = R.drawable.ic_delete,
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleMenuItem(
    textResId: Int,
    iconResId: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(textResId)) },
        leadingIcon = {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = onClick
    )
}

@Preview
@Composable
private fun StylePreviewCardPreview() {
    ColorBlendrTheme {
        Column {
            StylePreviewCard(
                title = "Tonal Spot",
                description = "Default Material You style",
                selected = true,
                onSelect = {},
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StylePreviewCard(
                title = "My custom style",
                description = "Custom saved style",
                selected = false,
                onSelect = {},
                onEdit = {},
                onDelete = {}
            )
        }
    }
}

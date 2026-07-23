package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingCard(
    item: PendingSubmission,
    busy: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onBlock: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        label = "pendingCardScale"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(
                if (selected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(24.dp)
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp, end = 12.dp, top = 16.dp, bottom = 10.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = selectionMode,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(24.dp)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                                CircleShape
                            )
                    ) {
                        if (selected) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Rounded.Check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    modifier = Modifier.width(62.dp)
                ) {
                    listOfNotNull(
                        item.seedColor, item.secondaryColor, item.tertiaryColor
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    CircleShape
                                )
                                .padding(2.dp)
                                .background(Color(color), CircleShape)
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.author.ifEmpty { stringResource(R.string.anonymous) }} · " +
                                DateFormat.getDateInstance(DateFormat.MEDIUM)
                                    .format(Date(item.created)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(modifier = Modifier.padding(top = 6.dp)) {
                        DeviceChip(device = item.device)
                    }
                }
            }
            AnimatedVisibility(visible = !selectionMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    FilledIconButton(
                        onClick = onApprove,
                        enabled = !busy,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Check),
                            contentDescription = stringResource(R.string.approve)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onReject,
                        enabled = !busy,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Close),
                            contentDescription = stringResource(R.string.reject)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onBlock,
                        enabled = !busy,
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Block),
                            contentDescription = stringResource(R.string.block)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PendingCardPreview() {
    DevTheme {
        PendingCard(
            item = PendingSubmission(
                id = "ocean-breeze-abc123",
                name = "Ocean Breeze",
                author = "DrDisagree",
                device = "a1b2c3d4e5f6",
                created = 1752800000000L,
                seedColor = 0xFF51BDFF.toInt(),
                secondaryColor = 0xFF7C4DFF.toInt(),
                tertiaryColor = 0xFF26A69A.toInt(),
                payloadJson = "{}"
            ),
            busy = false,
            selectionMode = false,
            selected = false,
            onClick = {},
            onLongClick = {},
            onApprove = {},
            onReject = {},
            onBlock = {}
        )
    }
}

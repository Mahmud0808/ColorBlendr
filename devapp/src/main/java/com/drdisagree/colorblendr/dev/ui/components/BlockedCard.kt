package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.BlockedEntry
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import java.text.DateFormat
import java.util.Date

@Composable
fun BlockedCard(
    item: BlockedEntry,
    busy: Boolean,
    onUnblock: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 18.dp, end = 10.dp, top = 12.dp, bottom = 12.dp
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.Block),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = item.reason.ifEmpty { item.device.take(12) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = DateFormat.getDateInstance(DateFormat.MEDIUM)
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
            FilledTonalIconButton(
                onClick = onUnblock,
                enabled = !busy,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.LockOpen),
                    contentDescription = stringResource(R.string.unblock)
                )
            }
        }
    }
}

@Preview
@Composable
private fun BlockedCardPreview() {
    DevTheme {
        BlockedCard(
            item = BlockedEntry(
                device = "a1b2c3d4e5f6",
                reason = "Ocean Breeze by DrDisagree",
                created = 1752800000000L
            ),
            busy = false,
            onUnblock = {}
        )
    }
}
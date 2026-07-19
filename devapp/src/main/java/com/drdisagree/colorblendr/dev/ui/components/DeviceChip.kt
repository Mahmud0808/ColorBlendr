package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme

fun deviceColor(device: String): Color {
    var hash = 0
    for (c in device) hash = c.code + ((hash shl 5) - hash)
    val hue = ((hash % 360) + 360) % 360
    return Color.hsv(hue.toFloat(), 0.55f, 0.85f)
}

@Composable
fun DeviceChip(device: String) {
    val color = deviceColor(device)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = device.take(10),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun DeviceChipPreview() {
    DevTheme {
        DeviceChip(device = "a1b2c3d4e5f6a7b8")
    }
}
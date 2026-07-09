package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import com.google.android.material.R as MaterialR

@Composable
fun ColorPickerItem(
    title: String,
    previewColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: Painter? = null,
    iconSpaceReserved: Boolean = false,
    enabled: Boolean = true,
    disabledReason: String? = null,
    position: WidgetPosition = WidgetPosition.Single
) {
    val swatchColor = if (enabled) {
        previewColor
    } else {
        if (isSystemInDarkTheme()) Color(0xFF444444) else Color(0xFFCCCCCC)
    }

    PositionedCard(
        position = position,
        onClick = { if (enabled) onClick() },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
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

            Spacer(
                modifier = Modifier
                    .size(42.dp)
                    .background(color = swatchColor, shape = CircleShape)
            )
        }
    }
}

@Preview
@Composable
private fun ColorPickerItemPreview() {
    ColorBlendrTheme {
        ColorPickerItem(
            title = "Seed color",
            summary = "Custom seed color for palette",
            previewColor = Color(0xFF4285F4),
            icon = painterResource(R.drawable.ic_color_picker),
            onClick = {}
        )
    }
}

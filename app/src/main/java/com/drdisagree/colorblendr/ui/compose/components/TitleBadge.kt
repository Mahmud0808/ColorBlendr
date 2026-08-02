package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TitleWithBadge(
    title: String,
    badge: String?,
    textStyle: TextStyle,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    if (badge.isNullOrEmpty()) {
        Text(text = title, style = textStyle, color = textColor, modifier = modifier)
        return
    }

    val badgeFontSize = textStyle.fontSize.takeOrElse { 14.sp } * 0.75f
    val glyphSize = with(LocalDensity.current) { badgeFontSize.toDp() }
    val badgeColor = textColor.copy(alpha = textColor.alpha * 0.85f)
    val outlineColor = textColor.copy(alpha = textColor.alpha * 0.35f)

    FlowRow(
        modifier = modifier,
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$title ", style = textStyle, color = textColor)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(glyphSize)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badge,
                style = textStyle,
                fontSize = badgeFontSize,
                color = badgeColor
            )
        }
    }
}

@Preview
@Composable
private fun TitleWithBadgePreview() {
    ColorBlendrTheme {
        TitleWithBadge(
            title = "Accurate shades",
            badge = "Requires root",
            textStyle = MaterialTheme.typography.titleSmall,
            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
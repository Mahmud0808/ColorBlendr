package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Mirrors RoundedBackgroundSpan: badge text at 0.75x title size inside an
// 8dp-rounded errorContainer pill with 6dp/2dp padding, centered to the line.
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

    FlowRow(
        modifier = modifier,
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$title ", style = textStyle, color = textColor)
        Text(
            text = badge,
            style = textStyle,
            fontSize = textStyle.fontSize * 0.75f,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
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
            textColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

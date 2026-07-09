package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import com.google.android.material.R as MaterialR

@Composable
fun SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    summaryOn: String? = null,
    summaryOff: String? = null,
    icon: Painter? = null,
    iconSpaceReserved: Boolean = false,
    enabled: Boolean = true,
    disabledReason: String? = null,
    isMasterSwitch: Boolean = false,
    position: WidgetPosition = WidgetPosition.Single
) {
    val haptics = LocalHapticFeedback.current

    fun toggle(newValue: Boolean) {
        haptics.performHapticFeedback(
            if (newValue) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
        )
        onCheckedChange(newValue)
    }

    val masterBackground = if (checked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 64f / 255f)
    }
    val masterContent = if (checked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val content: @Composable () -> Unit = {
        SwitchItemContent(
            title = title,
            checked = checked,
            onCheckedChange = ::toggle,
            summary = when {
                summaryOn != null && summaryOff != null -> if (checked) summaryOn else summaryOff
                else -> summary
            },
            icon = icon,
            iconSpaceReserved = iconSpaceReserved,
            enabled = enabled,
            disabledReason = disabledReason,
            iconTint = if (isMasterSwitch) masterContent else themeAttrColor(MaterialR.attr.colorPrimaryVariant),
            textColor = if (isMasterSwitch) masterContent else MaterialTheme.colorScheme.onSurface
        )
    }

    if (isMasterSwitch) {
        PositionedCardBase(
            shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius_round)),
            bottomMargin = dimensionResource(R.dimen.container_margin_bottom) * 2,
            backgroundColor = masterBackground,
            enabled = enabled,
            onClick = { toggle(!checked) },
            modifier = modifier,
            content = content
        )
    } else {
        PositionedCard(
            position = position,
            enabled = enabled,
            onClick = { toggle(!checked) },
            modifier = modifier,
            content = content
        )
    }
}

@Composable
private fun SwitchItemContent(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String?,
    icon: Painter?,
    iconSpaceReserved: Boolean,
    enabled: Boolean,
    disabledReason: String?,
    iconTint: Color,
    textColor: Color
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
                tint = iconTint.copy(alpha = if (enabled) 1f else 0.4f),
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
                textColor = textColor.copy(alpha = if (enabled) 1f else 0.6f)
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = if (enabled) 0.8f else 0.4f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onCheckedChange(it) },
            enabled = enabled,
            thumbContent = {
                Icon(
                    painter = painterResource(
                        if (checked) R.drawable.ic_tick else R.drawable.ic_cross
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}

@Preview
@Composable
private fun SwitchItemPreview() {
    ColorBlendrTheme {
        Column {
            var first by remember { mutableStateOf(true) }
            var master by remember { mutableStateOf(true) }
            SwitchItem(
                title = "Accurate shades",
                checked = first,
                onCheckedChange = { first = it },
                summary = "Use more accurate color shades",
                icon = painterResource(R.drawable.ic_color_fill),
                position = WidgetPosition.Top
            )
            SwitchItem(
                title = "Pitch black theme",
                checked = false,
                onCheckedChange = {},
                summary = "Requires dark mode",
                enabled = false,
                disabledReason = "Requires root",
                position = WidgetPosition.Bottom
            )
            SwitchItem(
                title = "Use ColorBlendr",
                checked = master,
                onCheckedChange = { master = it },
                summaryOn = "Enabled",
                summaryOff = "Disabled",
                isMasterSwitch = true
            )
        }
    }
}

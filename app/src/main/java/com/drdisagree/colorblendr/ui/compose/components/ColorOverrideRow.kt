package com.drdisagree.colorblendr.ui.compose.components

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.labelRes
import com.drdisagree.colorblendr.ui.compose.utils.transparencyGrid
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToArgbHexColor

@Composable
fun ColorOverrideRow(
    title: String,
    @ColorInt color: Int?,
    override: ColorOverride?,
    position: WidgetPosition,
    onClick: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null
) {
    val hex = color?.let(::intToArgbHexColor) ?: stringResource(R.string.color_unknown)
    val source = when (override) {
        is ColorOverride.Shade -> stringResource(
            R.string.color_tokens_from_shade,
            hex,
            stringResource(override.role.labelRes),
            ShadeScale.shades[override.index]
        )

        is ColorOverride.Custom -> "$hex · ${stringResource(R.string.color_tokens_source_custom)}"
        null -> hex
    }
    val subtitle = if (override == null && note != null) "$source · $note" else source

    PositionedCard(position = position, onClick = onClick, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .transparencyGrid(color)
                    .background(color?.let(::Color) ?: Color.Transparent)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (override != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (override != null) {
                IconButton(onClick = onReset) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.Undo),
                        contentDescription = stringResource(R.string.reset),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun ColorOverrideRowPreview() {
    ColorBlendrTheme {
        Column {
            ColorOverrideRow(
                title = "Surface container",
                color = 0xFF1D2024.toInt(),
                override = null,
                note = "Preset",
                position = WidgetPosition.Top,
                onClick = {},
                onReset = {}
            )
            ColorOverrideRow(
                title = "Primary",
                color = 0xFF3F6AA0.toInt(),
                override = ColorOverride.Shade(PaletteRole.PRIMARY, 8),
                position = WidgetPosition.Bottom,
                onClick = {},
                onReset = {}
            )
        }
    }
}

package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import android.R as AndroidR

// 48dp outlined pill over Haze-blurred backdrop; pass screen's HazeState
// whose source is content scrolling underneath.
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onColorPickClick: (() -> Unit)? = null
) {
    val overlayColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
    val secondaryTextColor = themeAttrColor(AndroidR.attr.textColorSecondary)
    val primaryTextColor = themeAttrColor(AndroidR.attr.textColorPrimary)
    val shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius))

    val blurModifier = if (hazeState != null) {
        Modifier
            .clip(shape)
            .hazeEffect(state = hazeState) {
                style = HazeStyle(
                    backgroundColor = overlayColor,
                    tint = HazeTint(overlayColor)
                )
            }
    } else {
        Modifier
    }

    Surface(
        shape = shape,
        color = if (hazeState != null) {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0f)
        } else {
            overlayColor
        },
        border = AppCardDefaults.outlinedBorder(),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.container_margin_horizontal),
                vertical = 12.dp
            )
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = blurModifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Search),
                contentDescription = null,
                tint = secondaryTextColor,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_hint),
                        fontSize = 16.sp,
                        color = secondaryTextColor
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = primaryTextColor
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Clear),
                        contentDescription = null,
                        tint = secondaryTextColor
                    )
                }
            }
            if (onColorPickClick != null) {
                IconButton(onClick = onColorPickClick) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Colorize),
                        contentDescription = stringResource(R.string.search_by_color),
                        tint = secondaryTextColor
                    )
                }
            }
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.FilterList),
                    contentDescription = null,
                    tint = secondaryTextColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchBarPreview() {
    ColorBlendrTheme {
        SearchBar(query = "", onQueryChange = {}, onFilterClick = {})
    }
}

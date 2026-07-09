package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import android.graphics.Color as AndroidColor

// Header labels are hardcoded in view_color_table.xml; keep them identical.
private val columnHeaders = listOf("Accent\n1", "Accent\n2", "Accent\n3", "Neutral\n1", "Neutral\n2")

// Mirrors view_color_table.xml: outlined card, header row on surfaceContainer,
// then 5 columns x 13 rows of 16dp-tall color cells.
@Composable
fun ColorTable(
    colors: List<List<Int>>,
    modifier: Modifier = Modifier,
    onCellClick: ((column: Int, row: Int) -> Unit)? = null,
    onCellLongClick: ((column: Int, row: Int) -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.surface,
        border = AppCardDefaults.outlinedBorder(),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.container_margin_horizontal),
                end = dimensionResource(R.dimen.container_margin_horizontal),
                bottom = dimensionResource(R.dimen.container_margin_bottom)
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(vertical = 10.dp)
            ) {
                columnHeaders.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(5) { column ->
                    Column(modifier = Modifier.weight(1f)) {
                        repeat(13) { row ->
                            val color = colors.getOrNull(column)?.getOrNull(row)
                                ?.let { Color(it) } ?: Color.White
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(color)
                                    .combinedClickable(
                                        enabled = onCellClick != null || onCellLongClick != null,
                                        onLongClick = onCellLongClick?.let {
                                            { it(column, row) }
                                        },
                                        onClick = { onCellClick?.invoke(column, row) }
                                    )
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ColorTablePreview() {
    ColorBlendrTheme {
        ColorTable(
            colors = List(5) { column ->
                List(13) { row ->
                    AndroidColor.HSVToColor(
                        floatArrayOf(column * 60f, 0.5f, 1f - row * 0.07f)
                    )
                }
            }
        )
    }
}

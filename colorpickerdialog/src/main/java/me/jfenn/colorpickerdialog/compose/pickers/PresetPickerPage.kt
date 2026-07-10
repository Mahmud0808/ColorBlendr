package me.jfenn.colorpickerdialog.compose.pickers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jfenn.colorpickerdialog.compose.components.SelectableCircleColor

// PresetPickerView.DEFAULT_PRESETS, verbatim.
internal val DEFAULT_PRESETS = listOf(
    0xfff44336.toInt(),
    0xffe91e63.toInt(),
    0xff9c27b0.toInt(),
    0xff673ab7.toInt(),
    0xff3f51b5.toInt(),
    0xff2196f3.toInt(),
    0xff03a9f4.toInt(),
    0xff00bcd4.toInt(),
    0xff009688.toInt(),
    0xff4caf50.toInt(),
    0xff8bc34a.toInt(),
    0xffcddc39.toInt(),
    0xffffeb3b.toInt(),
    0xffffc107.toInt(),
    0xffff9800.toInt(),
    0xffff5722.toInt(),
    0xff795548.toInt(),
    0xff9e9e9e.toInt(),
    0xff607d8b.toInt()
)

// Compose port of PresetPickerView + colorpicker_layout_preset_picker.xml:
// 300dp tall 4-column grid (26dp side padding, 8dp top, 126dp bottom,
// clipToPadding=false) of selectable circle swatches; the swatch equal to the
// current color renders selected.
@Composable
internal fun PresetPickerPage(
    color: Int,
    presets: List<Int>,
    onColorPicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(
            start = 26.dp,
            end = 26.dp,
            top = 8.dp,
            bottom = 126.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        items(presets) { preset ->
            SelectableCircleColor(
                color = preset,
                selected = color == preset,
                onClick = { onColorPicked(preset) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetPickerPagePreview() {
    PresetPickerPage(
        color = 0xff2196f3.toInt(),
        presets = DEFAULT_PRESETS,
        onColorPicked = {}
    )
}

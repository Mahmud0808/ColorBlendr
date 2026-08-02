package me.jfenn.colorpickerdialog.compose.pickers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jfenn.colorpickerdialog.R
import me.jfenn.colorpickerdialog.compose.components.AlphaSliderRow
import me.jfenn.colorpickerdialog.compose.theme.PickerColors
import me.jfenn.colorpickerdialog.compose.components.PickerSliderRow
import android.graphics.Color as AndroidColor

// Red, green, blue slider rows with solid theme-colored tracks and matching
// thumb tints; optional alpha row.
@Composable
internal fun RgbPickerPage(
    color: Int,
    alphaEnabled: Boolean,
    onColorPicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val redColor = remember(context) { Color(PickerColors.red(context)) }
    val greenColor = remember(context) { Color(PickerColors.green(context)) }
    val blueColor = remember(context) { Color(PickerColors.blue(context)) }

    val red = AndroidColor.red(color)
    val green = AndroidColor.green(color)
    val blue = AndroidColor.blue(color)
    val alpha = AndroidColor.alpha(color)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 18.dp)
    ) {
        PickerSliderRow(
            label = stringResource(R.string.colorPickerDialog_red),
            value = red,
            max = 255,
            trackColors = listOf(redColor),
            thumbColor = redColor,
            valueText = red.toString(),
            onValueChange = { onColorPicked(AndroidColor.argb(alpha, it, green, blue)) }
        )
        PickerSliderRow(
            label = stringResource(R.string.colorPickerDialog_green),
            value = green,
            max = 255,
            trackColors = listOf(greenColor),
            thumbColor = greenColor,
            valueText = green.toString(),
            onValueChange = { onColorPicked(AndroidColor.argb(alpha, red, it, blue)) }
        )
        PickerSliderRow(
            label = stringResource(R.string.colorPickerDialog_blue),
            value = blue,
            max = 255,
            trackColors = listOf(blueColor),
            thumbColor = blueColor,
            valueText = blue.toString(),
            onValueChange = { onColorPicked(AndroidColor.argb(alpha, red, green, it)) }
        )
        if (alphaEnabled) {
            AlphaSliderRow(
                alpha = alpha,
                onAlphaChange = { onColorPicked(AndroidColor.argb(it, red, green, blue)) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RgbPickerPagePreview() {
    RgbPickerPage(
        color = 0xFF6750A4.toInt(),
        alphaEnabled = true,
        onColorPicked = {}
    )
}

package me.jfenn.colorpickerdialog.compose.pickers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Locale
import android.graphics.Color as AndroidColor

// Hue, saturation, brightness slider rows (26dp/18dp padding); hue track
// shows color wheel, other tracks re-derive from current hue; neutral thumbs;
// optional alpha row.
@Composable
internal fun HsvPickerPage(
    color: Int,
    alphaEnabled: Boolean,
    onColorPicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val neutral = remember(context) { Color(PickerColors.neutral(context)) }

    val initialHsv = remember {
        FloatArray(3).also { AndroidColor.colorToHSV(color, it) }
    }
    var hue by remember { mutableIntStateOf(initialHsv[0].toInt()) }
    var saturation by remember { mutableIntStateOf((initialHsv[1] * 255).toInt()) }
    var brightness by remember { mutableIntStateOf((initialHsv[2] * 255).toInt()) }
    var alpha by remember { mutableIntStateOf(AndroidColor.alpha(color)) }

    fun currentColor(): Int {
        val rgb = AndroidColor.HSVToColor(
            floatArrayOf(hue.toFloat(), saturation / 255f, brightness / 255f)
        )
        return (alpha shl 24) or (rgb and 0x00FFFFFF)
    }

    // External color changes re-seed sliders; guarded so own emissions don't
    // reset hue on degenerate colors.
    LaunchedEffect(color) {
        if (currentColor() != color) {
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(color, hsv)
            hue = hsv[0].toInt()
            saturation = (hsv[1] * 255).toInt()
            brightness = (hsv[2] * 255).toInt()
            alpha = AndroidColor.alpha(color)
        }
    }

    val hueTrack = remember { PickerColors.colorWheelArr(1f, 1f).map { Color(it) } }
    val saturationTrack = remember(hue) {
        listOf(
            Color(AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), 0f, 1f))),
            Color(AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 1f)))
        )
    }
    val brightnessTrack = remember(hue) {
        listOf(
            Color(AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 0f))),
            Color(AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 1f)))
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 18.dp)
    ) {
        PickerSliderRow(
            label = stringResource(R.string.colorPickerDialog_hue),
            value = hue,
            max = 360,
            trackColors = hueTrack,
            thumbColor = neutral,
            valueText = hue.toString(),
            onValueChange = {
                hue = it
                onColorPicked(currentColor())
            }
        )
        PickerSliderRow(
            label = stringResource(R.string.colorPickerDialog_saturation),
            value = saturation,
            max = 255,
            trackColors = saturationTrack,
            thumbColor = neutral,
            valueText = String.format(Locale.getDefault(), "%.2f", saturation / 255f),
            onValueChange = {
                saturation = it
                onColorPicked(currentColor())
            }
        )
        PickerSliderRow(
            label = stringResource(R.string.colorPickerDialog_brightness),
            value = brightness,
            max = 255,
            trackColors = brightnessTrack,
            thumbColor = neutral,
            valueText = String.format(Locale.getDefault(), "%.2f", brightness / 255f),
            onValueChange = {
                brightness = it
                onColorPicked(currentColor())
            }
        )
        if (alphaEnabled) {
            AlphaSliderRow(
                alpha = alpha,
                onAlphaChange = {
                    alpha = it
                    onColorPicked(currentColor())
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HsvPickerPagePreview() {
    HsvPickerPage(
        color = 0xFF6750A4.toInt(),
        alphaEnabled = true,
        onColorPicked = {}
    )
}

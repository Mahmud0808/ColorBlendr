package me.jfenn.colorpickerdialog.compose.pickers

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jfenn.colorpickerdialog.R
import me.jfenn.colorpickerdialog.compose.components.AlphaSliderRow
import me.jfenn.colorpickerdialog.compose.components.PickerSliderRow
import me.jfenn.colorpickerdialog.compose.theme.PickerColors
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import android.graphics.Color as AndroidColor

// Hue/saturation wheel: hue = angle, saturation = distance from center;
// brightness on slider below. Same external-change reseeding as HSV page.
@Composable
internal fun WheelPickerPage(
    color: Int,
    alphaEnabled: Boolean,
    onColorPicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val neutral = remember(context) { Color(PickerColors.neutral(context)) }
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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

    val wheelColors = remember {
        PickerColors.colorWheelArr(1f, 1f).map { Color(it) }
    }
    val brightnessTrack = remember(hue, saturation) {
        listOf(
            Color(AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), saturation / 255f, 0f))),
            Color(AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), saturation / 255f, 1f)))
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 18.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(if (isLandscape) 140.dp else 220.dp)
                .padding(bottom = 12.dp)
                .pointerInput(Unit) {
                    fun update(position: Offset) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = position.x - center.x
                        val dy = position.y - center.y
                        val radius = size.width / 2f

                        hue = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 360) % 360).toInt()
                        saturation = ((hypot(dx, dy) / radius).coerceIn(0f, 1f) * 255).toInt()
                        onColorPicked(currentColor())
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        update(down.position)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()
                            if (!change.pressed) break
                            change.consume()
                            update(change.position)
                        }
                    }
                }
        ) {
            val radius = size.minDimension / 2f

            // Hue around wheel, saturation fades to white at center.
            drawCircle(brush = Brush.sweepGradient(wheelColors), radius = radius)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White, Color.White.copy(alpha = 0f))
                ),
                radius = radius
            )

            val angle = Math.toRadians(hue.toDouble()).toFloat()
            val distance = saturation / 255f * radius
            val thumbCenter = center + Offset(cos(angle), sin(angle)) * distance
            val thumbColor = AndroidColor.HSVToColor(
                floatArrayOf(hue.toFloat(), saturation / 255f, 1f)
            )

            drawCircle(
                color = Color(thumbColor),
                radius = 10.dp.toPx(),
                center = thumbCenter
            )
            drawCircle(
                color = if (PickerColors.isColorDark(thumbColor)) Color.White else Color.Black,
                radius = 10.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 2.dp.toPx())
            )
        }

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
private fun WheelPickerPagePreview() {
    WheelPickerPage(
        color = 0xFF6750A4.toInt(),
        alphaEnabled = true,
        onColorPicked = {}
    )
}

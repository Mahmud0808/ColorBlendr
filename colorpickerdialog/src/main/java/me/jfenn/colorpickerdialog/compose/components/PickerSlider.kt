package me.jfenn.colorpickerdialog.compose.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.jfenn.colorpickerdialog.R
import me.jfenn.colorpickerdialog.compose.theme.PickerColors
import java.util.Locale
import kotlin.math.roundToInt

// Decelerate easing for progress animations.
internal val DecelerateEasing = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

// Slider row: 48dp tall, label(1) : slider(4) : value(1) weights, 14sp
// medium secondary text, M3-style 12dp rounded track + handle thumb.
@Composable
internal fun PickerSliderRow(
    label: String,
    value: Int,
    max: Int,
    trackColors: List<Color>,
    thumbColor: Color,
    valueText: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    thumb: PickerThumb = PickerThumb.Pill,
    onTrackingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val textColor = remember(context) { Color(PickerColors.textColorSecondary(context)) }
    val surfaceColor = remember(context) { Color(PickerColors.colorSurface(context)) }
    val textStyle = TextStyle(
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = label,
            style = textStyle,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        SliderTrack(
            value = value,
            max = max,
            trackColors = trackColors,
            thumbColor = thumbColor,
            outlineColor = surfaceColor,
            thumb = thumb,
            onValueChange = onValueChange,
            onTrackingChanged = onTrackingChanged,
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight()
        )

        Text(
            text = valueText,
            style = textStyle,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SliderTrack(
    value: Int,
    max: Int,
    trackColors: List<Color>,
    thumbColor: Color,
    outlineColor: Color,
    thumb: PickerThumb,
    onValueChange: (Int) -> Unit,
    onTrackingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isTracking by remember { mutableStateOf(false) }
    val progress = remember { Animatable(value.toFloat()) }
    val currentMax by rememberUpdatedState(max)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnTrackingChanged by rememberUpdatedState(onTrackingChanged)

    // External color changes animate thumb; user drags snap directly.
    LaunchedEffect(value) {
        if (isTracking) {
            progress.snapTo(value.toFloat())
        } else if (progress.targetValue.roundToInt() != value) {
            progress.animateTo(
                targetValue = value.toFloat(),
                animationSpec = tween(durationMillis = 300, easing = DecelerateEasing)
            )
        }
    }

    val brush = remember(trackColors) {
        if (trackColors.size < 2) {
            Brush.horizontalGradient(listOf(trackColors.first(), trackColors.first()))
        } else {
            Brush.horizontalGradient(trackColors)
        }
    }

    var trackWidth by remember { mutableStateOf(0f) }

    fun valueAt(x: Float): Int {
        if (trackWidth <= 0f) return value
        val fraction = (x / trackWidth).coerceIn(0f, 1f)
        return (fraction * currentMax).roundToInt()
    }

    Spacer(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isTracking = true
                        currentOnTrackingChanged(true)
                    },
                    onTap = { offset ->
                        currentOnValueChange(valueAt(offset.x))
                        isTracking = false
                        currentOnTrackingChanged(false)
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isTracking = true
                        currentOnTrackingChanged(true)
                    },
                    onDragEnd = {
                        isTracking = false
                        currentOnTrackingChanged(false)
                    },
                    onDragCancel = {
                        isTracking = false
                        currentOnTrackingChanged(false)
                    }
                ) { change, _ ->
                    change.consume()
                    currentOnValueChange(valueAt(change.position.x))
                }
            }
            .drawBehind {
                trackWidth = size.width

                // M3-style full-height gradient track.
                val trackHeight = 12.dp.toPx()
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(0f, center.y - trackHeight / 2f),
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f)
                )

                val fraction = if (currentMax > 0) {
                    (progress.value / currentMax).coerceIn(0f, 1f)
                } else {
                    0f
                }

                when (thumb) {
                    PickerThumb.Pill -> {
                        // M3 expressive handle: narrow vertical bar taller
                        // than the track; surface-colored outline separates
                        // it from the gradient.
                        val thumbWidth = 6.dp.toPx()
                        val thumbHeight = 28.dp.toPx()
                        val outline = 3.dp.toPx()
                        val centerX = thumbWidth / 2f + outline +
                                fraction * (size.width - thumbWidth - outline * 2f)
                        drawRoundRect(
                            color = outlineColor,
                            topLeft = Offset(
                                centerX - thumbWidth / 2f - outline,
                                center.y - thumbHeight / 2f - outline
                            ),
                            size = Size(
                                thumbWidth + outline * 2f,
                                thumbHeight + outline * 2f
                            ),
                            cornerRadius = CornerRadius(thumbWidth / 2f + outline)
                        )
                        drawRoundRect(
                            color = thumbColor,
                            topLeft = Offset(
                                centerX - thumbWidth / 2f,
                                center.y - thumbHeight / 2f
                            ),
                            size = Size(thumbWidth, thumbHeight),
                            cornerRadius = CornerRadius(thumbWidth / 2f)
                        )
                    }

                    PickerThumb.Circle -> {
                        // Circle thumb for alpha row; same outline treatment.
                        val radius = 9.dp.toPx()
                        val outline = 3.dp.toPx()
                        val centerX = radius + outline +
                                fraction * (size.width - (radius + outline) * 2f)
                        drawCircle(
                            color = outlineColor,
                            radius = radius + outline,
                            center = Offset(centerX, center.y)
                        )
                        drawCircle(
                            color = thumbColor,
                            radius = radius,
                            center = Offset(centerX, center.y)
                        )
                    }
                }
            }
    )
}

// Alpha row: max 255, "%.2f" of progress/255, neutral track, circle thumb.
@Composable
internal fun AlphaSliderRow(
    alpha: Int,
    onAlphaChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onTrackingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val neutral = remember(context) { Color(PickerColors.neutral(context)) }

    PickerSliderRow(
        label = stringResource(R.string.colorPickerDialog_alpha),
        value = alpha,
        max = 255,
        trackColors = listOf(neutral),
        thumbColor = neutral,
        thumb = PickerThumb.Circle,
        valueText = String.format(Locale.getDefault(), "%.2f", alpha / 255f),
        onValueChange = onAlphaChange,
        onTrackingChanged = onTrackingChanged,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun PickerSliderRowPreview() {
    PickerSliderRow(
        label = "H",
        value = 180,
        max = 360,
        trackColors = PickerColors.colorWheelArr(1f, 1f).map { Color(it) },
        thumbColor = Color(0xFF6750A4),
        valueText = "180",
        onValueChange = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun AlphaSliderRowPreview() {
    AlphaSliderRow(
        alpha = 255,
        onAlphaChange = {}
    )
}

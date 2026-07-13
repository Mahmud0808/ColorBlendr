package com.drdisagree.colorblendr.ui.compose.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import java.text.DecimalFormat
import android.R as AndroidR

// Whole card = track; progress fill = 24dp rounded rect clipped
// horizontally.
@Composable
fun SeekbarItem(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    minValue: Int = 0,
    maxValue: Int = 100,
    defaultValue: Int? = null,
    onReset: (() -> Unit)? = null,
    valueFormat: String = "",
    isDecimalFormat: Boolean = false,
    decimalFormat: String = "#.#",
    outputScale: Float = 1f,
    enabled: Boolean = true,
    disabledReason: String? = null,
    position: WidgetPosition = WidgetPosition.Single
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val fraction = if (maxValue > minValue) {
        (value.coerceIn(minValue, maxValue) - minValue).toFloat() / (maxValue - minValue)
    } else {
        0f
    }

    val fillColor = if (enabled) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    } else {
        themeAttrColor(AndroidR.attr.textColorPrimary).copy(alpha = 0.05f)
    }
    val textIconColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val cornerRadius = with(LocalDensity.current) {
        dimensionResource(R.dimen.default_corner_radius).toPx()
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val formattedValue = if (valueFormat.isBlank()) {
        stringResource(
            R.string.opt_selected1,
            if (!isDecimalFormat) {
                (value / outputScale).toInt().toString()
            } else {
                DecimalFormat(decimalFormat).format((value / outputScale).toDouble())
            }
        )
    } else {
        stringResource(
            R.string.opt_selected1,
            stringResource(
                R.string.opt_selected2,
                if (!isDecimalFormat) {
                    value.toString()
                } else {
                    DecimalFormat(decimalFormat).format((value / outputScale).toDouble())
                },
                valueFormat
            )
        )
    }

    PositionedCard(position = position, modifier = modifier) {
        var trackWidth by remember { mutableIntStateOf(0) }

        fun progressAt(x: Float): Int {
            if (trackWidth <= 0) return value
            val raw = (x / trackWidth).coerceIn(0f, 1f)
            val fromStart = if (isRtl) 1f - raw else raw
            return (minValue + fromStart * (maxValue - minValue) + 0.5f).toInt()
                .coerceIn(minValue, maxValue)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = value.toFloat(),
                        range = minValue.toFloat()..maxValue.toFloat()
                    )
                }
                .pointerInput(enabled, minValue, maxValue) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onTap = { offset ->
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onValueChange(progressAt(offset.x))
                            onValueChangeFinished?.invoke()
                        }
                    )
                }
                .pointerInput(enabled, minValue, maxValue) {
                    if (!enabled) return@pointerInput
                    var lastTickBucket = Int.MIN_VALUE
                    detectHorizontalDragGestures(
                        onDragEnd = { onValueChangeFinished?.invoke() },
                        onDragCancel = { onValueChangeFinished?.invoke() }
                    ) { change, _ ->
                        change.consume()
                        val newValue = progressAt(change.position.x)
                        val tickBucket = newValue / 10
                        if (tickBucket != lastTickBucket) {
                            if (lastTickBucket != Int.MIN_VALUE) {
                                haptics.performHapticFeedback(
                                    HapticFeedbackType.SegmentFrequentTick
                                )
                            }
                            lastTickBucket = tickBucket
                        }
                        onValueChange(newValue)
                    }
                }
                .drawBehind {
                    trackWidth = size.width.toInt()
                    val fillWidth = size.width * fraction
                    val clipLeft = if (isRtl) size.width - fillWidth else 0f
                    val clipRight = if (isRtl) size.width else fillWidth
                    clipRect(left = clipLeft, right = clipRight) {
                        drawRoundRect(
                            color = fillColor,
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                TitleWithBadge(
                    title = title,
                    badge = if (enabled) null else disabledReason,
                    textStyle = MaterialTheme.typography.titleSmall,
                    textColor = textIconColor.copy(alpha = if (enabled) 1f else 0.5f)
                )
                Text(
                    text = formattedValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = textIconColor.copy(alpha = if (enabled) 0.8f else 0.3f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (defaultValue != null && value != defaultValue) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(start = 16.dp, end = 22.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.RestartAlt),
                        contentDescription = stringResource(R.string.reset),
                        tint = textIconColor,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.reset_icon_size))
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false),
                                enabled = enabled,
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        R.string.long_press_to_reset,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onValueChange(defaultValue)
                                    onReset?.invoke()
                                }
                            )
                            .alpha(if (enabled) 0.4f else 0.5f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SeekbarItemPreview() {
    ColorBlendrTheme {
        Column {
            var value by remember { mutableFloatStateOf(75f) }
            SeekbarItem(
                title = "Accent saturation",
                value = value.toInt(),
                onValueChange = { value = it.toFloat() },
                defaultValue = 100,
                position = WidgetPosition.Top
            )
            SeekbarItem(
                title = "Background lightness",
                value = 50,
                onValueChange = {},
                enabled = false,
                disabledReason = "Requires root",
                position = WidgetPosition.Bottom
            )
        }
    }
}

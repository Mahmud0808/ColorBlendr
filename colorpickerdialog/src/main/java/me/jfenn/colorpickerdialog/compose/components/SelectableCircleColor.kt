package me.jfenn.colorpickerdialog.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jfenn.colorpickerdialog.compose.theme.PickerColors
import android.graphics.Color as AndroidColor

private val CheckerSquareColor = Color(0xFFCCCCCC) // Color.LTGRAY

// Compose port of colorpicker_item_color.xml + SelectableCircleColorView:
// square cell with borderless ripple and 8dp margin around a circle swatch;
// selection scales the circle 0.8 -> 1.0 with decelerate easing; the outline
// is a 2dp auto-contrast stroke; translucent colors sit on a circular
// checkerboard.
@Composable
internal fun SelectableCircleColor(
    color: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val neutral = remember(context) { PickerColors.neutral(context) }

    val outlineColor = remember(color, neutral) {
        if (PickerColors.isColorDark(neutral)) {
            if (PickerColors.isColorDark(color)) color else neutral
        } else {
            if (PickerColors.isColorDark(color)) neutral else color
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.8f,
        animationSpec = tween(easing = DecelerateEasing),
        label = "swatchScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick
            )
            .padding(8.dp)
            .drawBehind {
                val radius = size.minDimension / 2f * scale
                val strokeWidth = 2.dp.toPx()

                if (AndroidColor.alpha(color) < 255) {
                    drawCheckerCircle(radius)
                }

                drawCircle(color = Color(color), radius = radius)
                drawCircle(
                    color = Color(outlineColor),
                    radius = radius - strokeWidth / 2f,
                    style = Stroke(width = strokeWidth)
                )
            }
    )
}

// SelectableCircleColorView alpha grid: 8dp LTGRAY squares clipped to the
// swatch circle.
private fun DrawScope.drawCheckerCircle(radius: Float) {
    val square = 8.dp.toPx()
    val circle = Path().apply {
        addOval(Rect(center = center, radius = radius))
    }

    clipPath(circle) {
        var x = 0f
        var column = 0
        while (x < size.width) {
            var y = if (column % 2 == 0) 0f else square
            while (y < size.height) {
                drawRect(
                    color = CheckerSquareColor,
                    topLeft = Offset(x, y),
                    size = Size(square, square)
                )
                y += square * 2
            }
            x += square
            column++
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectableCircleColorPreview() {
    Row(modifier = Modifier.fillMaxWidth()) {
        SelectableCircleColor(
            color = 0xFFF44336.toInt(),
            selected = true,
            onClick = {},
            modifier = Modifier.size(56.dp)
        )
        SelectableCircleColor(
            color = 0xFF2196F3.toInt(),
            selected = false,
            onClick = {},
            modifier = Modifier.size(56.dp)
        )
        SelectableCircleColor(
            color = 0x804CAF50.toInt(),
            selected = false,
            onClick = {},
            modifier = Modifier.size(56.dp)
        )
    }
}

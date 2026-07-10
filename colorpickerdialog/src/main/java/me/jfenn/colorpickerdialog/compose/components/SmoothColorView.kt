package me.jfenn.colorpickerdialog.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.min

// Compose port of SmoothColorView + AlphaColorDrawable: draws the color over
// an 8dp checkered tile pattern while it is translucent; color changes
// crossfade in 100ms (TransitionDrawable parity), or snap when animate=false.

private val CheckerSquare = Color(0xFFCCCCCC) // Color.LTGRAY

@Composable
fun SmoothColorView(
    color: Int,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val displayedColor by animateColorAsState(
        targetValue = Color(color),
        animationSpec = if (animate) tween(durationMillis = 100) else snap(),
        label = "smoothColor"
    )

    Spacer(
        modifier = modifier.drawBehind {
            drawCheckeredColor(displayedColor)
        }
    )
}

// Height is always 56.25% of the available width (HorizontalSmoothColorView).
@Composable
fun HorizontalSmoothColorView(
    color: Int,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    SmoothColorView(
        color = color,
        animate = animate,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / 0.5625f)
    )
}

// Width matches the available height, capped at 200dp (VerticalSmoothColorView).
@Composable
fun VerticalSmoothColorView(
    color: Int,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    SmoothColorView(
        color = color,
        animate = animate,
        modifier = modifier
            .fillMaxHeight()
            .layout { measurable, constraints ->
                // Height can be unbounded during intrinsic measurement.
                val cap = 200.dp.roundToPx()
                val height = if (constraints.hasBoundedHeight) constraints.maxHeight else cap
                val width = min(cap, height)
                val placeable = measurable.measure(Constraints.fixed(width, height))
                layout(width, height) { placeable.place(0, 0) }
            }
    )
}

// AlphaColorDrawable parity: white base with staggered LTGRAY squares, only
// visible while the color has transparency.
internal fun DrawScope.drawCheckeredColor(color: Color) {
    if (color.alpha < 1f) {
        val tile = 8.dp.toPx()

        drawRect(Color.White)

        var x = 0f
        var column = 0
        while (x < size.width) {
            var y = if (column % 2 == 0) 0f else tile
            while (y < size.height) {
                drawRect(
                    color = CheckerSquare,
                    topLeft = Offset(x, y),
                    size = Size(tile, tile)
                )
                y += tile * 2
            }
            x += tile
            column++
        }
    }

    drawRect(color)
}

@Preview
@Composable
private fun SmoothColorViewPreview() {
    SmoothColorView(
        color = 0x806750A4.toInt(),
        modifier = Modifier.size(200.dp, 64.dp)
    )
}

@Preview
@Composable
private fun HorizontalSmoothColorViewPreview() {
    HorizontalSmoothColorView(
        color = 0xFF6750A4.toInt(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(heightDp = 300)
@Composable
private fun VerticalSmoothColorViewPreview() {
    VerticalSmoothColorView(
        color = 0xFF6750A4.toInt(),
        modifier = Modifier.height(300.dp)
    )
}

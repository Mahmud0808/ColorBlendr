package me.jfenn.colorpickerdialog.compose.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import me.jfenn.colorpickerdialog.compose.theme.PickerColors
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

// Touch sampler with pinch zoom/pan + floating loupe: one finger samples,
// magnified preview shows above finger (ringed with sampled color) so finger
// never hides pick; two fingers zoom/pan. Color commits on finger up, alpha
// forced opaque.
@Composable
internal fun ImageColorPicker(
    bitmap: Bitmap,
    onColorPicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    var touchPosition by remember(bitmap) { mutableStateOf<Offset?>(null) }
    // Content-space position of last committed pick; marker stays on same
    // pixel through zoom/pan.
    var pickedPosition by remember(bitmap) { mutableStateOf<Offset?>(null) }
    var pickedColor by remember(bitmap) { mutableStateOf<Int?>(null) }
    var viewSize by remember(bitmap) { mutableStateOf(IntSize.Zero) }

    // Container position -> untransformed content position (graphicsLayer
    // scales/translates around center).
    fun toContentPosition(position: Offset): Offset {
        val center = Offset(viewSize.width / 2f, viewSize.height / 2f)
        return center + (position - center - offset) / scale
    }

    fun pixelAt(position: Offset): Int? {
        if (viewSize.width <= 0 || viewSize.height <= 0) return null
        val content = toContentPosition(position)
        val bitmapX = (content.x * bitmap.width / viewSize.width).toInt()
        val bitmapY = (content.y * bitmap.height / viewSize.height).toInt()
        if (bitmapX !in 0 until bitmap.width || bitmapY !in 0 until bitmap.height) return null

        val pixel = bitmap.getPixel(bitmapX, bitmapY)
        return AndroidColor.argb(
            255,
            AndroidColor.red(pixel),
            AndroidColor.green(pixel),
            AndroidColor.blue(pixel)
        )
    }

    fun clampOffset(candidate: Offset): Offset {
        val maxX = viewSize.width * (scale - 1f) / 2f
        val maxY = viewSize.height * (scale - 1f) / 2f
        return Offset(
            candidate.x.coerceIn(-maxX, maxX),
            candidate.y.coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(bitmap.width.toFloat() / bitmap.height)
            .clipToBounds()
            .pointerInput(bitmap) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    var isTransforming = false
                    var last = down.position
                    touchPosition = last

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }

                        when {
                            pressed.size >= 2 -> {
                                // Pinch: zoom around centroid + pan.
                                isTransforming = true
                                touchPosition = null

                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val centroid = event.calculateCentroid()
                                val center = Offset(
                                    viewSize.width / 2f,
                                    viewSize.height / 2f
                                )

                                val newScale = (scale * zoom).coerceIn(1f, 8f)
                                val zoomFactor = newScale / scale
                                offset = clampOffset(
                                    offset - (centroid - center - offset) *
                                            (zoomFactor - 1f) + pan
                                )
                                scale = newScale

                                event.changes.forEach { it.consume() }
                            }

                            pressed.size == 1 && !isTransforming -> {
                                last = pressed.first().position
                                touchPosition = last
                                pressed.first().consume()
                            }

                            pressed.isEmpty() -> {
                                if (!isTransforming) {
                                    pixelAt(last)?.let { sampled ->
                                        pickedPosition = toContentPosition(last)
                                        pickedColor = sampled
                                        onColorPicked(sampled)
                                    }
                                }
                                touchPosition = null
                                break
                            }
                        }
                    }
                }
            }
            .drawWithContent {
                viewSize = IntSize(size.width.toInt(), size.height.toInt())
                drawContent()

                // Single marker: follows finger while sampling, then stays
                // on picked pixel through zoom/pan.
                val touch = touchPosition
                val markerCenter: Offset?
                val markerColor: Int?
                if (touch != null) {
                    markerCenter = touch
                    markerColor = pixelAt(touch)
                } else {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    markerCenter = pickedPosition?.let { center + (it - center) * scale + offset }
                    markerColor = pickedColor
                }

                if (markerCenter != null && markerColor != null) {
                    drawCircle(
                        color = Color(markerColor),
                        radius = 18.dp.toPx(),
                        center = markerCenter
                    )
                    drawCircle(
                        color = if (PickerColors.isColorDark(markerColor)) {
                            Color.White
                        } else {
                            Color.Black
                        },
                        radius = 18.dp.toPx(),
                        center = markerCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                if (touch != null && markerColor != null) {
                    drawLoupe(
                        image = imageBitmap,
                        bitmap = bitmap,
                        touch = touch,
                        contentPosition = toContentPosition(touch),
                        sampledColor = markerColor,
                        viewScale = size.width / bitmap.width * scale
                    )
                }
            }
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

// Floating magnifier above finger: circle previews pixels around sample
// point at 2x current zoom, ringed with sampled color + contrast outline.
private fun DrawScope.drawLoupe(
    image: ImageBitmap,
    bitmap: Bitmap,
    touch: Offset,
    contentPosition: Offset,
    sampledColor: Int,
    viewScale: Float
) {
    val loupeRadius = 40.dp.toPx()
    val fingerGap = 84.dp.toPx()
    val contrast = if (PickerColors.isColorDark(sampledColor)) Color.White else Color.Black

    // Above finger; below when no room.
    val rawCenter = if (touch.y - fingerGap - loupeRadius >= 0f) {
        Offset(touch.x, touch.y - fingerGap)
    } else {
        Offset(touch.x, touch.y + fingerGap)
    }
    val loupeCenter = Offset(
        rawCenter.x.coerceIn(loupeRadius, size.width - loupeRadius),
        rawCenter.y.coerceIn(loupeRadius, size.height - loupeRadius)
    )

    // Bitmap region shown inside loupe (2x magnification over current
    // on-screen zoom).
    val magnification = viewScale * 2f
    val srcRadius = (loupeRadius / magnification).coerceAtLeast(2f)
    val bitmapX = contentPosition.x * bitmap.width / size.width
    val bitmapY = contentPosition.y * bitmap.height / size.height

    val srcSize = (srcRadius * 2).roundToInt()
        .coerceAtMost(bitmap.width)
        .coerceAtMost(bitmap.height)
    val srcX = (bitmapX - srcSize / 2f).roundToInt()
        .coerceIn(0, bitmap.width - srcSize)
    val srcY = (bitmapY - srcSize / 2f).roundToInt()
        .coerceIn(0, bitmap.height - srcSize)

    val loupePath = Path().apply {
        addOval(Rect(center = loupeCenter, radius = loupeRadius))
    }

    clipPath(loupePath) {
        drawImage(
            image = image,
            srcOffset = IntOffset(srcX, srcY),
            srcSize = IntSize(srcSize, srcSize),
            dstOffset = IntOffset(
                (loupeCenter.x - loupeRadius).roundToInt(),
                (loupeCenter.y - loupeRadius).roundToInt()
            ),
            dstSize = IntSize(
                (loupeRadius * 2).roundToInt(),
                (loupeRadius * 2).roundToInt()
            ),
            filterQuality = FilterQuality.None
        )
    }

    // Crosshair at loupe center, sampled-color ring, contrast outline.
    drawCircle(
        color = contrast,
        radius = 4.dp.toPx(),
        center = loupeCenter,
        style = Stroke(width = 1.5f.dp.toPx())
    )
    drawCircle(
        color = Color(sampledColor),
        radius = loupeRadius,
        center = loupeCenter,
        style = Stroke(width = 4.dp.toPx())
    )
    drawCircle(
        color = contrast,
        radius = loupeRadius + 2.5f.dp.toPx(),
        center = loupeCenter,
        style = Stroke(width = 1.dp.toPx())
    )
}

@Preview
@Composable
private fun ImageColorPickerPreview() {
    val bitmap = remember {
        createBitmap(160, 90).apply {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    setPixel(x, y, AndroidColor.HSVToColor(floatArrayOf(x * 360f / width, 1f, 1f)))
                }
            }
        }
    }
    ImageColorPicker(
        bitmap = bitmap,
        onColorPicked = {},
        modifier = Modifier.size(320.dp, 180.dp)
    )
}

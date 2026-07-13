package me.jfenn.colorpickerdialog.compose.dialogs

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.jfenn.colorpickerdialog.R
import me.jfenn.colorpickerdialog.compose.components.HorizontalSmoothColorView
import me.jfenn.colorpickerdialog.compose.components.SmoothColorView
import me.jfenn.colorpickerdialog.compose.components.VerticalSmoothColorView
import me.jfenn.colorpickerdialog.compose.theme.PickerColors
import me.jfenn.colorpickerdialog.compose.pickers.DEFAULT_PRESETS
import me.jfenn.colorpickerdialog.compose.pickers.HsvPickerPage
import me.jfenn.colorpickerdialog.compose.pickers.ImagePickerPage
import me.jfenn.colorpickerdialog.compose.pickers.PresetPickerPage
import me.jfenn.colorpickerdialog.compose.pickers.RgbPickerPage
import me.jfenn.colorpickerdialog.compose.pickers.WheelPickerPage
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

// Color band with centered hex field, tabbed pickers (tab clicks only, no
// swipe), 64dp M3 expressive button bar. Portrait stacks band on top;
// landscape puts vertical band on left. M3 dialog corners (28dp).
@Composable
fun ColorPickerDialog(
    initialColor: Int,
    onDismissRequest: () -> Unit,
    onColorPicked: (Int) -> Unit,
    alphaEnabled: Boolean = true,
    pickers: List<ColorPickerType> = listOf(ColorPickerType.RGB, ColorPickerType.HSV),
    presets: List<Int> = DEFAULT_PRESETS,
    cornerRadius: Dp = 2.dp
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var color by rememberSaveable { mutableIntStateOf(initialColor) }
    var animateHeader by remember { mutableStateOf(false) }
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }
    var sampleImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    fun updateColor(newColor: Int, animate: Boolean) {
        color = newColor
        animateHeader = animate
    }

    // Window width: min(500dp portrait / 800dp landscape, 90% of screen);
    // card corner = max(requested radius, M3 dialog 28dp).
    val dialogWidth = min(
        if (isLandscape) 800 else 500,
        (configuration.screenWidthDp * 0.9f).toInt()
    ).dp
    val effectiveRadius = maxOf(cornerRadius, 28.dp)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(effectiveRadius),
            color = Color(PickerColors.colorSurface(context)),
            modifier = Modifier.width(dialogWidth)
        ) {
            if (isLandscape) {
                // Color band sized from measured picker column: width =
                // min(200dp, column height).
                Layout(
                    content = {
                        Box(contentAlignment = Alignment.Center) {
                            SmoothColorView(
                                color = color,
                                animate = animateHeader,
                                modifier = Modifier.fillMaxSize()
                            )
                            HexField(
                                color = color,
                                alphaEnabled = alphaEnabled,
                                onColorParsed = { updateColor(it, animate = false) }
                            )
                        }
                        Column {
                            PickerTabs(
                                pickers = pickers,
                                selectedPage = selectedPage,
                                onPageSelected = { selectedPage = it }
                            )
                            PickerPages(
                                pickers = pickers,
                                selectedPage = selectedPage,
                                color = color,
                                alphaEnabled = alphaEnabled,
                                presets = presets,
                                onColorPicked = { updateColor(it, animate = false) },
                                onImageSelected = { sampleImageUri = it }
                            )
                            DialogButtons(
                                onCancel = onDismissRequest,
                                onConfirm = { onColorPicked(color) }
                            )
                        }
                    }
                ) { measurables, constraints ->
                    val bandCap = 200.dp.roundToPx()
                    val pickerColumn = measurables[1].measure(
                        constraints.copy(
                            minWidth = 0,
                            minHeight = 0,
                            maxWidth = (constraints.maxWidth - bandCap).coerceAtLeast(0)
                        )
                    )
                    val bandWidth = min(bandCap, pickerColumn.height)
                    val band = measurables[0].measure(
                        Constraints.fixed(bandWidth, pickerColumn.height)
                    )

                    layout(bandWidth + pickerColumn.width, pickerColumn.height) {
                        band.place(0, 0)
                        pickerColumn.place(bandWidth, 0)
                    }
                }
            } else {
                Column {
                    Box(contentAlignment = Alignment.Center) {
                        HorizontalSmoothColorView(
                            color = color,
                            animate = animateHeader,
                            modifier = Modifier.fillMaxWidth()
                        )
                        HexField(
                            color = color,
                            alphaEnabled = alphaEnabled,
                            onColorParsed = { updateColor(it, animate = false) }
                        )
                    }
                    PickerTabs(
                        pickers = pickers,
                        selectedPage = selectedPage,
                        onPageSelected = { selectedPage = it }
                    )
                    PickerPages(
                        pickers = pickers,
                        selectedPage = selectedPage,
                        color = color,
                        alphaEnabled = alphaEnabled,
                        presets = presets,
                        onColorPicked = { updateColor(it, animate = false) },
                        onImageSelected = { sampleImageUri = it }
                    )
                    DialogButtons(
                        onCancel = onDismissRequest,
                        onConfirm = { onColorPicked(color) }
                    )
                }
            }
        }
    }

    sampleImageUri?.let { uri ->
        ImageColorPickerDialog(
            imageUri = uri,
            initialColor = color,
            onDismissRequest = { sampleImageUri = null },
            onColorPicked = { sampled ->
                sampleImageUri = null
                updateColor(sampled, animate = true)
            }
        )
    }
}

// Centered hex field: 24sp, hash + hex digits only, 7/9 chars by alpha,
// caps, contrast text + cursor; complete valid value updates color;
// programmatic color changes rewrite text.
@Composable
private fun HexField(
    color: Int,
    alphaEnabled: Boolean,
    onColorParsed: (Int) -> Unit
) {
    val length = if (alphaEnabled) 9 else 7
    val formatted = if (alphaEnabled) {
        String.format("#%08X", color)
    } else {
        String.format("#%06X", 0xFFFFFF and color)
    }

    var text by remember { mutableStateOf(formatted) }
    var lastColor by remember { mutableIntStateOf(color) }
    if (lastColor != color) {
        lastColor = color
        text = formatted
    }

    val textColor = if (
        PickerColors.isColorDark(PickerColors.withBackground(color, AndroidColor.WHITE))
    ) {
        Color.White
    } else {
        Color.Black
    }

    BasicTextField(
        value = text,
        onValueChange = { value ->
            val sanitized = "#" + value
                .removePrefix("#")
                .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                .take(length - 1)
                .uppercase(Locale.getDefault())
            text = sanitized

            if (sanitized.length == length) {
                runCatching { AndroidColor.parseColor(sanitized) }
                    .getOrNull()
                    ?.let { parsed ->
                        lastColor = parsed
                        onColorParsed(parsed)
                    }
            }
        },
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(textColor)
    )
}

// M3 expressive connected-button tabs: rounded track, pill slides behind
// selected tab. Tabs get at least equal share of track; overflowing labels
// make row scroll, fading edge only on sides that can still scroll.
@Composable
private fun PickerTabs(
    pickers: List<ColorPickerType>,
    selectedPage: Int,
    onPageSelected: (Int) -> Unit
) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val pillColor = MaterialTheme.colorScheme.secondaryContainer
    val selectedText = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedText = MaterialTheme.colorScheme.onSurfaceVariant

    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val tabBounds = remember(pickers) { mutableStateMapOf<Int, Pair<Float, Float>>() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor)
            .drawWithContent {
                drawContent()

                val fade = 24.dp.toPx()
                val startFade = (scrollState.value / fade).coerceIn(0f, 1f)
                val endFade =
                    ((scrollState.maxValue - scrollState.value) / fade).coerceIn(0f, 1f)

                if (startFade > 0f) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to trackColor.copy(alpha = startFade),
                            1f to trackColor.copy(alpha = 0f),
                            endX = fade
                        )
                    )
                }
                if (endFade > 0f) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to trackColor.copy(alpha = 0f),
                            1f to trackColor.copy(alpha = endFade),
                            startX = size.width - fade
                        )
                    )
                }
            }
    ) {
        val minTabWidth = maxWidth / pickers.size

        // Keep selected tab in view on change.
        LaunchedEffect(selectedPage) {
            tabBounds[selectedPage]?.let { (x, width) ->
                val viewport = constraints.maxWidth.toFloat()
                scrollState.animateScrollTo(
                    (x + width / 2f - viewport / 2f).roundToInt()
                        .coerceIn(0, scrollState.maxValue)
                )
            }
        }

        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            tabBounds[selectedPage]?.let { (x, width) ->
                val pillSpring = spring<Float>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
                val pillX by animateFloatAsState(
                    targetValue = x,
                    animationSpec = pillSpring,
                    label = "pillX"
                )
                val pillWidth by animateFloatAsState(
                    targetValue = width,
                    animationSpec = pillSpring,
                    label = "pillWidth"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(pillX.roundToInt(), 0) }
                        .width(with(density) { pillWidth.toDp() })
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(percent = 50))
                        .background(pillColor)
                )
            }

            Row(modifier = Modifier.fillMaxHeight()) {
                pickers.forEachIndexed { index, picker ->
                    val textColor by animateColorAsState(
                        targetValue = if (selectedPage == index) selectedText else unselectedText,
                        label = "tabTextColor"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .widthIn(min = minTabWidth)
                            .fillMaxHeight()
                            .onGloballyPositioned {
                                tabBounds[index] =
                                    it.positionInParent().x to it.size.width.toFloat()
                            }
                            .clip(RoundedCornerShape(percent = 50))
                            .clickable { onPageSelected(index) }
                    ) {
                        Text(
                            text = stringResource(picker.titleRes()),
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Pages slide horizontally on tab clicks; height snaps to new page; no
// swipe.
@Composable
private fun PickerPages(
    pickers: List<ColorPickerType>,
    selectedPage: Int,
    color: Int,
    alphaEnabled: Boolean,
    presets: List<Int>,
    onColorPicked: (Int) -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    AnimatedContent(
        targetState = selectedPage,
        transitionSpec = {
            val towards = if (targetState > initialState) 1 else -1
            (slideInHorizontally(tween(250)) { it * towards } togetherWith
                    slideOutHorizontally(tween(250)) { -it * towards })
                .using(SizeTransform(clip = true) { _, _ -> snap() })
        },
        label = "pickerPage"
    ) { page ->
        when (pickers.getOrNull(page)) {
            ColorPickerType.WHEEL -> WheelPickerPage(
                color = color,
                alphaEnabled = alphaEnabled,
                onColorPicked = onColorPicked
            )

            ColorPickerType.RGB -> RgbPickerPage(
                color = color,
                alphaEnabled = alphaEnabled,
                onColorPicked = onColorPicked
            )

            ColorPickerType.HSV -> HsvPickerPage(
                color = color,
                alphaEnabled = alphaEnabled,
                onColorPicked = onColorPicked
            )

            ColorPickerType.PRESETS -> PresetPickerPage(
                color = color,
                presets = presets,
                onColorPicked = onColorPicked
            )

            ColorPickerType.IMAGE -> ImagePickerPage(
                onImageSelected = onImageSelected
            )

            null -> Box(modifier = Modifier.fillMaxWidth())
        }
    }
}

// 64dp end-aligned bar: M3 expressive outlined cancel + filled confirm.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DialogButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(text = stringResource(android.R.string.cancel))
        }
        Button(
            onClick = onConfirm,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(text = stringResource(android.R.string.ok))
        }
    }
}

@Preview
@Composable
private fun ColorPickerDialogPreview() {
    ColorPickerDialog(
        initialColor = 0xFF6750A4.toInt(),
        onDismissRequest = {},
        onColorPicked = {},
        pickers = listOf(
            ColorPickerType.RGB,
            ColorPickerType.HSV,
            ColorPickerType.PRESETS,
            ColorPickerType.IMAGE
        )
    )
}

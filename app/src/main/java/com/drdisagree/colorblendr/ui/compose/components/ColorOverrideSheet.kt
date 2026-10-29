package com.drdisagree.colorblendr.ui.compose.components

import androidx.annotation.ColorInt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.horizontalFadingEdges
import com.drdisagree.colorblendr.ui.compose.utils.labelRes
import com.drdisagree.colorblendr.ui.compose.utils.transparencyGrid
import com.drdisagree.colorblendr.utils.colors.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToArgbHexColor
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerType
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorOverrideSheet(
    title: String,
    scheme: ResolvedScheme,
    @ColorInt baseColor: Int?,
    origin: ColorOverride.Shade?,
    override: ColorOverride?,
    onOverride: (ColorOverride?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        ColorOverrideContent(
            title = title,
            scheme = scheme,
            baseColor = baseColor,
            origin = origin,
            override = override,
            onOverride = onOverride,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorOverrideContent(
    title: String,
    scheme: ResolvedScheme,
    @ColorInt baseColor: Int?,
    origin: ColorOverride.Shade?,
    override: ColorOverride?,
    onOverride: (ColorOverride?) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var customMode by rememberSaveable(title) { mutableStateOf(override is ColorOverride.Custom) }
    var role by rememberSaveable(title) {
        mutableStateOf((override as? ColorOverride.Shade)?.role ?: origin?.role ?: PaletteRole.NEUTRAL)
    }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    val baseAlpha = baseColor?.let(AndroidColor::alpha) ?: OPAQUE_ALPHA
    val translucent = baseAlpha < OPAQUE_ALPHA
    @ColorInt val color = override?.let(scheme::colorOf) ?: baseColor
    val subtitle = when (override) {
        is ColorOverride.Shade -> stringResource(
            R.string.color_tokens_from_shade,
            color?.let(::intToArgbHexColor) ?: stringResource(R.string.color_unknown),
            stringResource(override.role.labelRes),
            ShadeScale.shades[override.index]
        )

        else -> color?.let(::intToArgbHexColor) ?: stringResource(R.string.color_unknown)
    }

    if (showPicker) {
        ColorPickerDialog(
            initialColor = color ?: AndroidColor.GRAY,
            onDismissRequest = { showPicker = false },
            onColorPicked = { picked ->
                showPicker = false
                onOverride(ColorOverride.Custom(if (translucent) picked else picked or OPAQUE))
            },
            alphaEnabled = translucent,
            pickers = listOf(
                ColorPickerType.WHEEL,
                ColorPickerType.RGB,
                ColorPickerType.HSV,
                ColorPickerType.IMAGE
            ),
            cornerRadius = 24.dp
        )
    }

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        OverridePreviewTile(
            title = title,
            subtitle = subtitle,
            previewColor = color,
            originalColor = baseColor.takeIf { override != null }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp)
        ) {
            listOf(false, true).forEach { custom ->
                ToggleButton(
                    checked = customMode == custom,
                    onCheckedChange = {
                        if (customMode != custom) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            customMode = custom
                        }
                    }
                ) {
                    Text(
                        text = stringResource(
                            if (custom) {
                                R.string.color_tokens_source_custom
                            } else {
                                R.string.color_tokens_source_palette
                            }
                        ),
                        fontSize = 13.sp
                    )
                }
            }
        }

        val fadeSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        AnimatedContent(
            targetState = customMode,
            transitionSpec = { fadeIn(fadeSpec).togetherWith(fadeOut(fadeSpec)) },
            label = "tokenSource"
        ) { custom ->
            if (custom) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showPicker = true },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Colorize),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.color_tokens_pick_color))
                    }
                }
            } else {
                Column {
                    RoleSelector(
                        selected = role,
                        onSelect = { selected ->
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            role = selected
                        }
                    )
                    ShadeStrip(
                        shades = ShadeScale.indices.map { scheme[role, it] },
                        selectedIndex = (override as? ColorOverride.Shade)
                            ?.takeIf { it.role == role }
                            ?.index,
                        originIndex = origin?.takeIf { it.role == role }?.index,
                        onSelect = { index ->
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onOverride(ColorOverride.Shade(role, index, baseAlpha.takeIf { translucent }))
                        },
                        modifier = Modifier.padding(start = SHEET_PADDING, end = SHEET_PADDING, top = 16.dp)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SHEET_PADDING, end = SHEET_PADDING, top = 24.dp)
        ) {
            OutlinedButton(
                onClick = { onOverride(null) },
                enabled = override != null,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = stringResource(R.string.reset))
            }
            Button(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = stringResource(R.string.done))
            }
        }
    }
}

@Composable
private fun OverridePreviewTile(
    title: String,
    subtitle: String,
    @ColorInt previewColor: Int?,
    @ColorInt originalColor: Int?
) {
    val color = previewColor ?: AndroidColor.TRANSPARENT
    val background by animateColorAsState(
        targetValue = Color(color),
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "tokenPreview"
    )
    val content = Color(calculateTextColor(ColorUtils.compositeColors(color, GRID_AVERAGE)))
    val shape = RoundedCornerShape(24.dp)
    val lastOriginal = remember { mutableStateOf(originalColor) }
    if (originalColor != null && lastOriginal.value != originalColor) lastOriginal.value = originalColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_PADDING)
            .height(96.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .transparencyGrid(color)
                .background(background)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = content.copy(alpha = 0.8f)
            )
        }

        AnimatedVisibility(
            visible = originalColor != null,
            enter = expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                    fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) +
                    fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
        ) {
            OriginalSwatch(color = lastOriginal.value ?: AndroidColor.TRANSPARENT)
        }
    }
}

@Composable
private fun OriginalSwatch(@ColorInt color: Int) {
    val content = Color(calculateTextColor(ColorUtils.compositeColors(color, GRID_AVERAGE)))

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(88.dp)
            .fillMaxHeight()
            .transparencyGrid(color)
            .background(Color(color))
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.color_original),
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
        Text(
            text = intToArgbHexColor(color),
            style = MaterialTheme.typography.labelSmall,
            color = content.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RoleSelector(
    selected: PaletteRole,
    onSelect: (PaletteRole) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalFadingEdges(scrollState)
            .horizontalScroll(scrollState)
            .padding(horizontal = SHEET_PADDING)
    ) {
        PaletteRole.entries.forEach { role ->
            ToggleButton(
                checked = role == selected,
                onCheckedChange = { if (role != selected) onSelect(role) }
            ) {
                Text(
                    text = stringResource(role.labelRes),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ShadeStrip(
    shades: List<Int>,
    selectedIndex: Int?,
    originIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        shades.forEachIndexed { index, shade ->
            val selected = index == selectedIndex
            val weight by animateFloatAsState(
                targetValue = if (selected) 2f else 1f,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "shadeWeight"
            )
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 4.dp, bottomEnd = 4.dp)
                shades.lastIndex -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(4.dp)
            }
            val content = Color(calculateTextColor(shade))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(Color(shade))
                    .clickable { onSelect(index) }
            ) {
                when {
                    selected -> Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Check),
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(18.dp)
                    )

                    index == originIndex -> Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(content.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

private const val OPAQUE = 0xFF000000.toInt()

private const val OPAQUE_ALPHA = 0xFF

private const val GRID_AVERAGE = 0xFFE6E6E6.toInt()

private val SHEET_PADDING = 24.dp

@Preview
@Composable
private fun ColorOverrideContentPreview() {
    val scheme = ThemeEngine.scheme(
        source = ThemeSource(style = MONET.TONAL_SPOT, seedColor = 0xFF4285F4.toInt()),
        adjustments = ThemeAdjustments(),
        isDark = false
    )
    ColorBlendrTheme {
        ColorOverrideContent(
            title = "Primary container",
            scheme = scheme,
            baseColor = scheme[PaletteRole.PRIMARY, 3],
            origin = ColorOverride.Shade(PaletteRole.PRIMARY, 3),
            override = ColorOverride.Shade(PaletteRole.PRIMARY, 4),
            onOverride = {},
            onDismiss = {}
        )
    }
}

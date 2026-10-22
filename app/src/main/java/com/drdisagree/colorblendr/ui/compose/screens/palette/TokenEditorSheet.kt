package com.drdisagree.colorblendr.ui.compose.screens.palette

import androidx.annotation.ColorInt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.override.TokenOverride
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.horizontalFadingEdges
import com.drdisagree.colorblendr.utils.colors.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToHexColor
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerType
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenEditorSheet(
    token: Token,
    scheme: ResolvedScheme,
    override: TokenOverride?,
    onOverride: (TokenOverride?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        TokenEditorContent(
            token = token,
            scheme = scheme,
            override = override,
            onOverride = onOverride,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TokenEditorContent(
    token: Token,
    scheme: ResolvedScheme,
    override: TokenOverride?,
    onOverride: (TokenOverride?) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val origin = scheme.origin(token)
    var customMode by rememberSaveable(token) { mutableStateOf(override is TokenOverride.Custom) }
    var role by rememberSaveable(token) {
        mutableStateOf((override as? TokenOverride.Shade)?.role ?: origin?.role ?: PaletteRole.NEUTRAL)
    }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    @ColorInt val color = when (override) {
        is TokenOverride.Shade -> scheme[override.role, override.index]
        is TokenOverride.Custom -> override.color
        null -> scheme[token]
    }
    val subtitle = when (override) {
        is TokenOverride.Shade -> stringResource(
            R.string.color_tokens_from_shade,
            intToHexColor(color),
            stringResource(override.role.labelRes),
            ShadeScale.shades[override.index]
        )

        else -> intToHexColor(color)
    }

    if (showPicker) {
        ColorPickerDialog(
            initialColor = color,
            onDismissRequest = { showPicker = false },
            onColorPicked = { picked ->
                showPicker = false
                onOverride(TokenOverride.Custom(picked or OPAQUE))
            },
            alphaEnabled = false,
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
        TokenPreviewTile(
            title = token.displayName,
            subtitle = subtitle,
            color = color
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
                        selectedIndex = (override as? TokenOverride.Shade)
                            ?.takeIf { it.role == role }
                            ?.index,
                        originIndex = origin?.takeIf { it.role == role }?.let { nearestShade(it.tone) },
                        onSelect = { index ->
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onOverride(TokenOverride.Shade(role, index))
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
private fun TokenPreviewTile(
    title: String,
    subtitle: String,
    @ColorInt color: Int
) {
    val background by animateColorAsState(
        targetValue = Color(color),
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "tokenPreview"
    )
    val content = Color(calculateTextColor(color))
    val shape = RoundedCornerShape(24.dp)

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_PADDING)
            .height(96.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape)
            .background(background)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = content
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = content.copy(alpha = 0.8f)
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

private fun nearestShade(tone: Double): Int =
    ShadeScale.indices.minBy { abs(ShadeScale.tones[it] - tone) }

private const val OPAQUE = 0xFF000000.toInt()

private val SHEET_PADDING = 24.dp

@Preview
@Composable
private fun TokenEditorContentPreview() {
    val scheme = ThemeEngine.scheme(
        source = ThemeSource(style = MONET.TONAL_SPOT, seedColor = 0xFF4285F4.toInt()),
        adjustments = ThemeAdjustments(),
        isDark = false
    )
    ColorBlendrTheme {
        TokenEditorContent(
            token = Token.PRIMARY_CONTAINER,
            scheme = scheme,
            override = TokenOverride.Shade(PaletteRole.PRIMARY, 3),
            onOverride = {},
            onDismiss = {}
        )
    }
}

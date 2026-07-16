package com.drdisagree.colorblendr.ui.compose.screens.colors

import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.domain.AppEvents
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ColorPalettePreviewCard
import com.drdisagree.colorblendr.ui.compose.components.CommunityShowcase
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.compose.views.WallColorPreviewCanvas
import com.drdisagree.colorblendr.ui.compose.views.WallColorPreviewColors
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.utils.colors.ColorUtil.calculateTextColor
import kotlinx.coroutines.launch
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerType
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorsScreen(
    colorsViewModel: ColorsViewModel,
    onNavigateToColorPalette: () -> Unit,
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToCommunityTheme: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }
    val isDark = isSystemInDarkTheme()
    val rootMode = remember { isRootMode() }

    val wallpaperColors by colorsViewModel.wallpaperColors.collectAsStateWithLifecycle()
    val basicColors by colorsViewModel.basicColors.collectAsStateWithLifecycle()
    val wallpaperPalettes by colorsViewModel.wallpaperColorPalettes.collectAsStateWithLifecycle()
    val basicPalettes by colorsViewModel.basicColorPalettes.collectAsStateWithLifecycle()
    val customColorPref by colorsViewModel.customColorEnabled.collectAsStateWithLifecycle()

    var seedColor by remember { mutableIntStateOf(getSeedColorValue(0)) }
    var customColor by remember { mutableStateOf(customColorEnabled()) }
    var showWallpaperColors by remember {
        mutableStateOf(
            !customColorEnabled() && getWallpaperColorList().contains(getSeedColorValue())
        )
    }
    var showSeedColorPicker by rememberSaveable { mutableStateOf(false) }

    // React when Settings custom-color switch changes pref (via
    // RefreshCoordinator).
    LaunchedEffect(customColorPref) {
        if (customColor != customColorPref) {

            val wallpaperColorList = getWallpaperColorList()
            val wallpaperColor =
                if (wallpaperColorList.isNotEmpty()) wallpaperColorList[0] else AndroidColor.BLUE
            seedColor = if (!customColorPref) wallpaperColor else getSeedColorValue(wallpaperColor)
            customColor = customColorPref
        }
    }

    // Re-sync local selection when preview discarded (or other external pref
    // restore) so selected tick returns to saved color.
    LaunchedEffect(Unit) {
        RefreshCoordinator.refreshEvent.collect {
            val customColorPref = customColorEnabled()
            customColor = customColorPref
            seedColor = getSeedColorValue(0)
            showWallpaperColors =
                !customColorPref && getWallpaperColorList().contains(getSeedColorValue())
            colorsViewModel.refreshData()
        }
    }

    LaunchedEffect(Unit) {
        @Suppress("DEPRECATION")
        AppEvents.events.collect { action ->
            if (action == Intent.ACTION_WALLPAPER_CHANGED) {
                colorsViewModel.loadWallpaperColors()
            }
        }
    }

    fun applyColor(color: Int, isWallpaperColor: Boolean, alreadySelected: Boolean) {
        PreviewController.beginPreview()
        if (!alreadySelected) {
            resetCustomStyleIfNotNull()
        }

        setSeedColorValue(color)
        setCustomColorEnabled(!isWallpaperColor)
        colorsViewModel.onSeedColorSelected(color, !isWallpaperColor)
        seedColor = color
        customColor = !isWallpaperColor

        scope.launch {
            PreviewController.updatePreview()
        }
    }

    if (showSeedColorPicker) {
        ColorPickerDialog(
            initialColor = seedColor,
            onDismissRequest = { showSeedColorPicker = false },
            onColorPicked = { color ->
                showSeedColorPicker = false
                if (seedColor != color || !customColor) {
                    applyColor(color, isWallpaperColor = false, alreadySelected = customColor)
                    scope.launch { colorsViewModel.refreshData() }
                }
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

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.app_name),
                lifted = toolbarLifted
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .contentWidthLimit()
                    .verticalScroll(scrollState)
                    .padding(bottom = LocalPreviewBottomInset.current)
                    .padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
                    color = MaterialTheme.colorScheme.surface,
                    border = AppCardDefaults.outlinedBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(R.dimen.container_margin_horizontal),
                            end = dimensionResource(R.dimen.container_margin_horizontal),
                            bottom = dimensionResource(R.dimen.container_margin_bottom)
                        )
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp, end = 22.dp, top = 16.dp)
                        ) {
                            ToggleButton(
                                checked = showWallpaperColors,
                                onCheckedChange = {
                                    if (it && !showWallpaperColors) {
                                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        showWallpaperColors = true
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.wallpaper_colors),
                                    fontSize = 13.sp
                                )
                            }
                            ToggleButton(
                                checked = !showWallpaperColors,
                                onCheckedChange = {
                                    if (it && showWallpaperColors) {
                                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        showWallpaperColors = false
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.basic_colors),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        val gridFadeSpec =
                            MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
                        val gridSizeSpec =
                            MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

                        AnimatedContent(
                            targetState = showWallpaperColors,
                            transitionSpec = {
                                fadeIn(gridFadeSpec)
                                    .togetherWith(fadeOut(gridFadeSpec))
                                    .using(SizeTransform(clip = true) { _, _ -> gridSizeSpec })
                            },
                            label = "colorGrid"
                        ) { showWallpaper ->
                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    top = 18.dp,
                                    bottom = 24.dp
                                )
                        ) {
                            val colors = if (showWallpaper) wallpaperColors else basicColors
                            val palettes = if (showWallpaper) wallpaperPalettes else basicPalettes

                            colors.forEach { color ->
                                val palette = palettes[color]
                                val selected = color == seedColor &&
                                        if (showWallpaper) !customColor else true

                                if (palette != null) {
                                    val textColor = calculateTextColor(color)
                                    WallColorPreviewCanvas(
                                        colors = WallColorPreviewColors(
                                            halfCircle = Color(palette[0][4]),
                                            firstQuarterCircle = Color(palette[2][5]),
                                            secondQuarterCircle = Color(palette[1][6]),
                                            square = Color(palette[4][if (!isDark) 3 else 9]),
                                            centerCircle = Color(color),
                                            tick = Color(
                                                palette[4][if (textColor == AndroidColor.WHITE) 2 else 11]
                                            )
                                        ),
                                        selected = selected,
                                        onClick = {
                                            applyColor(color, showWallpaper, selected)
                                        },
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .size(48.dp)
                                    )
                                }
                            }

                            if (!showWallpaper) {
                                CustomColorTile(
                                    color = seedColor,
                                    selected = customColor && !colors.contains(seedColor),
                                    onClick = { showSeedColorPicker = true },
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(48.dp)
                                )
                            }
                        }
                        }
                    }
                }

                CommunityShowcase(
                    onViewAll = onNavigateToCommunity,
                    onThemeClick = { onNavigateToCommunityTheme(it.id) },
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ColorPalettePreviewCard(
                    title = stringResource(R.string.color_palette_title),
                    summary = stringResource(R.string.color_palette_desc),
                    onClick = onNavigateToColorPalette
                )
            }
        }
    }
}


// Custom seed color entry living at the end of the basic color grid: rounded
// square in the picked color, contrast plus icon, ring when active.
@Composable
private fun CustomColorTile(
    color: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "customColorTileScale"
    )

    val hasColor = color != 0
    val fallback = MaterialTheme.colorScheme.primaryContainer
    val tileColor = if (hasColor) Color(color) else fallback
    val iconTint = if (hasColor) {
        if (calculateTextColor(color) == AndroidColor.WHITE) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    // Fill inset inside the border; painting both to the same edge lets the
    // fill's antialiased fringe bleed past the stroke.
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                RoundedCornerShape(12.dp)
            )
            .padding(2.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(tileColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
    ) {
        Icon(
            painter = if (selected) {
                rememberVectorPainter(Icons.Rounded.Check)
            } else {
                rememberVectorPainter(Icons.Rounded.Colorize)
            },
            contentDescription = stringResource(R.string.seed_color_picker_title),
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Preview
@Composable
private fun CustomColorTilePreview() {
    ColorBlendrTheme {
        CustomColorTile(
            color = 0xFF0061A4.toInt(),
            selected = true,
            onClick = {}
        )
    }
}

@Suppress("ViewModelConstructorInComposable")
@AdaptivePreviews
@Composable
private fun ColorsScreenPreview() {
    ColorBlendrTheme {
        ColorsScreen(
            colorsViewModel = ColorsViewModel(),
            onNavigateToColorPalette = {}
        )
    }
}

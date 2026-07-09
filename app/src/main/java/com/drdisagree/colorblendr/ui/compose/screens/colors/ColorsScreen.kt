package com.drdisagree.colorblendr.ui.compose.screens.colors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ColorPickerItem
import com.drdisagree.colorblendr.ui.compose.components.MenuItem
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.views.WallColorPreviewCanvas
import com.drdisagree.colorblendr.ui.compose.views.WallColorPreviewColors
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.utils.colors.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorsScreen(
    colorsViewModel: ColorsViewModel,
    sharedViewModel: SharedViewModel?,
    fragmentManager: FragmentManager?,
    onNavigateToColorPalette: () -> Unit,
    onNavigateToPerAppTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val rootMode = remember { isRootMode() }

    val wallpaperColors by colorsViewModel.wallpaperColors.collectAsStateWithLifecycle()
    val basicColors by colorsViewModel.basicColors.collectAsStateWithLifecycle()
    val wallpaperPalettes by colorsViewModel.wallpaperColorPalettes.collectAsStateWithLifecycle()
    val basicPalettes by colorsViewModel.basicColorPalettes.collectAsStateWithLifecycle()
    val visibilityStates by (sharedViewModel?.visibilityStates?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(emptyMap<String, Int>()) })

    var seedColor by remember { mutableIntStateOf(getSeedColorValue(0)) }
    var customColor by remember { mutableStateOf(customColorEnabled()) }
    var showWallpaperColors by remember {
        mutableStateOf(
            !customColorEnabled() && getWallpaperColorList().contains(getSeedColorValue())
        )
    }
    var seedPickerVisible by remember { mutableStateOf(customColorEnabled()) }

    // Mirrors ColorsFragment.updateViewVisibility for the Settings toggle.
    val seedVisibilityState = visibilityStates[MONET_SEED_COLOR_ENABLED]
    LaunchedEffect(seedVisibilityState) {
        if (seedVisibilityState == null) return@LaunchedEffect

        val visible = seedVisibilityState == View.VISIBLE
        if (seedPickerVisible != visible) {
            seedPickerVisible = visible

            val wallpaperColorList = getWallpaperColorList()
            val wallpaperColor =
                if (wallpaperColorList.isNotEmpty()) wallpaperColorList[0] else AndroidColor.BLUE
            seedColor = if (!visible) wallpaperColor else getSeedColorValue(wallpaperColor)
            customColor = customColorEnabled()
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                colorsViewModel.loadWallpaperColors()
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
        )
        onDispose {
            try {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    fun applyColor(color: Int, isWallpaperColor: Boolean, alreadySelected: Boolean) {
        if (!alreadySelected) {
            resetCustomStyleIfNotNull()
        }

        setSeedColorValue(color)
        setCustomColorEnabled(!isWallpaperColor)
        seedColor = color
        customColor = !isWallpaperColor
        seedPickerVisible = !isWallpaperColor

        scope.launch {
            updateColorAppliedTimestamp()
            withContext(Dispatchers.IO) {
                try {
                    applyFabricatedColors()
                } catch (_: Exception) {
                }
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.app_name),
                lifted = scrollState.value > 0
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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
                                onCheckedChange = { if (it) showWallpaperColors = true }
                            ) {
                                Text(
                                    text = stringResource(R.string.wallpaper_colors),
                                    fontSize = 13.sp
                                )
                            }
                            ToggleButton(
                                checked = !showWallpaperColors,
                                onCheckedChange = { if (it) showWallpaperColors = false }
                            ) {
                                Text(
                                    text = stringResource(R.string.basic_colors),
                                    fontSize = 13.sp
                                )
                            }
                        }

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
                            val colors = if (showWallpaperColors) wallpaperColors else basicColors
                            val palettes = if (showWallpaperColors) wallpaperPalettes else basicPalettes

                            colors.forEach { color ->
                                val palette = palettes[color]
                                val selected = color == seedColor &&
                                        if (showWallpaperColors) !customColor else true

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
                                            applyColor(color, showWallpaperColors, selected)
                                        },
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = seedPickerVisible,
                    enter = expandVertically(
                        MaterialTheme.motionScheme.defaultSpatialSpec()
                    ) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = shrinkVertically(
                        MaterialTheme.motionScheme.defaultSpatialSpec()
                    ) + fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
                ) {
                    ColorPickerItem(
                        title = stringResource(R.string.seed_color_picker_title),
                        summary = stringResource(R.string.seed_color_picker_desc),
                        previewColor = Color(seedColor),
                        icon = painterResource(R.drawable.ic_paint),
                        onClick = {
                            val manager = fragmentManager ?: return@ColorPickerItem
                            ColorPickerDialog()
                                .withCornerRadius(24f)
                                .withColor(seedColor)
                                .withAlphaEnabled(false)
                                .withPicker(ImagePickerView::class.java)
                                .withListener { _: ColorPickerDialog?, color: Int ->
                                    if (seedColor != color) {
                                        seedColor = color
                                        setSeedColorValue(color)

                                        scope.launch {
                                            updateColorAppliedTimestamp()
                                            delay(300)
                                            withContext(Dispatchers.IO) {
                                                applyFabricatedColors()
                                            }
                                            colorsViewModel.refreshData()
                                        }
                                    }
                                }
                                .show(manager, "seedColorPicker")
                        }
                    )
                }

                MenuItem(
                    title = stringResource(R.string.color_palette_title),
                    summary = stringResource(R.string.color_palette_desc),
                    icon = painterResource(R.drawable.ic_table_view),
                    showEndArrow = true,
                    onClick = onNavigateToColorPalette
                )
                MenuItem(
                    title = stringResource(R.string.force_per_app_theme_title),
                    summary = stringResource(R.string.force_per_app_theme_desc),
                    icon = painterResource(R.drawable.ic_app_widgets),
                    showEndArrow = true,
                    enabled = rootMode,
                    disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                    onClick = onNavigateToPerAppTheme
                )
            }
        }
    }
}


@Suppress("ViewModelConstructorInComposable")
@Preview
@Composable
private fun ColorsScreenPreview() {
    ColorBlendrTheme {
        ColorsScreen(
            colorsViewModel = ColorsViewModel(),
            sharedViewModel = null,
            fragmentManager = null,
            onNavigateToColorPalette = {},
            onNavigateToPerAppTheme = {}
        )
    }
}

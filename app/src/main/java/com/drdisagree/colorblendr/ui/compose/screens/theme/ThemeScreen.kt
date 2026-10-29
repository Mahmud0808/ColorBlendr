package com.drdisagree.colorblendr.ui.compose.screens.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.MODE_SPECIFIC_THEMES
import com.drdisagree.colorblendr.data.common.Constant.accentSaturationKey
import com.drdisagree.colorblendr.data.common.Constant.backgroundLightnessKey
import com.drdisagree.colorblendr.data.common.Constant.backgroundSaturationKey
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.modeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.resetBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.resetBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundSaturation
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.SeekbarItem
import com.drdisagree.colorblendr.ui.compose.components.ThemeModeToggle
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.compose.utils.LocalWidthClass
import com.drdisagree.colorblendr.ui.compose.utils.WidthClass
import com.drdisagree.colorblendr.ui.compose.utils.rememberPrefState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ThemeScreen() {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }
    val isDark = isSystemInDarkTheme()
    val rootMode = remember { isRootMode() }

    val haptics = LocalHapticFeedback.current
    val modeSpecific by rememberPrefState(MODE_SPECIFIC_THEMES) { modeSpecificThemesEnabled() }
    var editDark by rememberSaveable { mutableStateOf(isDark) }
    val targetDark = if (modeSpecific) editDark else isDark

    var accentSaturation by key(targetDark, modeSpecific) {
        rememberPrefState(accentSaturationKey(targetDark)) { getAccentSaturation(targetDark) }
    }
    var backgroundSaturation by key(targetDark, modeSpecific) {
        rememberPrefState(backgroundSaturationKey(targetDark)) { getBackgroundSaturation(targetDark) }
    }
    var backgroundLightness by key(targetDark, modeSpecific) {
        rememberPrefState(backgroundLightnessKey(targetDark)) { getBackgroundLightness(targetDark) }
    }

    val preview by PreviewController.previewTheme.collectAsState()

    var committedPalette by remember { mutableStateOf(emptyList<List<Int>>()) }
    LaunchedEffect(preview == null, targetDark, accentSaturation, backgroundSaturation, backgroundLightness) {
        if (preview != null) return@LaunchedEffect

        committedPalette = withContext(Dispatchers.Default) {
            ThemePrefs.scheme(
                isDark = targetDark,
                adjustments = ThemePrefs.adjustments(
                    isDark = targetDark,
                    accentSaturation = accentSaturation,
                    backgroundSaturation = backgroundSaturation,
                    backgroundLightness = backgroundLightness
                )
            ).shades
        }
    }

    val colorPalette = preview?.scheme(targetDark)?.shades ?: committedPalette

    fun updateColors() {
        scope.launch {
            PreviewController.updatePreview()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.theme),
                lifted = toolbarLifted
            )
            val twoPane = LocalWidthClass.current == WidthClass.Expanded
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .contentWidthLimit(max = if (twoPane) 1100.dp else 640.dp)
                    .verticalScroll(scrollState)
                    .padding(previewContentPadding())
                    .padding(top = 12.dp)
            ) {
                val rampCard: @Composable () -> Unit = {
                    TonalRampCard(colorPalette = colorPalette)
                }
                val sliders: @Composable () -> Unit = {
                    if (modeSpecific) {
                        ThemeModeToggle(
                            isDark = editDark,
                            onChange = { dark ->
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                editDark = dark
                            },
                            modifier = Modifier.padding(
                                bottom = dimensionResource(R.dimen.container_margin_bottom)
                            )
                        )
                    }
                    ThemeSlider(
                        title = stringResource(R.string.accent_saturation),
                        value = accentSaturation,
                        enabled = rootMode,
                        onValueChange = {
                            accentSaturation = it
                            PreviewController.beginPreview()
                            setAccentSaturation(it, targetDark)
                            PreviewController.updatePreview(refreshOthers = false)
                        },
                        onValueChangeFinished = {
                            PreviewController.beginPreview()
                            resetCustomStyleIfNotNull()
                            setAccentSaturation(accentSaturation, targetDark)
                            updateColors()
                        },
                        onReset = {
                            PreviewController.beginPreview()
                            resetCustomStyleIfNotNull()
                            accentSaturation = 100
                            resetAccentSaturation(targetDark)
                            updateColors()
                        }
                    )
                    ThemeSlider(
                        title = stringResource(R.string.background_saturation),
                        value = backgroundSaturation,
                        enabled = rootMode,
                        onValueChange = {
                            backgroundSaturation = it
                            PreviewController.beginPreview()
                            setBackgroundSaturation(it, targetDark)
                            PreviewController.updatePreview(refreshOthers = false)
                        },
                        onValueChangeFinished = {
                            PreviewController.beginPreview()
                            resetCustomStyleIfNotNull()
                            setBackgroundSaturation(backgroundSaturation, targetDark)
                            updateColors()
                        },
                        onReset = {
                            PreviewController.beginPreview()
                            resetCustomStyleIfNotNull()
                            backgroundSaturation = 100
                            resetBackgroundSaturation(targetDark)
                            updateColors()
                        }
                    )
                    ThemeSlider(
                        title = stringResource(R.string.background_lightness),
                        value = backgroundLightness,
                        enabled = rootMode,
                        onValueChange = {
                            backgroundLightness = it
                            PreviewController.beginPreview()
                            setBackgroundLightness(it, targetDark)
                            PreviewController.updatePreview(refreshOthers = false)
                        },
                        onValueChangeFinished = {
                            PreviewController.beginPreview()
                            resetCustomStyleIfNotNull()
                            setBackgroundLightness(backgroundLightness, targetDark)
                            updateColors()
                        },
                        onReset = {
                            PreviewController.beginPreview()
                            resetCustomStyleIfNotNull()
                            backgroundLightness = 100
                            resetBackgroundLightness(targetDark)
                            updateColors()
                        }
                    )
                }

                if (twoPane) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) { rampCard() }
                        Column(modifier = Modifier.weight(1f)) { sliders() }
                    }
                } else {
                    rampCard()
                    sliders()
                }
            }
        }
    }
}

@Composable
private fun TonalRampCard(colorPalette: List<List<Int>>) {
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
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            TonalRampRow(
                label = stringResource(R.string.primary),
                shades = colorPalette.getOrNull(0)
            )
            TonalRampRow(
                label = stringResource(R.string.secondary),
                shades = colorPalette.getOrNull(1)
            )
            TonalRampRow(
                label = stringResource(R.string.tertiary),
                shades = colorPalette.getOrNull(2)
            )
            TonalRampRow(
                label = stringResource(R.string.neutral_1),
                shades = colorPalette.getOrNull(3)
            )
            TonalRampRow(
                label = stringResource(R.string.neutral_2),
                shades = colorPalette.getOrNull(4)
            )
        }
    }
}

@Composable
private fun ThemeSlider(
    title: String,
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: () -> Unit,
    onReset: () -> Unit
) {
    SeekbarItem(
        title = title,
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        minValue = 0,
        maxValue = 200,
        defaultValue = 100,
        onReset = onReset,
        valueFormat = "x",
        isDecimalFormat = true,
        decimalFormat = "#.##",
        outputScale = 100f,
        enabled = enabled,
        disabledReason = if (!enabled) stringResource(R.string.root_required) else null
    )
}

@Composable
private fun TonalRampRow(
    label: String,
    shades: List<Int>?
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (shades != null) {
                shades.forEach { shade ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(shade))
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun ThemeScreenPreview() {
    ColorBlendrTheme {
        ThemeScreen()
    }
}
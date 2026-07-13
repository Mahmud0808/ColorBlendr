package com.drdisagree.colorblendr.ui.compose.screens.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCENT_SATURATION
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_SATURATION
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.resetBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.resetBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundSaturation
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.SeekbarItem
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.rememberPrefState
import com.drdisagree.colorblendr.ui.compose.views.ColorPreviewCanvas
import com.drdisagree.colorblendr.utils.colors.ColorUtil
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

    var accentSaturation by rememberPrefState(MONET_ACCENT_SATURATION) { getAccentSaturation() }
    var backgroundSaturation by rememberPrefState(MONET_BACKGROUND_SATURATION) { getBackgroundSaturation() }
    var backgroundLightness by rememberPrefState(MONET_BACKGROUND_LIGHTNESS) { getBackgroundLightness() }

    val preview by PreviewController.previewColors.collectAsState()

    var committedPalette by remember {
        mutableStateOf(
            ColorUtil.generateModifiedColors(
                getCurrentMonetStyle(),
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
                pitchBlackThemeEnabled(),
                accurateShadesEnabled()
            ).map { it.toList() }
        )
    }
    LaunchedEffect(preview == null, accentSaturation, backgroundSaturation, backgroundLightness) {
        if (preview != null) return@LaunchedEffect

        committedPalette = withContext(Dispatchers.Default) {
            ColorUtil.generateModifiedColors(
                getCurrentMonetStyle(),
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
                pitchBlackThemeEnabled(),
                accurateShadesEnabled()
            ).map { it.toList() }
        }
    }

    val colorPalette = preview?.let { if (isDark) it.paletteDark else it.paletteLight }
        ?: committedPalette

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
                showBackButton = true,
                lifted = toolbarLifted
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LabeledColorPreview(
                                label = stringResource(R.string.primary),
                                palette = colorPalette.getOrNull(0),
                                isDark = isDark
                            )
                            LabeledColorPreview(
                                label = stringResource(R.string.secondary),
                                palette = colorPalette.getOrNull(1),
                                isDark = isDark
                            )
                            LabeledColorPreview(
                                label = stringResource(R.string.tertiary),
                                palette = colorPalette.getOrNull(2),
                                isDark = isDark
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LabeledColorPreview(
                                label = stringResource(R.string.neutral_1),
                                palette = colorPalette.getOrNull(3),
                                isDark = isDark
                            )
                            LabeledColorPreview(
                                label = stringResource(R.string.neutral_2),
                                palette = colorPalette.getOrNull(4),
                                isDark = isDark
                            )
                        }
                    }
                }

                ThemeSlider(
                    title = stringResource(R.string.accent_saturation),
                    value = accentSaturation,
                    enabled = rootMode,
                    onValueChange = {
                        accentSaturation = it
                        PreviewController.beginPreview()
                        setAccentSaturation(it)
                        PreviewController.updatePreview(refreshOthers = false)
                    },
                    onValueChangeFinished = {
                        PreviewController.beginPreview()
                        resetCustomStyleIfNotNull()
                        setAccentSaturation(accentSaturation)
                        updateColors()
                    },
                    onReset = {
                        PreviewController.beginPreview()
                        resetCustomStyleIfNotNull()
                        accentSaturation = 100
                        resetAccentSaturation()
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
                        setBackgroundSaturation(it)
                        PreviewController.updatePreview(refreshOthers = false)
                    },
                    onValueChangeFinished = {
                        PreviewController.beginPreview()
                        resetCustomStyleIfNotNull()
                        setBackgroundSaturation(backgroundSaturation)
                        updateColors()
                    },
                    onReset = {
                        PreviewController.beginPreview()
                        resetCustomStyleIfNotNull()
                        backgroundSaturation = 100
                        resetBackgroundSaturation()
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
                        setBackgroundLightness(it)
                        PreviewController.updatePreview(refreshOthers = false)
                    },
                    onValueChangeFinished = {
                        PreviewController.beginPreview()
                        resetCustomStyleIfNotNull()
                        setBackgroundLightness(backgroundLightness)
                        updateColors()
                    },
                    onReset = {
                        PreviewController.beginPreview()
                        resetCustomStyleIfNotNull()
                        backgroundLightness = 100
                        resetBackgroundLightness()
                        updateColors()
                    }
                )
            }
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
private fun LabeledColorPreview(
    label: String,
    palette: List<Int>?,
    isDark: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(12.dp)
    ) {
        if (palette != null) {
            ColorPreviewCanvas(
                squareColor = Color(palette[if (!isDark) 3 else 9]),
                halfCircleColor = Color(palette[4]),
                firstQuarterCircleColor = Color(palette[5]),
                secondQuarterCircleColor = Color(palette[6]),
                padding = 8.dp,
                modifier = Modifier.size(48.dp)
            )
        } else {
            ColorPreviewCanvas(
                padding = 8.dp,
                modifier = Modifier.size(48.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Preview
@Composable
private fun ThemeScreenPreview() {
    ColorBlendrTheme {
        ThemeScreen()
    }
}

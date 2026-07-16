package com.drdisagree.colorblendr.ui.compose.screens.settings

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.DARKER_LAUNCHER_ICONS
import com.drdisagree.colorblendr.data.common.Constant.FORCE_PITCH_BLACK_SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.MODE_SPECIFIC_THEMES
import com.drdisagree.colorblendr.data.common.Constant.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.data.common.Constant.MONET_SECONDARY_COLOR
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.MONET_TERTIARY_COLOR
import com.drdisagree.colorblendr.data.common.Constant.PIXEL_LAUNCHER
import com.drdisagree.colorblendr.data.common.Constant.SCREEN_OFF_UPDATE_COLORS
import com.drdisagree.colorblendr.data.common.Constant.SEMI_TRANSPARENT_LAUNCHER_ICONS
import com.drdisagree.colorblendr.data.common.Constant.TASKER_INTEGRATION
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.darkerLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.forcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getColorSpecVersion
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.getSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.getTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.modeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.screenOffColorUpdateEnabled
import com.drdisagree.colorblendr.data.common.Utilities.semiTransparentLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setColorSpecVersion
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.setDarkerLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setForcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setModeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setScreenOffColorUpdateEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.setSemiTransparentLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setTaskerIntegrationEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.taskerIntegrationEnabled
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.domain.AppScope
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ColorPickerItem
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.MenuItem
import com.drdisagree.colorblendr.ui.compose.components.SingleChoiceDialog
import com.drdisagree.colorblendr.ui.compose.components.SwitchItem
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.rememberPrefState
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerType
import kotlin.time.Duration.Companion.milliseconds

private const val TARGET_SECONDARY = "secondary"
private const val TASKER_GUIDE_URL =
    "https://github.com/Mahmud0808/ColorBlendr#tasker-integration"
private const val TARGET_TERTIARY = "tertiary"

@Composable
fun SettingsAdvancedScreen(
    onNavigateToPerAppTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }

    val rootMode = remember { isRootMode() }
    val customColor by rememberPrefState(MONET_SEED_COLOR_ENABLED) { customColorEnabled() }
    val hasPixelLauncher = remember { SystemUtil.isAppInstalled(PIXEL_LAUNCHER) }
    val pitchBlackEnabled by rememberPrefState(MONET_PITCH_BLACK_THEME) { pitchBlackThemeEnabled() }

    var secondaryColor by rememberPrefState(MONET_SECONDARY_COLOR) { getSecondaryColorValue() }
    var tertiaryColor by rememberPrefState(MONET_TERTIARY_COLOR) { getTertiaryColorValue() }
    var screenOffUpdate by rememberPrefState(SCREEN_OFF_UPDATE_COLORS) { screenOffColorUpdateEnabled() }
    var taskerIntegration by rememberPrefState(TASKER_INTEGRATION) { taskerIntegrationEnabled() }
    var modeSpecificThemes by rememberPrefState(MODE_SPECIFIC_THEMES) { modeSpecificThemesEnabled() }
    var darkerIcons by rememberPrefState(DARKER_LAUNCHER_ICONS) { darkerLauncherIconsEnabled() }
    var semiTransparentIcons by rememberPrefState(SEMI_TRANSPARENT_LAUNCHER_ICONS) { semiTransparentLauncherIconsEnabled() }
    var pitchBlackWorkaround by rememberPrefState(FORCE_PITCH_BLACK_SETTINGS) { forcePitchBlackSettingsEnabled() }

    fun updateColors() {
        scope.launch {
            PreviewController.updatePreview()
        }
    }

    fun applyColorsNow() {
        AppScope.launch {
            updateColorAppliedTimestamp()
            delay(300.milliseconds)
            withContext(Dispatchers.IO) {
                applyFabricatedColors()
            }
        }
    }

    fun savePixelLauncherInPerAppTheme() {
        if (!hasPixelLauncher || !rootMode) return
        val selectedApps = getSelectedFabricatedApps()
        selectedApps[PIXEL_LAUNCHER] = true
        setSelectedFabricatedApps(selectedApps)
    }

    // Saveable target so dialog survives rotation (lambdas can't go in
    // instance state).
    var colorPickerTarget by rememberSaveable { mutableStateOf<String?>(null) }

    colorPickerTarget?.let { target ->
        val isSecondary = target == TARGET_SECONDARY

        ColorPickerDialog(
            initialColor = if (isSecondary) secondaryColor else tertiaryColor,
            onDismissRequest = { colorPickerTarget = null },
            onColorPicked = { color ->
                colorPickerTarget = null
                val currentColor = if (isSecondary) secondaryColor else tertiaryColor
                if (currentColor != color) {
                    PreviewController.beginPreview()
                    resetCustomStyleIfNotNull()
                    if (isSecondary) {
                        secondaryColor = color
                        setSecondaryColorValue(color)
                    } else {
                        tertiaryColor = color
                        setTertiaryColorValue(color)
                    }
                    updateColors()
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

    var showColorSpecDialog by rememberSaveable { mutableStateOf(false) }
    if (showColorSpecDialog) {
        val currentVersion = getColorSpecVersion()
        SingleChoiceDialog(
            title = stringResource(R.string.colorspec_title),
            options = resources.getStringArray(R.array.colorspec_versions).toList(),
            selectedIndex = currentVersion,
            onSelect = { which ->
                if (currentVersion != which) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    PreviewController.beginPreview()
                    resetCustomStyleIfNotNull()
                    setColorSpecVersion(which)

                    if (which != 2 && getCurrentMonetStyle() == MONET.CMF) {
                        setCurrentMonetStyle(MONET.TONAL_SPOT)
                    }

                    RefreshCoordinator.triggerRefresh()
                    updateColors()
                }
            },
            onDismiss = { showColorSpecDialog = false }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.advanced_settings),
                showBackButton = true,
                lifted = toolbarLifted
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .contentWidthLimit()
                    .verticalScroll(scrollState)
                    .padding(bottom = LocalPreviewBottomInset.current)
                    .padding(top = 16.dp)
            ) {
                ColorPickerItem(
                    title = stringResource(R.string.custom_secondary_color_desc),
                    summary = stringResource(R.string.custom_secondary_color_summary),
                    previewColor = Color(secondaryColor),
                    icon = rememberVectorPainter(Icons.Rounded.FormatPaint),
                    enabled = customColor && rootMode,
                    disabledReason = when {
                        !rootMode -> stringResource(R.string.root_required)
                        !customColor -> stringResource(R.string.custom_primary_color_required)
                        else -> null
                    },
                    position = WidgetPosition.Top,
                    onClick = {
                        colorPickerTarget = TARGET_SECONDARY
                    }
                )
                ColorPickerItem(
                    title = stringResource(R.string.custom_tertiary_color_title),
                    summary = stringResource(R.string.custom_tertiary_color_desc),
                    previewColor = Color(tertiaryColor),
                    icon = rememberVectorPainter(Icons.Rounded.FormatPaint),
                    enabled = customColor && rootMode,
                    disabledReason = when {
                        !rootMode -> stringResource(R.string.root_required)
                        !customColor -> stringResource(R.string.custom_primary_color_required)
                        else -> null
                    },
                    position = WidgetPosition.Bottom,
                    onClick = {
                        colorPickerTarget = TARGET_TERTIARY
                    }
                )

                MenuItem(
                    title = stringResource(R.string.colorspec_title),
                    summary = stringResource(R.string.colorspec_desc),
                    icon = rememberVectorPainter(Icons.Rounded.AutoAwesome),
                    enabled = rootMode,
                    disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                    position = WidgetPosition.Top,
                    onClick = { showColorSpecDialog = true }
                )
                SwitchItem(
                    title = stringResource(R.string.screen_off_update_title),
                    summary = stringResource(R.string.screen_off_update_desc),
                    icon = rememberVectorPainter(Icons.Rounded.Update),
                    checked = screenOffUpdate,
                    position = WidgetPosition.Middle,
                    onCheckedChange = { isChecked ->
                        screenOffUpdate = isChecked
                        setScreenOffColorUpdateEnabled(isChecked)
                        if (!isChecked) {
                            applyColorsNow()
                        }
                    }
                )
                SwitchItem(
                    title = stringResource(R.string.mode_specific_theme_title),
                    summary = stringResource(R.string.mode_specific_theme_desc),
                    icon = rememberVectorPainter(Icons.Rounded.Contrast),
                    checked = modeSpecificThemes,
                    enabled = rootMode,
                    disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                    position = WidgetPosition.Middle,
                    onCheckedChange = { isChecked ->
                        modeSpecificThemes = isChecked
                        resetCustomStyleIfNotNull()
                        setModeSpecificThemesEnabled(isChecked)
                        applyColorsNow()
                    }
                )
                SwitchItem(
                    title = stringResource(R.string.tasker_integration_title),
                    summary = stringResource(R.string.tasker_integration_desc),
                    icon = rememberVectorPainter(Icons.Rounded.AutoMode),
                    checked = taskerIntegration,
                    position = WidgetPosition.Bottom,
                    summaryLinkText = stringResource(R.string.tasker_setup_guide_title),
                    onSummaryLinkClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, TASKER_GUIDE_URL.toUri())
                            )
                        }.onFailure {
                            Toast.makeText(
                                context,
                                R.string.no_browser_found,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onCheckedChange = { isChecked ->
                        taskerIntegration = isChecked
                        setTaskerIntegrationEnabled(isChecked)
                    }
                )

                MenuItem(
                    title = stringResource(R.string.force_per_app_theme_title),
                    summary = stringResource(R.string.force_per_app_theme_desc),
                    icon = rememberVectorPainter(Icons.Rounded.Widgets),
                    showEndArrow = true,
                    enabled = rootMode,
                    disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                    onClick = onNavigateToPerAppTheme
                )

                SwitchItem(
                    title = stringResource(R.string.darker_launcher_icons_title),
                    summary = stringResource(R.string.darker_launcher_icons_desc),
                    icon = rememberVectorPainter(Icons.Rounded.DarkMode),
                    checked = darkerIcons,
                    enabled = rootMode && hasPixelLauncher,
                    disabledReason = when {
                        !rootMode -> stringResource(R.string.root_required)
                        !hasPixelLauncher -> stringResource(R.string.pixel_launcher_required)
                        else -> null
                    },
                    position = WidgetPosition.Top,
                    onCheckedChange = { isChecked ->
                        darkerIcons = isChecked
                        if (isChecked) {
                            savePixelLauncherInPerAppTheme()
                        }
                        setDarkerLauncherIconsEnabled(isChecked)
                        applyColorsNow()
                    }
                )
                SwitchItem(
                    title = stringResource(R.string.semitransparent_launcher_title),
                    summary = stringResource(R.string.semitransparent_launcher_desc),
                    icon = rememberVectorPainter(Icons.Rounded.Opacity),
                    checked = semiTransparentIcons,
                    enabled = rootMode && hasPixelLauncher,
                    disabledReason = when {
                        !rootMode -> stringResource(R.string.root_required)
                        !hasPixelLauncher -> stringResource(R.string.pixel_launcher_required)
                        else -> null
                    },
                    position = WidgetPosition.Bottom,
                    onCheckedChange = { isChecked ->
                        semiTransparentIcons = isChecked
                        if (isChecked) {
                            savePixelLauncherInPerAppTheme()
                        }
                        setSemiTransparentLauncherIconsEnabled(isChecked)
                        applyColorsNow()
                    }
                )

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    SwitchItem(
                        title = stringResource(R.string.pitch_black_settings_workaround_title),
                        summary = stringResource(R.string.pitch_black_settings_workaround_desc),
                        icon = rememberVectorPainter(Icons.Rounded.Tune),
                        checked = pitchBlackWorkaround,
                        enabled = rootMode && pitchBlackEnabled,
                        disabledReason = when {
                            !rootMode -> stringResource(R.string.root_required)
                            !pitchBlackEnabled -> stringResource(R.string.pitch_black_theme_required)
                            else -> null
                        },
                        onCheckedChange = { isChecked ->
                            pitchBlackWorkaround = isChecked
                            setForcePitchBlackSettingsEnabled(isChecked)
                            applyColorsNow()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsAdvancedScreenPreview() {
    ColorBlendrTheme {
        SettingsAdvancedScreen()
    }
}

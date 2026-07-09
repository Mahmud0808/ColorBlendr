package com.drdisagree.colorblendr.ui.compose.screens.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.clearAllOverriddenColors
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isColorOverriddenFor
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.manualColorOverrideEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setAccurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setManualColorOverrideEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setPitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setTintedTextEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbarHost
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.showSnackbarReplacing
import com.drdisagree.colorblendr.ui.compose.components.BackupRestoreCard
import com.drdisagree.colorblendr.ui.compose.components.ToolbarOverflowButton
import com.drdisagree.colorblendr.ui.compose.components.MenuItem
import com.drdisagree.colorblendr.ui.compose.components.SwitchItem
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.utils.app.BackupRestore.backupDatabaseAndPrefs
import com.drdisagree.colorblendr.utils.app.BackupRestore.restoreDatabaseAndPrefs
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.manager.OverlayManager.isOverlayEnabled
import com.drdisagree.colorblendr.utils.manager.OverlayManager.removeFabricatedColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.R as AndroidR

@Composable
fun SettingsScreen(
    restoreUri: Uri?,
    onRestoreUriConsumed: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAdvanced: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val rootMode = remember { isRootMode() }
    var masterChecked by remember {
        mutableStateOf(
            (isThemingEnabled() && isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM))
                    || isShizukuThemingEnabled()
                    || isWirelessAdbThemingEnabled()
        )
    }
    var accurateShades by remember { mutableStateOf(accurateShadesEnabled()) }
    var pitchBlack by remember { mutableStateOf(pitchBlackThemeEnabled()) }
    var customPrimaryColor by remember { mutableStateOf(customColorEnabled()) }
    var tintTextColor by remember { mutableStateOf(tintedTextEnabled()) }
    var overrideManually by remember { mutableStateOf(manualColorOverrideEnabled()) }
    var overflowExpanded by remember { mutableStateOf(false) }

    val backupSuccessText = stringResource(R.string.backup_success)
    val backupFailText = stringResource(R.string.backup_fail)
    val restoreFailText = stringResource(R.string.restore_fail)
    val dismissText = stringResource(R.string.dismiss)
    val retryText = stringResource(R.string.retry)

    fun updateColors() {
        scope.launch {
            updateColorAppliedTimestamp()
            delay(300)
            withContext(Dispatchers.IO) {
                applyFabricatedColors()
            }
        }
    }

    fun numColorsOverridden(): Int {
        var colorOverridden = 0
        systemPaletteNames.forEach { palettes ->
            palettes
                .asSequence()
                .filter { isColorOverriddenFor(it) }
                .forEach { _ -> colorOverridden++ }
        }
        return colorOverridden
    }

    var pendingBackup by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf(false) }

    fun fileIntent(isBackingUp: Boolean) = Intent().apply {
        action = if (isBackingUp) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT
        type = "*/*"
        putExtra(
            Intent.EXTRA_TITLE,
            "theme_config_" + System.currentTimeMillis() + ".colorblendr"
        )
    }

    fun showRestoreDialog(uri: Uri) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.confirmation_title))
            .setMessage(context.getString(R.string.confirmation_desc))
            .setPositiveButton(context.getString(AndroidR.string.ok)) { dialog, _ ->
                dialog.dismiss()
                scope.launch(Dispatchers.IO) {
                    val success = uri.restoreDatabaseAndPrefs()
                    withContext(Dispatchers.Main) {
                        if (success) {
                            updateColors()
                        } else {
                            scope.launch {
                                val result = snackbarHostState.showSnackbarReplacing(
                                    message = restoreFailText,
                                    actionLabel = retryText,
                                    duration = SnackbarDuration.Indefinite
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    pendingRestore = true
                                }
                            }
                        }
                        onRestoreUriConsumed()
                    }
                }
            }
            .setNegativeButton(context.getString(AndroidR.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                onRestoreUriConsumed()
            }
            .show()
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                val success = uri.backupDatabaseAndPrefs()
                withContext(Dispatchers.Main) {
                    scope.launch {
                        if (success) {
                            snackbarHostState.showSnackbarReplacing(
                                message = backupSuccessText,
                                actionLabel = dismissText
                            )
                        } else {
                            val snackResult = snackbarHostState.showSnackbarReplacing(
                                message = backupFailText,
                                actionLabel = retryText,
                                duration = SnackbarDuration.Indefinite
                            )
                            if (snackResult == SnackbarResult.ActionPerformed) {
                                pendingBackup = true
                            }
                        }
                    }
                }
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { showRestoreDialog(it) }
        }
    }

    LaunchedEffect(pendingBackup) {
        if (pendingBackup) {
            pendingBackup = false
            backupLauncher.launch(fileIntent(true))
        }
    }
    LaunchedEffect(pendingRestore) {
        if (pendingRestore) {
            pendingRestore = false
            restoreLauncher.launch(fileIntent(false))
        }
    }
    LaunchedEffect(restoreUri) {
        restoreUri?.let { showRestoreDialog(it) }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            Column {
                AppToolbar(
                    title = stringResource(R.string.settings),
                    showBackButton = true,
                    lifted = scrollState.value > 0,
                    actions = {
                        Box {
                            ToolbarOverflowButton(onClick = { overflowExpanded = true })
                            DropdownMenu(
                                expanded = overflowExpanded,
                                onDismissRequest = { overflowExpanded = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                offset = DpOffset((-16).dp, 0.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.advanced_settings)) },
                                    onClick = {
                                        overflowExpanded = false
                                        onNavigateToAdvanced()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(R.string.privacy_policy)) },
                                    onClick = {
                                        overflowExpanded = false
                                        onNavigateToPrivacyPolicy()
                                    }
                                )
                            }
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = 16.dp)
                ) {
                    SwitchItem(
                        title = stringResource(
                            R.string.app_service_title,
                            stringResource(R.string.app_name)
                        ),
                        checked = masterChecked,
                        summaryOn = stringResource(R.string.app_service_desc_on),
                        summaryOff = stringResource(R.string.app_service_desc_off),
                        icon = painterResource(R.drawable.ic_service),
                        isMasterSwitch = true,
                        onCheckedChange = { isChecked ->
                            masterChecked = isChecked
                            setThemingEnabled(isChecked)
                            setShizukuThemingEnabled(isChecked)
                            setWirelessAdbThemingEnabled(isChecked)
                            updateColorAppliedTimestamp()

                            scope.launch {
                                try {
                                    delay(300)
                                    withContext(Dispatchers.IO) {
                                        if (isChecked) {
                                            updateColorAppliedTimestamp()
                                            applyFabricatedColors()
                                        } else {
                                            removeFabricatedColors()
                                        }
                                    }

                                    val isOverlayActive =
                                        isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM)
                                                || isShizukuThemingEnabled()
                                                || isWirelessAdbThemingEnabled()
                                    masterChecked = isOverlayActive

                                    if (isChecked != isOverlayActive) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.something_went_wrong),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                    )

                    SwitchItem(
                        title = stringResource(R.string.accurate_shades_title),
                        summary = stringResource(R.string.accurate_shades_desc),
                        icon = painterResource(R.drawable.ic_brush),
                        checked = accurateShades,
                        enabled = rootMode,
                        disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                        position = WidgetPosition.Top,
                        onCheckedChange = { isChecked ->
                            accurateShades = isChecked
                            resetCustomStyleIfNotNull()
                            setAccurateShadesEnabled(isChecked)
                            RefreshCoordinator.triggerRefresh()
                            updateColors()
                        }
                    )
                    SwitchItem(
                        title = stringResource(R.string.pitch_black_theme_title),
                        summary = stringResource(R.string.pitch_black_theme_desc),
                        icon = painterResource(R.drawable.ic_invert_colors),
                        checked = pitchBlack,
                        enabled = rootMode,
                        disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                        position = WidgetPosition.Middle,
                        onCheckedChange = { isChecked ->
                            pitchBlack = isChecked
                            resetCustomStyleIfNotNull()
                            setPitchBlackThemeEnabled(isChecked)
                            updateColors()
                        }
                    )
                    SwitchItem(
                        title = stringResource(R.string.custom_primary_color_title),
                        summary = stringResource(R.string.custom_primary_color_desc),
                        icon = painterResource(R.drawable.ic_color_fill),
                        checked = customPrimaryColor,
                        position = WidgetPosition.Middle,
                        onCheckedChange = { isChecked ->
                            customPrimaryColor = isChecked
                            resetCustomStyleIfNotNull()
                            setCustomColorEnabled(isChecked)
                            RefreshCoordinator.triggerRefresh()
                            if (!isChecked) {
                                val wallpaperColorList = getWallpaperColorList()
                                setSeedColorValue(
                                    if (wallpaperColorList.isNotEmpty()) wallpaperColorList[0]
                                    else Color.BLUE
                                )
                                updateColors()
                            }
                        }
                    )
                    SwitchItem(
                        title = stringResource(R.string.tint_text_title),
                        summary = stringResource(R.string.tint_text_desc),
                        icon = painterResource(R.drawable.ic_text),
                        checked = tintTextColor,
                        enabled = rootMode,
                        disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                        position = WidgetPosition.Middle,
                        onCheckedChange = { isChecked ->
                            tintTextColor = isChecked
                            resetCustomStyleIfNotNull()
                            setTintedTextEnabled(isChecked)
                            updateColors()
                        }
                    )
                    SwitchItem(
                        title = stringResource(R.string.override_colors_manually_title),
                        summary = stringResource(R.string.override_colors_manually_desc),
                        icon = painterResource(R.drawable.ic_color_picker),
                        checked = overrideManually,
                        enabled = rootMode,
                        disabledReason = if (!rootMode) stringResource(R.string.root_required) else null,
                        position = WidgetPosition.Bottom,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                overrideManually = true
                                setManualColorOverrideEnabled(true)
                            } else {
                                if (numColorsOverridden() > 5) {
                                    MaterialAlertDialogBuilder(context)
                                        .setTitle(context.getString(R.string.confirmation_title))
                                        .setMessage(context.getString(R.string.this_cannot_be_undone))
                                        .setPositiveButton(context.getString(AndroidR.string.ok)) { dialog, _ ->
                                            dialog.dismiss()
                                            overrideManually = false
                                            setManualColorOverrideEnabled(false)
                                            if (numColorsOverridden() != 0) {
                                                clearAllOverriddenColors()
                                                updateColors()
                                            }
                                        }
                                        .setNegativeButton(context.getString(AndroidR.string.cancel)) { dialog, _ ->
                                            dialog.dismiss()
                                            overrideManually = true
                                        }
                                        .show()
                                } else {
                                    overrideManually = false
                                    setManualColorOverrideEnabled(false)
                                    if (numColorsOverridden() != 0) {
                                        clearAllOverriddenColors()
                                        updateColors()
                                    }
                                }
                            }
                        }
                    )

                    BackupRestoreCard(
                        onBackup = { backupLauncher.launch(fileIntent(true)) },
                        onRestore = { restoreLauncher.launch(fileIntent(false)) }
                    )

                    MenuItem(
                        title = stringResource(R.string.about_this_app_title),
                        summary = stringResource(R.string.about_this_app_desc),
                        icon = painterResource(R.drawable.ic_info),
                        showEndArrow = true,
                        onClick = onNavigateToAbout
                    )
                }
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    ColorBlendrTheme {
        SettingsScreen(
            restoreUri = null,
            onRestoreUriConsumed = {},
            onNavigateToAbout = {},
            onNavigateToAdvanced = {},
            onNavigateToPrivacyPolicy = {}
        )
    }
}

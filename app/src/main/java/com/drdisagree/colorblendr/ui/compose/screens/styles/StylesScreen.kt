package com.drdisagree.colorblendr.ui.compose.screens.styles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.CUSTOM_MONET_STYLE
import com.drdisagree.colorblendr.data.common.Constant.MONET_STYLE
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.clearOriginalStyleName
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.setOriginalStyleName
import com.drdisagree.colorblendr.data.config.Prefs.toPrefs
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbar
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.SnackbarVisibility
import com.drdisagree.colorblendr.ui.compose.components.OutlinedTextFieldDialog
import com.drdisagree.colorblendr.ui.compose.components.StylePreviewCard
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.rememberPrefState
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.app.BackupRestore
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.getStyleNameForRootless
import kotlinx.coroutines.launch
import android.R as AndroidR

@Composable
fun StylesScreen(stylesViewModel: StylesViewModel) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val toolbarLifted by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    val styleList by stylesViewModel.styleList.collectAsStateWithLifecycle()
    val stylePalettes by stylesViewModel.stylePalettes.collectAsStateWithLifecycle()

    val rootMode = remember { isRootMode() }
    var selectedStyle by rememberPrefState(MONET_STYLE) { getCurrentMonetStyle() }
    var selectedCustomStyle by rememberPrefState(CUSTOM_MONET_STYLE) { getCurrentCustomStyle() }
    var dialogState by remember { mutableStateOf<StyleDialogState?>(null) }

    LaunchedEffect(styleList) {
        if (styleList.isNotEmpty() &&
            selectedCustomStyle != null &&
            !styleList.any { it.customStyle?.styleId == selectedCustomStyle }
        ) {
            resetCustomStyle()
            selectedCustomStyle = null
        }
    }

    fun applyColorScheme() {
        scope.launch {
            PreviewController.updatePreview()
        }
    }

    val previewColors by PreviewController.previewColors.collectAsStateWithLifecycle()
    val isApplying by PreviewController.isApplying.collectAsStateWithLifecycle()
    // Additive push: snackbar lifts the base, preview FABs stack on top.
    val fabBottomPadding by animateDpAsState(
        targetValue = (if (SnackbarVisibility.visible) 76.dp else 12.dp) +
                (if (previewColors != null && !isApplying) 72.dp else 0.dp),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "fabPreviewPush"
    )

    var fabHidden by rememberSaveable { mutableStateOf(false) }
    // Confirm targets survive rotation; lambdas can't be saved.
    var pendingUpdateStyleId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteStyleId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            if (listState.isScrollInProgress) {
                fabHidden = if (index != previousIndex) {
                    index > previousIndex
                } else {
                    offset > previousOffset
                }
            }
            previousIndex = index
            previousOffset = offset
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            Column {
                AppToolbar(
                    title = stringResource(R.string.styles),
                    showBackButton = true,
                    lifted = toolbarLifted
                )
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 12.dp + LocalPreviewBottomInset.current
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(styleList, key = { it.customStyle?.styleId ?: it.titleResId }) { style ->
                        StyleListItem(
                            style = style,
                            stylePalettes = stylePalettes,
                            isSelected = if (style.customStyle == null) {
                                style.monetStyle == selectedStyle && selectedCustomStyle == null
                            } else {
                                style.customStyle.styleId == selectedCustomStyle
                            },
                            onSelect = {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                if (style.customStyle == null) {
                                    selectedStyle = style.monetStyle
                                    selectedCustomStyle = null
                                    scope.launch {
                                        PreviewController.beginPreview()
                                        setCurrentMonetStyle(style.monetStyle)
                                        resetCustomStyle()
                                        setOriginalStyleName(style.titleResId.getStyleNameForRootless())
                                        applyColorScheme()
                                    }
                                } else {
                                    selectedCustomStyle = style.customStyle.styleId
                                    scope.launch {
                                        PreviewController.beginPreview()
                                        BackupRestore.restorePrefsMap(style.customStyle.prefsGson.toPrefs())
                                        setCurrentCustomStyle(style.customStyle.styleId)
                                        clearOriginalStyleName()
                                        applyColorScheme()
                                    }
                                }
                            },
                            onEdit = {
                                dialogState = StyleDialogState(
                                    styleId = style.customStyle!!.styleId,
                                    initialTitle = style.customStyle.styleName,
                                    initialDescription = style.customStyle.description
                                )
                            },
                            onUpdate = {
                                pendingUpdateStyleId = style.customStyle!!.styleId
                            },
                            onDelete = {
                                pendingDeleteStyleId = style.customStyle!!.styleId
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = rootMode && dialogState == null && !fabHidden,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = fabBottomPadding)
            ) {
                FloatingActionButton(
                    onClick = { dialogState = StyleDialogState() },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Add),
                        contentDescription = stringResource(R.string.save_current_style)
                    )
                }
            }
        }
    }

    pendingUpdateStyleId?.let { styleId ->
        ConfirmDialog(
            title = stringResource(R.string.update_style_confirmation_title),
            message = stringResource(R.string.update_style_confirmation_desc),
            confirmText = stringResource(R.string.update),
            onConfirm = {
                pendingUpdateStyleId = null
                scope.launch { stylesViewModel.updateCustomStyle(styleId) }
            },
            onDismiss = { pendingUpdateStyleId = null }
        )
    }

    pendingDeleteStyleId?.let { styleId ->
        ConfirmDialog(
            title = stringResource(R.string.delete_style_confirmation_title),
            message = stringResource(R.string.delete_style_confirmation_desc),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                pendingDeleteStyleId = null
                scope.launch { stylesViewModel.deleteCustomStyle(styleId) }
            },
            onDismiss = { pendingDeleteStyleId = null }
        )
    }

    dialogState?.let { state ->
        OutlinedTextFieldDialog(
            title = stringResource(R.string.save_current_style),
            firstFieldLabel = stringResource(R.string.title),
            secondFieldLabel = stringResource(R.string.description),
            confirmText = stringResource(R.string.save),
            dismissText = stringResource(AndroidR.string.cancel),
            initialFirstValue = state.initialTitle,
            initialSecondValue = state.initialDescription,
            onConfirm = { title, description ->
                dialogState = null

                if (title.trim().isEmpty() || description.trim().isEmpty()) {
                    AppSnackbar.show(
                        context.getString(R.string.title_and_desc_cant_be_empty)
                    )
                    return@OutlinedTextFieldDialog
                }

                scope.launch {
                    if (state.styleId == null) {
                        val newStyle = stylesViewModel.addCustomStyle(title, description)
                        selectedCustomStyle = newStyle.styleId
                        setCurrentCustomStyle(newStyle.styleId)
                    } else {
                        stylesViewModel.editCustomStyle(title, description, state.styleId)
                    }
                }
            },
            onDismiss = { dialogState = null }
        )
    }
}

@Composable
private fun StyleListItem(
    style: StyleModel,
    stylePalettes: Map<String, List<List<Int>>>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    if (style.customStyle == null) {
        StylePreviewCard(
            title = stringResource(style.titleResId),
            description = stringResource(style.descriptionResId),
            selected = isSelected,
            onSelect = onSelect,
            colorPalette = remember(style.titleResId, stylePalettes) {
                stylePalettes[style.titleResId.getOriginalString()]
            },
            enabled = style.isEnabled,
            disabledReason = if (style.disabledReason != 0) {
                stringResource(style.disabledReason)
            } else {
                null
            }
        )
    } else {
        StylePreviewCard(
            title = style.customStyle.styleName,
            description = style.customStyle.description,
            selected = isSelected,
            onSelect = onSelect,
            colorPalette = style.customStyle.palette,
            enabled = style.isEnabled,
            disabledReason = if (style.disabledReason != 0) {
                stringResource(style.disabledReason)
            } else {
                null
            },
            onEdit = onEdit,
            onUpdate = onUpdate,
            onDelete = onDelete
        )
    }
}

@Suppress("ViewModelConstructorInComposable")
@Preview
@Composable
private fun StylesScreenPreview() {
    ColorBlendrTheme {
        StylesScreen(
            stylesViewModel = StylesViewModel(Utilities.getCustomStyleRepository())
        )
    }
}

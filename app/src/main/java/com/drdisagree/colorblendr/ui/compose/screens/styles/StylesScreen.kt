package com.drdisagree.colorblendr.ui.compose.screens.styles

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.clearOriginalStyleName
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentCustomStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.setOriginalStyleName
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.config.Prefs.toPrefs
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.OutlinedTextFieldDialog
import com.drdisagree.colorblendr.ui.compose.components.StylePreviewCard
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.app.BackupRestore
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.getStyleNameForRootless
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.R as AndroidR

private data class StyleDialogState(
    val styleId: String? = null,
    val initialTitle: String = "",
    val initialDescription: String = ""
)

@Composable
fun StylesScreen(stylesViewModel: StylesViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val styleList by stylesViewModel.styleList.collectAsStateWithLifecycle()
    val stylePalettes by stylesViewModel.stylePalettes.collectAsStateWithLifecycle()

    val rootMode = remember { isRootMode() }
    var selectedStyle by remember { mutableStateOf(getCurrentMonetStyle()) }
    var selectedCustomStyle by remember { mutableStateOf(getCurrentCustomStyle()) }
    var dialogState by remember { mutableStateOf<StyleDialogState?>(null) }

    LaunchedEffect(styleList) {
        if (styleList.isNotEmpty() &&
            selectedCustomStyle != null &&
            !styleList.any { it.customStyle?.styleId == selectedCustomStyle }
        ) {
            resetCustomStyle()
            selectedCustomStyle = null
        }
        if (selectedCustomStyle == null && selectedStyle == null) {
            selectedStyle = MONET.TONAL_SPOT
        }
    }

    fun applyColorScheme() {
        scope.launch {
            updateColorAppliedTimestamp()
            withContext(Dispatchers.IO) {
                applyFabricatedColors()
            }
        }
    }

    // Mirrors HideViewOnScrollBehavior: FAB hides while scrolling down.
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousOffset by remember { mutableIntStateOf(0) }
    val scrollingDown by remember {
        derivedStateOf {
            val down = if (listState.firstVisibleItemIndex != previousIndex) {
                listState.firstVisibleItemIndex > previousIndex
            } else {
                listState.firstVisibleItemScrollOffset > previousOffset
            }
            previousIndex = listState.firstVisibleItemIndex
            previousOffset = listState.firstVisibleItemScrollOffset
            down
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
                    lifted = listState.firstVisibleItemIndex > 0 ||
                        listState.firstVisibleItemScrollOffset > 0
                )
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
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
                                if (style.customStyle == null) {
                                    selectedStyle = style.monetStyle
                                    selectedCustomStyle = null
                                    scope.launch {
                                        setCurrentMonetStyle(style.monetStyle)
                                        resetCustomStyle()
                                        setOriginalStyleName(style.titleResId.getStyleNameForRootless())
                                        applyColorScheme()
                                    }
                                } else {
                                    selectedCustomStyle = style.customStyle.styleId
                                    scope.launch {
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
                                MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.update_style_confirmation_title)
                                    .setMessage(R.string.update_style_confirmation_desc)
                                    .setPositiveButton(R.string.update) { _, _ ->
                                        scope.launch {
                                            stylesViewModel.updateCustomStyle(style.customStyle!!.styleId)
                                        }
                                    }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                            },
                            onDelete = {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.delete_style_confirmation_title)
                                    .setMessage(R.string.delete_style_confirmation_desc)
                                    .setPositiveButton(R.string.delete) { _, _ ->
                                        scope.launch {
                                            stylesViewModel.deleteCustomStyle(style.customStyle!!.styleId)
                                        }
                                    }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = rootMode && dialogState == null && !scrollingDown,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
            ) {
                FloatingActionButton(
                    onClick = { dialogState = StyleDialogState() },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.save_current_style)
                    )
                }
            }
        }
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
                    Toast.makeText(
                        context,
                        R.string.title_and_desc_cant_be_empty,
                        Toast.LENGTH_SHORT
                    ).show()
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

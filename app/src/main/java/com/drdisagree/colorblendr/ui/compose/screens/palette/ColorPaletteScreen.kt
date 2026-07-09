package com.drdisagree.colorblendr.ui.compose.screens.palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.manualColorOverrideEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.config.Prefs.clearPref
import com.drdisagree.colorblendr.data.config.Prefs.getInt
import com.drdisagree.colorblendr.data.config.Prefs.putInt
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbarHost
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.showSnackbarReplacing
import com.drdisagree.colorblendr.ui.compose.components.ColorTable
import com.drdisagree.colorblendr.ui.compose.components.WarningCard
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.utils.colors.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToHexColor
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView

private val colorCodes = intArrayOf(0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)

@Composable
fun ColorPaletteScreen(
    colorPaletteViewModel: ColorPaletteViewModel,
    fragmentManager: FragmentManager?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }
    val snackbarHostState = remember { SnackbarHostState() }

    val colorPalette by colorPaletteViewModel.colorPalette.collectAsStateWithLifecycle()

    val isOverrideAvailable = remember { isRootMode() && manualColorOverrideEnabled() }
    var overrideRevision by remember { mutableIntStateOf(0) }

    val cellColors = remember(colorPalette, overrideRevision) {
        colorPalette.mapIndexed { column, colors ->
            colors.mapIndexed { row, color ->
                val overridden = getInt(systemPaletteNames[column][row], Int.MIN_VALUE)
                if (overridden != Int.MIN_VALUE) overridden else color
            }
        }
    }

    fun updateColors(delayMillis: Long) {
        scope.launch {
            updateColorAppliedTimestamp()
            delay(delayMillis)
            withContext(Dispatchers.IO) {
                applyFabricatedColors()
            }
            colorPaletteViewModel.refreshData()
            overrideRevision++
        }
    }

    val cannotOverrideText = stringResource(R.string.cannot_override_color)
    val dismissText = stringResource(R.string.dismiss)
    val overrideText = stringResource(R.string.override)
    val copyText = stringResource(R.string.copy)

    fun showOverridePicker(column: Int, row: Int, currentColor: Int) {
        val manager = fragmentManager ?: return
        ColorPickerDialog()
            .withCornerRadius(24f)
            .withColor(currentColor)
            .withAlphaEnabled(false)
            .withPicker(ImagePickerView::class.java)
            .withListener { _: ColorPickerDialog?, color: Int ->
                if (currentColor != color) {
                    resetCustomStyleIfNotNull()
                    putInt(systemPaletteNames[column][row], color)
                    updateColors(200)
                }
            }
            .show(manager, "overrideColorPicker$column$row")
    }

    fun onCellClick(column: Int, row: Int) {
        val cellColor = cellColors.getOrNull(column)?.getOrNull(row) ?: return
        val manualOverride = manualColorOverrideEnabled()

        scope.launch {
            val result = snackbarHostState.showSnackbarReplacing(
                message = context.getString(R.string.color_code, intToHexColor(cellColor)),
                actionLabel = if (manualOverride) overrideText else copyText,
                duration = SnackbarDuration.Indefinite
            )
            if (result != SnackbarResult.ActionPerformed) return@launch

            if (!manualOverride || isShizukuMode()) {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        systemPaletteNames[column][row],
                        intToHexColor(cellColor)
                    )
                )
                return@launch
            }

            if (row == 0 || row == 12) {
                snackbarHostState.showSnackbarReplacing(
                    message = cannotOverrideText,
                    actionLabel = dismissText,
                    duration = SnackbarDuration.Short
                )
                return@launch
            }

            showOverridePicker(column, row, cellColor)
        }
    }

    fun onCellLongClick(column: Int, row: Int) {
        if (row == 0 || row == 12 ||
            getInt(systemPaletteNames[column][row], Int.MIN_VALUE) == Int.MIN_VALUE
        ) {
            return
        }

        resetCustomStyleIfNotNull()
        clearPref(systemPaletteNames[column][row])
        updateColors(0)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            Column {
                AppToolbar(
                    title = stringResource(R.string.color_palette_title),
                    showBackButton = true,
                    lifted = toolbarLifted
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = 12.dp)
                ) {
                    WarningCard(
                        warningText = stringResource(
                            if (isOverrideAvailable) {
                                R.string.color_palette_root_warn
                            } else {
                                R.string.color_palette_rootless_warn
                            }
                        ),
                        modifier = Modifier.padding(
                            bottom = dimensionResource(R.dimen.container_margin_bottom)
                        )
                    )
                    if (cellColors.isNotEmpty()) {
                        ColorTable(
                            colors = cellColors,
                            cellLabel = { _, row -> colorCodes[row].toString() },
                            cellTextColor = { column, row ->
                                calculateTextColor(cellColors[column][row])
                            },
                            onCellClick = ::onCellClick,
                            onCellLongClick = ::onCellLongClick
                        )
                    }
                }
            }

            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


@Suppress("ViewModelConstructorInComposable")
@Preview
@Composable
private fun ColorPaletteScreenPreview() {
    ColorBlendrTheme {
        ColorPaletteScreen(
            colorPaletteViewModel = ColorPaletteViewModel(),
            fragmentManager = null
        )
    }
}

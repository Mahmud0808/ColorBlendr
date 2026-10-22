package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.model.PaletteRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ColorPaletteViewModel : ViewModel() {

    private val _colorPalette = MutableStateFlow<List<List<Int>>>(emptyList())
    val colorPalette: StateFlow<List<List<Int>>> = _colorPalette.asStateFlow()

    private var paletteJob: Job? = null

    init {
        refreshData()

        viewModelScope.launch {
            RefreshCoordinator.refreshEvent.collect {
                refreshData()
            }
        }
    }

    fun refreshData() {
        loadPaletteColors()
    }

    fun loadPaletteColors() {
        // Before onboarding there is no wallpaper color list to generate from.
        if (isFirstRun() || isWorkMethodUnknown()) return

        paletteJob?.cancel()
        paletteJob = viewModelScope.launch(Dispatchers.IO) {
            // Pending changes show the staged palette; otherwise show the
            // actual colors the device currently resolves.
            val colorList = if (PreviewController.isPreviewActive) {
                generatePalette()
            } else {
                readSystemPalette()
            }
            if (colorList != _colorPalette.value) {
                _colorPalette.value = colorList
            }
        }
    }

    private fun generatePalette(): List<List<Int>> = ThemePrefs.scheme().shades

    // Falls back to generated rows where the framework has no matching
    // resources (error shades).
    private fun readSystemPalette(): List<List<Int>> {
        val resources = appContext.resources
        var generated: List<List<Int>>? = null

        return PaletteRole.shadeNames.mapIndexed { index, row ->
            val ids = row.map { resources.getIdentifier(it, "color", "android") }
            if (ids.all { it != 0 }) {
                ids.map { resources.getColor(it, appContext.theme) }
            } else {
                val palette = generated ?: generatePalette().also { generated = it }
                palette.getOrElse(index) { emptyList() }
            }
        }
    }
}
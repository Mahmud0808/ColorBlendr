package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPaletteViewModel @Inject constructor() : ViewModel() {

    private val _colorPalette = MutableStateFlow<List<List<Int>>>(emptyList())
    val colorPalette: StateFlow<List<List<Int>>> = _colorPalette.asStateFlow()

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

        viewModelScope.launch(Dispatchers.IO) {
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

    private fun generatePalette(): List<List<Int>> = generateModifiedColors(
        getCurrentMonetStyle(),
        getAccentSaturation(),
        getBackgroundSaturation(),
        getBackgroundLightness(),
        pitchBlackThemeEnabled(),
        accurateShadesEnabled()
    )

    // Falls back to generated rows where the framework has no matching
    // resources (error shades).
    private fun readSystemPalette(): List<List<Int>> {
        val resources = appContext.resources
        var generated: List<List<Int>>? = null

        return systemPaletteNames.mapIndexed { index, row ->
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

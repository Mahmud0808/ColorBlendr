package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
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
        viewModelScope.launch(Dispatchers.IO) {
            val colorList = generateModifiedColors(
                getCurrentMonetStyle(),
                getAccentSaturation(),
                getBackgroundSaturation(),
                getBackgroundLightness(),
                pitchBlackThemeEnabled(),
                accurateShadesEnabled()
            )
            if (colorList != _colorPalette.value) {
                _colorPalette.value = colorList
            }
        }
    }
}

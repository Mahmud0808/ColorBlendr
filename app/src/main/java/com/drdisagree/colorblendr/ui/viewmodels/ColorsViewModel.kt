package com.drdisagree.colorblendr.ui.viewmodels

import androidx.core.graphics.toColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ColorsViewModel : ViewModel() {

    private val _wallpaperColors = MutableLiveData<List<Int>>(emptyList())
    val wallpaperColors: LiveData<List<Int>> = _wallpaperColors

    private val _basicColors = MutableLiveData<List<Int>>(emptyList())
    val basicColors: LiveData<List<Int>> = _basicColors

    private val _wallpaperColorPalettes = MutableLiveData<Map<Int, List<List<Int>>>>(emptyMap())
    val wallpaperColorPalettes: LiveData<Map<Int, List<List<Int>>>> = _wallpaperColorPalettes

    private val _basicColorPalettes = MutableLiveData<Map<Int, List<List<Int>>>>(emptyMap())
    val basicColorPalettes: LiveData<Map<Int, List<List<Int>>>> = _basicColorPalettes

    init {
        refreshData()

        viewModelScope.launch {
            RefreshCoordinator.refreshEvent.collect {
                refreshData()
            }
        }
    }

    fun refreshData() {
        loadWallpaperColors()
        loadBasicColors()
    }

    fun loadWallpaperColors() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperColors = getWallpaperColors()
            if (wallpaperColors != _wallpaperColors.value) {
                _wallpaperColors.postValue(wallpaperColors)
            }

            val colorPalettes = loadPreviewColorPalettes(wallpaperColors)
            if (colorPalettes != _wallpaperColorPalettes.value) {
                _wallpaperColorPalettes.postValue(colorPalettes)
            }
        }
    }

    fun loadBasicColors() {
        viewModelScope.launch(Dispatchers.IO) {
            val basicColors: List<Int> = appContext.resources
                .getStringArray(R.array.basic_color_codes)
                .map { it.toColorInt() }
            if (basicColors != _basicColors.value) {
                _basicColors.postValue(basicColors)
            }

            val colorPalettes = loadPreviewColorPalettes(basicColors)
            if (colorPalettes != _basicColorPalettes.value) {
                _basicColorPalettes.postValue(colorPalettes)
            }
        }
    }

    private suspend fun loadPreviewColorPalettes(colors: List<Int>): Map<Int, List<List<Int>>> {
        return withContext(Dispatchers.IO) {
            val currentMonetStyle = getCurrentMonetStyle()
            val accentSaturation = getAccentSaturation()
            val backgroundSaturation = getBackgroundSaturation()
            val backgroundLightness = getBackgroundLightness()
            val pitchBlackThemeEnabled = pitchBlackThemeEnabled()
            val accurateShadesEnabled = accurateShadesEnabled()
            val isDarkMode = SystemUtil.isDarkMode

            colors.associateWith { color ->
                generateModifiedColors(
                    currentMonetStyle,
                    color,
                    accentSaturation,
                    backgroundSaturation,
                    backgroundLightness,
                    pitchBlackThemeEnabled,
                    accurateShadesEnabled,
                    false,
                    isDarkMode,
                    false
                )
            }
        }
    }

    private fun getWallpaperColors(): List<Int> {
        return Utilities.getWallpaperColorList().ifEmpty { ColorUtil.monetAccentColors }
    }
}

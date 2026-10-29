package com.drdisagree.colorblendr.ui.viewmodels

import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ColorsViewModel : ViewModel() {

    private val _wallpaperColors = MutableStateFlow<List<Int>>(emptyList())
    val wallpaperColors: StateFlow<List<Int>> = _wallpaperColors.asStateFlow()

    private val _basicColors = MutableStateFlow<List<Int>>(emptyList())
    val basicColors: StateFlow<List<Int>> = _basicColors.asStateFlow()

    private val _wallpaperColorPalettes = MutableStateFlow<Map<Int, List<List<Int>>>>(emptyMap())
    val wallpaperColorPalettes: StateFlow<Map<Int, List<List<Int>>>> =
        _wallpaperColorPalettes.asStateFlow()

    private val _basicColorPalettes = MutableStateFlow<Map<Int, List<List<Int>>>>(emptyMap())
    val basicColorPalettes: StateFlow<Map<Int, List<List<Int>>>> =
        _basicColorPalettes.asStateFlow()

    private val _customColorEnabled = MutableStateFlow(customColorEnabled())
    val customColorEnabled: StateFlow<Boolean> = _customColorEnabled.asStateFlow()

    private val _seedColor = MutableStateFlow(getSeedColorValue(0))
    val seedColor: StateFlow<Int> = _seedColor.asStateFlow()

    private var wallpaperJob: Job? = null
    private var basicJob: Job? = null

    init {
        refreshData()

        viewModelScope.launch {
            RefreshCoordinator.refreshEvent.collect {
                refreshData()
            }
        }
    }

    fun refreshData() {
        _customColorEnabled.value = customColorEnabled()
        _seedColor.value = getSeedColorValue(0)
        loadBasicColors()
        loadWallpaperColors()
    }

    // Keeps the flows in sync when the screen writes the prefs directly.
    fun onSeedColorSelected(color: Int, customColor: Boolean) {
        _seedColor.value = color
        _customColorEnabled.value = customColor
    }

    fun loadWallpaperColors() {
        wallpaperJob?.cancel()
        wallpaperJob = viewModelScope.launch(Dispatchers.IO) {
            val wallpaperColors = getWallpaperColors()
            if (wallpaperColors != _wallpaperColors.value) {
                _wallpaperColors.value = wallpaperColors
            }

            val colorPalettes = loadPreviewColorPalettes(wallpaperColors)
            if (colorPalettes != _wallpaperColorPalettes.value) {
                _wallpaperColorPalettes.value = colorPalettes
            }
        }
    }

    fun loadBasicColors() {
        basicJob?.cancel()
        basicJob = viewModelScope.launch(Dispatchers.IO) {
            val basicColors: List<Int> = appContext.resources
                .getStringArray(R.array.basic_color_codes)
                .map { it.toColorInt() }
            if (basicColors != _basicColors.value) {
                _basicColors.value = basicColors
            }

            val colorPalettes = loadPreviewColorPalettes(basicColors)
            if (colorPalettes != _basicColorPalettes.value) {
                _basicColorPalettes.value = colorPalettes
            }
        }
    }

    private suspend fun loadPreviewColorPalettes(colors: List<Int>): Map<Int, List<List<Int>>> {
        return withContext(Dispatchers.IO) {
            val adjustments = ThemePrefs.adjustments(includeOverrides = false)
            val isDarkMode = SystemUtil.isDarkMode

            colors.associateWith { color ->
                ThemePrefs.scheme(
                    isDark = isDarkMode,
                    source = ThemePrefs.source(seedColor = color),
                    adjustments = adjustments
                ).shades
            }
        }
    }

    private fun getWallpaperColors(): List<Int> {
        return Utilities.getWallpaperColorList().ifEmpty { ColorUtil.monetAccentColors }
    }
}
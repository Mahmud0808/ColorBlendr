package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.EXCLUDED_PREFS_FROM_BACKUP
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.config.Prefs.toGsonString
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.CustomStyleModel
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.data.repository.CustomStyleRepository
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.stringToEnumMonetStyle
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StylesViewModel(
    private val customStyleRepository: CustomStyleRepository
) : ViewModel() {

    private val _styleList = MutableStateFlow<List<StyleModel>>(emptyList())
    val styleList: StateFlow<List<StyleModel>> = _styleList.asStateFlow()

    private val _stylePalettes = MutableStateFlow<Map<String, List<List<Int>>>>(emptyMap())
    val stylePalettes: StateFlow<Map<String, List<List<Int>>>> = _stylePalettes.asStateFlow()

    init {
        refreshData()

        viewModelScope.launch {
            RefreshCoordinator.refreshEvent.collect {
                refreshData()
            }
        }
    }

    fun refreshData() {
        loadStylePalettes()
    }

    fun loadStylePalettes() {
        // Before onboarding there is no wallpaper color list to generate from.
        if (isFirstRun() || isWorkMethodUnknown()) return

        viewModelScope.launch(Dispatchers.IO) {
            val styleList = getStyleList()
            if (styleList != _styleList.value) {
                _styleList.value = styleList
            }

            val filteredStyleList = styleList.filter { style ->
                style.titleResId != 0 && style.customStyle == null
            }.map { it.titleResId.getOriginalString() }
            val previewColorPalettes = loadPreviewColorPalettes(filteredStyleList)
            if (previewColorPalettes != _stylePalettes.value) {
                _stylePalettes.value = previewColorPalettes
            }
        }
    }

    suspend fun addCustomStyle(title: String, description: String): CustomStyleModel {
        val allPrefs = Prefs.getAllPrefs()
        for (excludedPref in EXCLUDED_PREFS_FROM_BACKUP) {
            allPrefs.remove(excludedPref)
        }

        val currentMonet = getCurrentMonetStyle()
        val newStyle = CustomStyleModel(
            styleName = title.trim(),
            description = description.trim(),
            prefsGson = allPrefs.mapValues { it.value as Any }.toGsonString(),
            monet = currentMonet,
            palette = generateModifiedColors(
                currentMonet,
                getAccentSaturation(),
                getBackgroundSaturation(),
                getBackgroundLightness(),
                pitchBlackThemeEnabled(),
                accurateShadesEnabled()
            ),
            sortOrder = customStyleRepository.getNextSortOrder()
        )

        customStyleRepository.saveCustomStyle(newStyle)
        refreshData()
        return newStyle
    }

    suspend fun editCustomStyle(title: String, description: String, styleId: String) {
        val customStyle = customStyleRepository.getCustomStyleById(styleId) ?: return
        val updatedStyle = customStyle.copy(
            styleName = title.trim(),
            description = description.trim()
        )

        customStyleRepository.updateCustomStyle(updatedStyle)
        refreshData()
    }

    suspend fun updateCustomStyle(styleId: String): CustomStyleModel? {
        val customStyle = customStyleRepository.getCustomStyleById(styleId) ?: return null

        val allPrefs = Prefs.getAllPrefs()
        for (excludedPref in EXCLUDED_PREFS_FROM_BACKUP) {
            allPrefs.remove(excludedPref)
        }

        val currentMonet = getCurrentMonetStyle()
        val updatedStyle = customStyle.copy(
            prefsGson = allPrefs.mapValues { it.value as Any }.toGsonString(),
            monet = currentMonet,
            palette = generateModifiedColors(
                currentMonet,
                getAccentSaturation(),
                getBackgroundSaturation(),
                getBackgroundLightness(),
                pitchBlackThemeEnabled(),
                accurateShadesEnabled()
            )
        )

        customStyleRepository.updateCustomStyle(updatedStyle)
        refreshData()
        return updatedStyle
    }

    suspend fun deleteCustomStyle(styleId: String) {
        val customStyle = customStyleRepository.getCustomStyleById(styleId) ?: return
        customStyleRepository.deleteCustomStyle(customStyle)
        refreshData()
    }

    fun moveCustomStyle(fromStyleId: String, toStyleId: String) {
        val list = _styleList.value.toMutableList()
        val fromIndex = list.indexOfFirst { it.customStyle?.styleId == fromStyleId }
        val toIndex = list.indexOfFirst { it.customStyle?.styleId == toStyleId }
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return
        list.add(toIndex, list.removeAt(fromIndex))
        _styleList.value = list
    }

    fun persistCustomStyleOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            customStyleRepository.updateSortOrders(
                _styleList.value.mapNotNull { it.customStyle?.styleId }
            )
        }
    }

    private suspend fun loadPreviewColorPalettes(styleName: List<String>): Map<String, List<List<Int>>> {
        return withContext(Dispatchers.IO) {
            val accentSaturation = getAccentSaturation()
            val backgroundSaturation = getBackgroundSaturation()
            val backgroundLightness = getBackgroundLightness()
            val pitchBlackThemeEnabled = pitchBlackThemeEnabled()
            val accurateShadesEnabled = accurateShadesEnabled()

            styleName.associateWith { name ->
                generateModifiedColors(
                    stringToEnumMonetStyle(enumString = name),
                    accentSaturation,
                    backgroundSaturation,
                    backgroundLightness,
                    pitchBlackThemeEnabled,
                    accurateShadesEnabled
                )
            }
        }
    }

    private suspend fun getStyleList(): List<StyleModel> {
        val rootMode = isRootMode()
        val colorSpecVersion = Utilities.getColorSpecVersion()

        fun styleModel(titleResId: Int, descriptionResId: Int, style: MONET) = StyleModel(
            titleResId = titleResId,
            descriptionResId = descriptionResId,
            isEnabled = style.isAvailable(rootMode, colorSpecVersion),
            disabledReason = style.disabledReason(rootMode, colorSpecVersion),
            monetStyle = style
        )

        return arrayListOf(
            styleModel(R.string.monet_neutral, R.string.monet_neutral_desc, MONET.SPRITZ),
            styleModel(R.string.monet_monochrome, R.string.monet_monochrome_desc, MONET.MONOCHROMATIC),
            styleModel(R.string.monet_tonalspot, R.string.monet_tonalspot_desc, MONET.TONAL_SPOT),
            styleModel(R.string.monet_vibrant, R.string.monet_vibrant_desc, MONET.VIBRANT),
            styleModel(R.string.monet_rainbow, R.string.monet_rainbow_desc, MONET.RAINBOW),
            styleModel(R.string.monet_expressive, R.string.monet_expressive_desc, MONET.EXPRESSIVE),
            styleModel(R.string.monet_fidelity, R.string.monet_fidelity_desc, MONET.FIDELITY),
            styleModel(R.string.monet_content, R.string.monet_content_desc, MONET.CONTENT),
            styleModel(R.string.monet_fruitsalad, R.string.monet_fruitsalad_desc, MONET.FRUIT_SALAD),
            styleModel(R.string.monet_cmf, R.string.monet_cmf_desc, MONET.CMF)
        ).apply {
            if (!rootMode) return@apply

            customStyleRepository.getCustomStyles().forEach { customStyle ->
                add(
                    StyleModel(
                        isEnabled = true,
                        monetStyle = customStyle.monet,
                        customStyle = customStyle
                    )
                )
            }
        }
    }
}

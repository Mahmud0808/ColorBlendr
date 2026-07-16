package com.drdisagree.colorblendr.ui.viewmodels

import android.os.Build
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

    private val isAtleastA13 = isRootMode() ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val isAtleastA14 = isRootMode() ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

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
        return arrayListOf(
            StyleModel(
                titleResId = R.string.monet_neutral,
                descriptionResId = R.string.monet_neutral_desc,
                isEnabled = isAtleastA13,
                disabledReason = if (!isAtleastA13) R.string.android_13_required else 0,
                monetStyle = MONET.SPRITZ
            ),
            StyleModel(
                titleResId = R.string.monet_monochrome,
                descriptionResId = R.string.monet_monochrome_desc,
                isEnabled = isAtleastA14,
                disabledReason = if (!isAtleastA14) R.string.android_14_required else 0,
                monetStyle = MONET.MONOCHROMATIC
            ),
            StyleModel(
                titleResId = R.string.monet_tonalspot,
                descriptionResId = R.string.monet_tonalspot_desc,
                isEnabled = true,
                monetStyle = MONET.TONAL_SPOT
            ),
            StyleModel(
                titleResId = R.string.monet_vibrant,
                descriptionResId = R.string.monet_vibrant_desc,
                isEnabled = isAtleastA13,
                disabledReason = if (!isAtleastA13) R.string.android_13_required else 0,
                monetStyle = MONET.VIBRANT
            ),
            StyleModel(
                titleResId = R.string.monet_rainbow,
                descriptionResId = R.string.monet_rainbow_desc,
                isEnabled = isAtleastA13,
                disabledReason = if (!isAtleastA13) R.string.android_13_required else 0,
                monetStyle = MONET.RAINBOW
            ),
            StyleModel(
                titleResId = R.string.monet_expressive,
                descriptionResId = R.string.monet_expressive_desc,
                isEnabled = isAtleastA13,
                disabledReason = if (!isAtleastA13) R.string.android_13_required else 0,
                monetStyle = MONET.EXPRESSIVE
            ),
            StyleModel(
                titleResId = R.string.monet_fidelity,
                descriptionResId = R.string.monet_fidelity_desc,
                isEnabled = isRootMode(),
                disabledReason = if (!isRootMode()) R.string.root_required else 0,
                monetStyle = MONET.FIDELITY
            ),
            StyleModel(
                titleResId = R.string.monet_content,
                descriptionResId = R.string.monet_content_desc,
                isEnabled = isRootMode(),
                disabledReason = if (!isRootMode()) R.string.root_required else 0,
                monetStyle = MONET.CONTENT
            ),
            StyleModel(
                titleResId = R.string.monet_fruitsalad,
                descriptionResId = R.string.monet_fruitsalad_desc,
                isEnabled = isAtleastA13,
                disabledReason = if (!isAtleastA13) R.string.android_13_required else 0,
                monetStyle = MONET.FRUIT_SALAD
            ),
            StyleModel(
                titleResId = R.string.monet_cmf,
                descriptionResId = R.string.monet_cmf_desc,
                isEnabled = isRootMode() && Utilities.getColorSpecVersion() == 2,
                disabledReason = if (!isRootMode()) R.string.root_required else if (Utilities.getColorSpecVersion() != 2) R.string.colorspec_2026_required else 0,
                monetStyle = MONET.CMF
            )
        ).apply {
            if (!isRootMode()) return@apply

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

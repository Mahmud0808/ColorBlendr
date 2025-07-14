package com.drdisagree.colorblendr.ui.viewmodels

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.stringToEnumMonetStyle
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StylesViewModel : ViewModel() {

    private val isAtleastA13 = isRootMode() ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val isAtleastA14 = isRootMode() ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    private val _styleList = MutableLiveData<List<StyleModel>>(emptyList())
    val styleList: LiveData<List<StyleModel>> = _styleList

    private val _stylePalettes = MutableLiveData<Map<String, List<List<Int>>>>(emptyMap())
    val stylePalettes: LiveData<Map<String, List<List<Int>>>> = _stylePalettes

    init {
        refreshData()
    }

    fun refreshData() {
        loadStylePalettes()
    }

    fun loadStylePalettes() {
        viewModelScope.launch(Dispatchers.IO) {
            val styleList = getStyleList()
            if (styleList != _styleList.value) {
                _styleList.postValue(styleList)
            }

            val filteredStyleList = styleList.filter { style ->
                style.titleResId != 0 && style.customStyle == null
            }.map { it.titleResId.getOriginalString() }
            val previewColorPalettes = loadPreviewColorPalettes(filteredStyleList)
            if (previewColorPalettes != _stylePalettes.value) {
                _stylePalettes.postValue(previewColorPalettes)
            }
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
                monetStyle = MONET.SPRITZ
            ),
            StyleModel(
                titleResId = R.string.monet_monochrome,
                descriptionResId = R.string.monet_monochrome_desc,
                isEnabled = isAtleastA14,
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
                monetStyle = MONET.VIBRANT
            ),
            StyleModel(
                titleResId = R.string.monet_rainbow,
                descriptionResId = R.string.monet_rainbow_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.RAINBOW
            ),
            StyleModel(
                titleResId = R.string.monet_expressive,
                descriptionResId = R.string.monet_expressive_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.EXPRESSIVE
            ),
            StyleModel(
                titleResId = R.string.monet_fidelity,
                descriptionResId = R.string.monet_fidelity_desc,
                isEnabled = isRootMode(),
                monetStyle = MONET.FIDELITY
            ),
            StyleModel(
                titleResId = R.string.monet_content,
                descriptionResId = R.string.monet_content_desc,
                isEnabled = isRootMode(),
                monetStyle = MONET.CONTENT
            ),
            StyleModel(
                titleResId = R.string.monet_fruitsalad,
                descriptionResId = R.string.monet_fruitsalad_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.FRUIT_SALAD
            )
        ).apply {
            if (!isRootMode()) return@apply

            Utilities.getCustomStyleRepository().getCustomStyles().forEach { customStyle ->
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

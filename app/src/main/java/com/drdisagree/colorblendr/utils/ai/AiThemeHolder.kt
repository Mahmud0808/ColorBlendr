package com.drdisagree.colorblendr.utils.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.models.CommunityTheme

object AiThemeHolder {

    private const val MAX_HISTORY = 10

    val themes = mutableStateListOf<CommunityTheme>()

    var stagedTheme: CommunityTheme? by mutableStateOf(null)

    fun push(theme: CommunityTheme) {
        themes.add(0, theme)
        while (themes.size > MAX_HISTORY) {
            themes.removeAt(themes.lastIndex)
        }
    }

    fun remove(theme: CommunityTheme) {
        themes.removeAll { it.id == theme.id }
        if (stagedTheme?.id == theme.id) stagedTheme = null
    }

    fun stagedThemeIfCurrent(): CommunityTheme? {
        val staged = stagedTheme ?: return null
        return staged.takeIf {
            customColorEnabled() &&
                    getSeedColorValue() == it.seedColor &&
                    getCurrentMonetStyle() == it.style
        }
    }

    fun clear() {
        themes.clear()
        stagedTheme = null
    }
}
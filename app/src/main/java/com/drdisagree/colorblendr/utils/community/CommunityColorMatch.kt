package com.drdisagree.colorblendr.utils.community

import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.drdisagree.materialcolorutilities.hct.Hct
import kotlin.math.abs
import kotlin.math.min

// Hue-window color search for community themes. Same family = HCT hue within
// window; wider would cross into other colors. Low-chroma query matches
// neutral seeds instead; hue meaningless there. Thresholds mirror the
// website's search so both give identical results.
object CommunityColorMatch {

    private const val HUE_WINDOW = 30.0
    private const val NEUTRAL_CHROMA = 12.0
    private val HEX_QUERY = Regex("^#?([0-9a-fA-F]{6})$")

    @ColorInt
    fun parseQuery(query: String): Int? {
        val hex = HEX_QUERY.find(query.trim())?.groupValues?.get(1) ?: return null
        return "#$hex".toColorInt()
    }

    fun matches(@ColorInt seed: Int, @ColorInt target: Int): Boolean {
        val seedHct = Hct.fromInt(seed)
        val targetHct = Hct.fromInt(target)
        if (targetHct.chroma < NEUTRAL_CHROMA) return seedHct.chroma < NEUTRAL_CHROMA
        if (seedHct.chroma < NEUTRAL_CHROMA) return false
        val distance = abs(seedHct.hue - targetHct.hue)
        return min(distance, 360.0 - distance) <= HUE_WINDOW
    }
}

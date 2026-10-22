package com.drdisagree.colorblendr.utils.colors

import android.content.Context
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString

object ColorSchemeUtil {

    fun Int.getStyleNameForRootless(): String {
        return when (this.getOriginalString()) {
            R.string.monet_neutral.getOriginalString() -> "SPRITZ"
            R.string.monet_vibrant.getOriginalString() -> "VIBRANT"
            R.string.monet_expressive.getOriginalString() -> "EXPRESSIVE"
            R.string.monet_rainbow.getOriginalString() -> "RAINBOW"
            R.string.monet_fruitsalad.getOriginalString() -> "FRUIT_SALAD"
            R.string.monet_content.getOriginalString() -> "CONTENT"
            R.string.monet_monochrome.getOriginalString() -> "MONOCHROMATIC"
            R.string.monet_fidelity.getOriginalString() -> "FIDELITY"
            else -> "TONAL_SPOT"
        }
    }

    fun stringToEnumMonetStyle(
        context: Context = appContext,
        enumString: String
    ): MONET {
        // compare both original string and localized string
        return when (enumString) {
            R.string.monet_neutral.getOriginalString(),
            context.getString(R.string.monet_neutral) -> MONET.SPRITZ

            R.string.monet_monochrome.getOriginalString(),
            context.getString(R.string.monet_monochrome) -> MONET.MONOCHROMATIC

            R.string.monet_tonalspot.getOriginalString(),
            context.getString(R.string.monet_tonalspot) -> MONET.TONAL_SPOT

            R.string.monet_vibrant.getOriginalString(),
            context.getString(R.string.monet_vibrant) -> MONET.VIBRANT

            R.string.monet_rainbow.getOriginalString(),
            context.getString(R.string.monet_rainbow) -> MONET.RAINBOW

            R.string.monet_expressive.getOriginalString(),
            context.getString(R.string.monet_expressive) -> MONET.EXPRESSIVE

            R.string.monet_fidelity.getOriginalString(),
            context.getString(R.string.monet_fidelity) -> MONET.FIDELITY

            R.string.monet_content.getOriginalString(),
            context.getString(R.string.monet_content) -> MONET.CONTENT

            R.string.monet_fruitsalad.getOriginalString(),
            context.getString(R.string.monet_fruitsalad) -> MONET.FRUIT_SALAD

            R.string.monet_cmf.getOriginalString(),
            context.getString(R.string.monet_cmf) -> MONET.CMF

            else -> MONET.TONAL_SPOT
        }
    }
}
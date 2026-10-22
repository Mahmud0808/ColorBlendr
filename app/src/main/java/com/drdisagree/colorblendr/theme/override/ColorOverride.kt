package com.drdisagree.colorblendr.theme.override

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.theme.model.PaletteRole

sealed interface ColorOverride {

    data class Shade(val role: PaletteRole, val index: Int, val alpha: Int? = null) : ColorOverride

    data class Custom(@param:ColorInt val color: Int) : ColorOverride
}

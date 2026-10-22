package com.drdisagree.colorblendr.theme.override

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.theme.model.PaletteRole

sealed interface TokenOverride {

    data class Shade(val role: PaletteRole, val index: Int) : TokenOverride

    data class Custom(@param:ColorInt val color: Int) : TokenOverride
}

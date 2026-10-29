package com.drdisagree.colorblendr.theme.token

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.theme.model.PaletteRole

data class TokenResolution(
    val role: PaletteRole?,
    val tone: Double,
    val chromaMultiplier: Double = 1.0,
    val opacity: Double? = null,
    @param:ColorInt val color: Int? = null
)

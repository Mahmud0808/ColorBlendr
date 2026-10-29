package com.drdisagree.colorblendr.theme.model

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.data.enums.MONET

data class ThemeSource(
    val style: MONET,
    @param:ColorInt val seedColor: Int,
    @param:ColorInt val secondaryColor: Int? = null,
    @param:ColorInt val tertiaryColor: Int? = null,
    val specVersion: Int = 0,
    val contrast: Double = 0.0
)

package com.drdisagree.colorblendr.data.models

import androidx.annotation.ColorInt

data class AppColorResource(
    val name: String,
    @param:ColorInt val light: Int?,
    @param:ColorInt val dark: Int?
)

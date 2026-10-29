package com.drdisagree.colorblendr.ui.compose.screens.perapp

import androidx.annotation.ColorInt

data class AppColorEntry(
    val name: String,
    @param:ColorInt val color: Int?,
    val themed: Boolean,
    val preset: Boolean
)

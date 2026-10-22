package com.drdisagree.colorblendr.ui.compose.utils

import androidx.annotation.StringRes
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.theme.model.PaletteRole

@get:StringRes
val PaletteRole.labelRes: Int
    get() = when (this) {
        PaletteRole.PRIMARY -> R.string.primary
        PaletteRole.SECONDARY -> R.string.secondary
        PaletteRole.TERTIARY -> R.string.tertiary
        PaletteRole.NEUTRAL -> R.string.palette_neutral
        PaletteRole.NEUTRAL_VARIANT -> R.string.palette_neutral_variant
        PaletteRole.ERROR -> R.string.palette_error
    }

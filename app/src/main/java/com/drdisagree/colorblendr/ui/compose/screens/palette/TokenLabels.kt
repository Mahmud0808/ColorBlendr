package com.drdisagree.colorblendr.ui.compose.screens.palette

import androidx.annotation.StringRes
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenCategory

val Token.displayName: String
    get() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@get:StringRes
val TokenCategory.labelRes: Int
    get() = when (this) {
        TokenCategory.SURFACE -> R.string.color_tokens_category_surface
        TokenCategory.PRIMARY -> R.string.color_tokens_category_primary
        TokenCategory.SECONDARY -> R.string.color_tokens_category_secondary
        TokenCategory.TERTIARY -> R.string.color_tokens_category_tertiary
        TokenCategory.ERROR -> R.string.color_tokens_category_error
        TokenCategory.FIXED -> R.string.color_tokens_category_fixed
        TokenCategory.FRAMEWORK -> R.string.color_tokens_category_framework
        TokenCategory.SYSTEM_UI -> R.string.color_tokens_category_system_ui
        TokenCategory.KEY_COLOR -> R.string.color_tokens_category_key_color
    }

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

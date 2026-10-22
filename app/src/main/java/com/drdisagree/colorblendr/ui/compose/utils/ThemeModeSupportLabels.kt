package com.drdisagree.colorblendr.ui.compose.utils

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.ui.graphics.vector.ImageVector
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.ThemeModeSupport

val ThemeModeSupport.icon: ImageVector?
    get() = when (this) {
        ThemeModeSupport.BOTH -> null
        ThemeModeSupport.DARK_ONLY -> Icons.Rounded.DarkMode
        ThemeModeSupport.LIGHT_ONLY -> Icons.Rounded.LightMode
        ThemeModeSupport.LOW_CONTRAST -> Icons.Rounded.Contrast
    }

@get:StringRes
val ThemeModeSupport.labelRes: Int?
    get() = when (this) {
        ThemeModeSupport.BOTH -> null
        ThemeModeSupport.DARK_ONLY -> R.string.community_dark_only
        ThemeModeSupport.LIGHT_ONLY -> R.string.community_light_only
        ThemeModeSupport.LOW_CONTRAST -> R.string.community_low_contrast
    }

package com.drdisagree.colorblendr.utils.community

import androidx.compose.material3.ColorScheme
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.theme.toColorScheme

fun communityColorScheme(
    theme: CommunityTheme,
    isDark: Boolean,
    base: ColorScheme
): ColorScheme = CommunityThemePalette.scheme(theme, isDark).toColorScheme(base)

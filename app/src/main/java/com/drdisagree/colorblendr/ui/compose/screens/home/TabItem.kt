package com.drdisagree.colorblendr.ui.compose.screens.home

import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.navigation.Routes

internal data class TabItem(
    val route: String,
    val group: Int,
    val labelResId: Int,
    val filledIconResId: Int,
    val outlineIconResId: Int
)

internal val tabs = listOf(
    TabItem(Routes.COLORS, 1, R.string.colors, R.drawable.ic_nav_colors_filled, R.drawable.ic_nav_colors_outline),
    TabItem(Routes.THEME, 2, R.string.theme, R.drawable.ic_nav_theme_filled, R.drawable.ic_nav_theme_outline),
    TabItem(Routes.STYLES, 3, R.string.styles, R.drawable.ic_nav_styles_filled, R.drawable.ic_nav_styles_outline),
    TabItem(Routes.SETTINGS_BASE, 4, R.string.settings, R.drawable.ic_nav_settings_filled, R.drawable.ic_nav_settings_outline)
)

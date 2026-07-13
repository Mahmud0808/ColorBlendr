package com.drdisagree.colorblendr.ui.compose.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.ui.graphics.vector.ImageVector
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.navigation.Routes

internal data class TabItem(
    val route: String,
    val group: Int,
    val labelResId: Int,
    val filledIcon: ImageVector,
    val outlineIcon: ImageVector
)

internal val tabs = listOf(
    TabItem(Routes.COLORS, 1, R.string.colors, Icons.Rounded.Palette, Icons.Outlined.Palette),
    TabItem(Routes.THEME, 2, R.string.theme, Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder),
    TabItem(Routes.STYLES, 3, R.string.styles, Icons.Rounded.Style, Icons.Outlined.Style),
    TabItem(Routes.SETTINGS_BASE, 4, R.string.settings, Icons.Rounded.Settings, Icons.Outlined.Settings)
)

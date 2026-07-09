package com.drdisagree.colorblendr.ui.compose.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val PAIRING = "pairing"
    const val HOME = "home"

    const val COLORS = "colors"
    const val THEME = "theme"
    const val STYLES = "styles"
    const val SETTINGS = "settings?restoreUri={restoreUri}"
    const val SETTINGS_BASE = "settings"
    const val COLOR_PALETTE = "colorPalette"
    const val PER_APP_THEME = "perAppTheme"
    const val SETTINGS_ADVANCED = "settingsAdvanced"
    const val ABOUT = "about"
    const val PRIVACY_POLICY = "privacyPolicy"
}

// Mirrors FragmentUtil group 1-4 mapping used for slide directions and
// bottom bar highlighting.
fun tabGroup(route: String?): Int = when (route?.substringBefore("?")) {
    Routes.COLORS, Routes.COLOR_PALETTE -> 1
    Routes.THEME -> 2
    Routes.STYLES -> 3
    Routes.SETTINGS_BASE, Routes.SETTINGS_ADVANCED, Routes.ABOUT, Routes.PRIVACY_POLICY,
    Routes.PER_APP_THEME -> 4
    else -> 0
}

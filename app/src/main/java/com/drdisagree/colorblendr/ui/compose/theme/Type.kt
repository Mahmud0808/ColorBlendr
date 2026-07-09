package com.drdisagree.colorblendr.ui.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle

// XML text uses includeFontPadding=true; Compose defaults to false, shifting
// text metrics by 1-2px. Force the XML behavior for pixel parity.
private val fontPaddingStyle = PlatformTextStyle(includeFontPadding = true)

val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(platformStyle = fontPaddingStyle),
        displayMedium = displayMedium.copy(platformStyle = fontPaddingStyle),
        displaySmall = displaySmall.copy(platformStyle = fontPaddingStyle),
        headlineLarge = headlineLarge.copy(platformStyle = fontPaddingStyle),
        headlineMedium = headlineMedium.copy(platformStyle = fontPaddingStyle),
        headlineSmall = headlineSmall.copy(platformStyle = fontPaddingStyle),
        titleLarge = titleLarge.copy(platformStyle = fontPaddingStyle),
        titleMedium = titleMedium.copy(platformStyle = fontPaddingStyle),
        titleSmall = titleSmall.copy(platformStyle = fontPaddingStyle),
        bodyLarge = bodyLarge.copy(platformStyle = fontPaddingStyle),
        bodyMedium = bodyMedium.copy(platformStyle = fontPaddingStyle),
        bodySmall = bodySmall.copy(platformStyle = fontPaddingStyle),
        labelLarge = labelLarge.copy(platformStyle = fontPaddingStyle),
        labelMedium = labelMedium.copy(platformStyle = fontPaddingStyle),
        labelSmall = labelSmall.copy(platformStyle = fontPaddingStyle)
    )
}

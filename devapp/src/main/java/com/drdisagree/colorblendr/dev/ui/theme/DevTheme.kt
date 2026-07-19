package com.drdisagree.colorblendr.dev.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun DevTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (LocalInspectionMode.current) {
        expressiveLightColorScheme()
    } else if (isSystemInDarkTheme()) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
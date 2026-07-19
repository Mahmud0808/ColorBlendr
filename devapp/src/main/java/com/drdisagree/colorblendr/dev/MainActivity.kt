package com.drdisagree.colorblendr.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.drdisagree.colorblendr.dev.ui.screens.DevScreen
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import com.google.android.material.color.DynamicColors

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()
        setContent {
            DevTheme {
                DevScreen()
            }
        }
    }
}
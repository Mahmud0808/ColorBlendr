package com.drdisagree.colorblendr.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.drdisagree.colorblendr.dev.ui.DevScreen
import com.drdisagree.colorblendr.dev.ui.DevTheme
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

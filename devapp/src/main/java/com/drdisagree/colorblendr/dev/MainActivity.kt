package com.drdisagree.colorblendr.dev

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.drdisagree.colorblendr.dev.ui.screens.DevScreen
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import com.drdisagree.colorblendr.dev.utils.PendingCheckScheduler
import com.drdisagree.colorblendr.dev.utils.PendingNotifier
import com.google.android.material.color.DynamicColors

class MainActivity : ComponentActivity() {

    private var openPendingTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        PendingNotifier.ensureChannel(this)
        PendingCheckScheduler.sync(this)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            DevTheme {
                DevScreen(openPendingTick = openPendingTick)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(PendingNotifier.EXTRA_OPEN_PENDING, false) == true) {
            openPendingTick++
        }
    }
}

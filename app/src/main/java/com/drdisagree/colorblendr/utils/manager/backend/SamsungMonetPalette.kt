package com.drdisagree.colorblendr.utils.manager.backend

import android.os.SystemClock
import com.drdisagree.colorblendr.data.common.Utilities.getSamsungPaletteOriginalState
import com.drdisagree.colorblendr.data.common.Utilities.setSamsungPaletteOriginalState

object SamsungMonetPalette {

    private const val OVERLAY = "android:SemWT_G_MonetPalette"
    private const val CLEAR_WINDOW_MS = 3000L
    private const val POLL_INTERVAL_MS = 250L

    // Always ends enabled: Google widgets and the launcher feed read this
    // palette, and fall back to gray while it is disabled.
    fun refresh(shell: (String) -> String): Boolean {
        val listing = shell("cmd overlay list | grep \"$OVERLAY\"")
        if (OVERLAY !in listing) return false

        val isEnabled = "[x]" in listing
        if (getSamsungPaletteOriginalState() == null) setSamsungPaletteOriginalState(isEnabled)
        if (isEnabled) shell("cmd overlay disable $OVERLAY")
        shell("cmd overlay enable $OVERLAY")
        return true
    }

    fun restore(shell: (String) -> String): Boolean {
        if (OVERLAY !in shell("cmd overlay list | grep \"$OVERLAY\"")) return false

        val steps = if (getSamsungPaletteOriginalState() == false) {
            listOf("enable", "disable")
        } else {
            listOf("disable", "enable")
        }
        steps.forEach { step -> shell("cmd overlay $step $OVERLAY") }
        setSamsungPaletteOriginalState(null)
        return true
    }

    // Disabling the palette from shell makes One UI clear
    // theme_customization_overlay_packages about a second later, wiping our write.
    fun reassert(expected: String, read: () -> String?, write: (String) -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + CLEAR_WINDOW_MS
        while (SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(POLL_INTERVAL_MS)
            if (read()?.trim() != expected) return write(expected)
        }
        return true
    }
}

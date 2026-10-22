package com.drdisagree.colorblendr.utils.manager.backend

import com.drdisagree.colorblendr.data.common.Utilities.getSamsungPaletteOriginalState
import com.drdisagree.colorblendr.data.common.Utilities.setSamsungPaletteOriginalState

object SamsungMonetPalette {

    private const val OVERLAY = "android:SemWT_G_MonetPalette"

    fun flip(shell: (String) -> String) {
        val listing = shell("cmd overlay list | grep \"$OVERLAY\"")
        if (OVERLAY !in listing) return

        val isEnabled = "[x]" in listing
        if (getSamsungPaletteOriginalState() == null) setSamsungPaletteOriginalState(isEnabled)
        shell("cmd overlay ${if (isEnabled) "disable" else "enable"} $OVERLAY")
    }

    fun restore(shell: (String) -> String) {
        if (OVERLAY !in shell("cmd overlay list | grep \"$OVERLAY\"")) return

        val steps = if (getSamsungPaletteOriginalState() == false) {
            listOf("enable", "disable")
        } else {
            listOf("disable", "enable")
        }
        steps.forEach { step -> shell("cmd overlay $step $OVERLAY") }
        setSamsungPaletteOriginalState(null)
    }
}

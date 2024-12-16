package com.drdisagree.colorblendr.service

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.drdisagree.colorblendr.utils.SamsungPalette
import com.drdisagree.colorblendr.utils.SystemPalette
import com.topjohnwu.superuser.Shell
import kotlin.system.exitProcess

class ShizukuConnection : IShizukuConnection.Stub {

    companion object {
        private val TAG: String = ShizukuConnection::class.java.simpleName
    }

    @Suppress("unused")
    constructor() {
        Log.i(TAG, "Constructed with no arguments")
    }

    @Suppress("unused")
    @Keep
    constructor(context: Context) {
        Log.i(TAG, "Constructed with context: $context")
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }

    override fun applyFabricatedColors(jsonString: String) {
        SystemPalette.applyFabricatedColors(jsonString)
    }

    override fun applyFabricatedColorsSamsung(jsonString: String, paletteArray: String) {
        SamsungPalette.applySystemColors(jsonString, paletteArray)
    }

    override fun isThemedIconEnabledSamsung(): Boolean {
        return SamsungPalette.isThemedIconEnabled
    }

    override fun enableThemedIconSamsung(isThemed: Boolean) {
        SamsungPalette.enableThemedIcon(isThemed)
    }

    override fun removeFabricatedColors() {
        SystemPalette.removeFabricatedColors()
    }

    override fun removeFabricatedColorsSamsung() {
        SamsungPalette.removeSystemColors()
    }

    override fun getCurrentSettings(): String {
        return SystemPalette.currentSettings
    }

    override fun rebootDevice() {
        Shell.cmd("svc power reboot").exec()
    }
}

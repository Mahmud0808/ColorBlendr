package com.drdisagree.colorblendr.utils.manager.backend

import android.util.Log
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage.shellQuoted
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell

object WirelessAdbOverlayBackend : OverlayBackend {

    private const val TAG = "WirelessAdbOverlayBackend"

    override fun apply(): Boolean = withSettings { current ->
        val themeJson = ThemeOverlayPackage.themeCustomizationOverlayPackages.toString()
        if (themeJson.isEmpty()) return@withSettings true

        val samsung = SamsungMonetPalette.refresh(::shell)
        putSettings(ThemeOverlayPackage.mergeIntoSettings(current, themeJson), samsung)
    }

    override fun remove(): Boolean = withSettings { current ->
        val samsung = SamsungMonetPalette.restore(::shell)
        putSettings(ThemeOverlayPackage.getOriginalSettings(current), samsung)
    }

    private inline fun withSettings(action: (String) -> Boolean): Boolean {
        if (!WifiAdbShell.ensureConnected()) {
            Log.w(TAG, "Device not connected in wireless ADB mode")
            OverlayErrors.report(R.string.error_wireless_adb_unavailable)
            return false
        }

        return try {
            val current = WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES")
            if (!current.success) {
                OverlayErrors.reportOperation(current.output)
                return false
            }
            action(current.output)
        } catch (e: Exception) {
            Log.e(TAG, "Wireless ADB overlay operation failed", e)
            OverlayErrors.reportOperation(e)
            false
        }
    }

    private fun putSettings(json: String, samsung: Boolean): Boolean {
        return writeSettings(json) && (!samsung || SamsungMonetPalette.reassert(json, ::readSettings, ::writeSettings))
    }

    private fun writeSettings(json: String): Boolean {
        val result = WifiAdbShell.exec(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES ${json.shellQuoted()}"
        )
        if (result.success) return true
        Log.e(TAG, result.output)
        OverlayErrors.reportOperation(result.output)
        return false
    }

    private fun readSettings(): String? =
        WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES")
            .takeIf { it.success }?.output

    private fun shell(command: String): String = WifiAdbShell.exec(command).output
}

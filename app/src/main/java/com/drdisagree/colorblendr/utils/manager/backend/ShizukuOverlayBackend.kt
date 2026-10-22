package com.drdisagree.colorblendr.utils.manager.backend

import android.util.Log
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil

object ShizukuOverlayBackend : OverlayBackend {

    private const val TAG = "ShizukuOverlayBackend"

    override fun apply(): Boolean {
        val connection = connect() ?: return false
        return attempt {
            val currentSettings = connection.currentSettings
            val themeJson = ThemeOverlayPackage.themeCustomizationOverlayPackages.toString()
            if (themeJson.isEmpty()) return@attempt true

            SamsungMonetPalette.flip(connection::run)
            succeeded(
                connection.applyFabricatedColors(
                    ThemeOverlayPackage.mergeIntoSettings(currentSettings, themeJson)
                )
            )
        }
    }

    override fun remove(): Boolean {
        val connection = connect() ?: return false
        return attempt {
            SamsungMonetPalette.restore(connection::run)
            succeeded(connection.removeFabricatedColors())
        }
    }

    private fun succeeded(shellError: String?): Boolean {
        if (shellError == null) return true
        Log.e(TAG, shellError)
        OverlayErrors.reportOperation(shellError)
        return false
    }

    private inline fun attempt(action: () -> Boolean): Boolean = try {
        action()
    } catch (e: Exception) {
        Log.e(TAG, "Shizuku overlay operation failed", e)
        OverlayErrors.reportOperation(e)
        false
    }

    private fun connect(): IShizukuConnection? {
        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available")
            OverlayErrors.report(R.string.error_shizuku_unavailable)
            return null
        }
        return ShizukuConnectionProvider.connect() ?: null.also {
            Log.w(TAG, "Shizuku service connection is null")
            OverlayErrors.report(R.string.error_shizuku_service_unavailable)
        }
    }
}

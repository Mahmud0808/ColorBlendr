package com.drdisagree.colorblendr.utils.manager

import android.os.RemoteException
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.BLISS_LAUNCHER
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEMUI
import com.drdisagree.colorblendr.data.common.Constant.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.data.common.Utilities.getSamsungPaletteOriginalState
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbMode
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSamsungPaletteOriginalState
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.data.domain.ThemingErrorReporter
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage.shellQuoted
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.overlay.ThemeOverlays
import com.drdisagree.colorblendr.utils.app.ResourceUtil
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Suppress("unused")
object OverlayManager {

    private const val TAG = "OverlayManager"
    private val overlayLock = ReentrantLock()
    private lateinit var mRootConnection: IRootConnection
    private lateinit var mShizukuConnection: IShizukuConnection

    private fun ensureRootConnection(): Boolean {
        val connection = rootConnection?.takeIf { it.asBinder().isBinderAlive }
        if (connection == null) {
            Log.w(TAG, "Root service connection is unavailable")
            return false
        }
        mRootConnection = connection
        return true
    }

    private fun shizukuConnectionError(): String? {
        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available")
            return appContext.getString(R.string.error_shizuku_unavailable)
        }

        val connection = ShizukuConnectionProvider.connect()
        if (connection == null) {
            Log.w(TAG, "Shizuku service connection is null")
            return appContext.getString(R.string.error_shizuku_service_unavailable)
        }

        mShizukuConnection = connection
        return null
    }

    fun enableOverlay(packageName: String) {
        if (!isRootMode()) return
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return
        }

        try {
            mRootConnection.enableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to enable overlay: $packageName", e)
            reportError(overlayError(e))
        }
    }

    fun disableOverlay(packageName: String) {
        if (!isRootMode()) return
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return
        }

        try {
            mRootConnection.disableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to disable overlay: $packageName", e)
            reportError(overlayError(e))
        }
    }

    fun isOverlayInstalled(packageName: String): Boolean {
        if (!isRootMode() || !ensureRootConnection()) return false

        try {
            return mRootConnection.isOverlayInstalled(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlay is installed: $packageName", e)
            return false
        }
    }

    fun isOverlayEnabled(packageName: String): Boolean {
        if (!isRootMode() || !ensureRootConnection()) return false

        try {
            return mRootConnection.isOverlayEnabled(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlay is enabled: $packageName", e)
            return false
        }
    }

    fun areOverlaysEnabled(packageNames: List<String>): BooleanArray {
        if (packageNames.isEmpty()) return BooleanArray(0)
        if (!isRootMode() || !ensureRootConnection()) return BooleanArray(packageNames.size)

        return try {
            mRootConnection.areOverlaysEnabled(packageNames)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlays are enabled", e)
            BooleanArray(packageNames.size)
        }
    }

    fun uninstallOverlayUpdates(packageName: String) {
        if (!isRootMode()) return
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return
        }

        try {
            mRootConnection.uninstallOverlayUpdates(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to uninstall overlay updates: $packageName", e)
            reportError(overlayError(e))
        }
    }

    private fun registerFabricatedOverlay(fabricatedOverlay: FabricatedOverlayResource): Boolean {
        if (!isRootMode()) return false
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return false
        }

        return try {
            mRootConnection.registerFabricatedOverlay(fabricatedOverlay)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to register fabricated overlay: " + fabricatedOverlay.overlayName, e)
            reportError(overlayError(e))
            false
        }
    }

    fun unregisterFabricatedOverlay(packageName: String): Boolean =
        overlayLock.withLock { unregisterFabricatedOverlayLocked(packageName) }

    private fun unregisterFabricatedOverlayLocked(packageName: String): Boolean {
        if (!isRootMode()) return false
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return false
        }

        return try {
            mRootConnection.unregisterFabricatedOverlay(packageName)
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister fabricated overlay: $packageName", e)
            reportError(overlayError(e))
            false
        }
    }

    fun applyFabricatedColors(): Boolean = overlayLock.withLock { applyFabricatedColorsLocked() }

    private fun applyFabricatedColorsLocked(): Boolean {
        if (!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled()) return true

        applyFabricatedColorsNonRoot()?.let { return it }

        ResourceUtil.clearLookupCache()

        val theme = ThemePrefs.resolve()
        val isDarkMode = SystemUtil.isDarkMode

        val success = buildList {
            add(ThemeOverlays.framework(theme, isDarkMode))
            add(ThemeOverlays.systemUi(theme, isDarkMode))
            getSelectedFabricatedApps().filter { (packageName, isSelected) ->
                isSelected == java.lang.Boolean.TRUE && SystemUtil.isAppInstalled(packageName)
            }.forEach { (packageName) ->
                add(ThemeOverlays.perApp(packageName, theme, isDarkMode))
            }
        }.map { registerFabricatedOverlay(it) }.all { it }

        // Trigger a refresh to all the viewmodels
        RefreshCoordinator.triggerRefresh()

        return success
    }

    fun applyFabricatedColorsPerApp(packageName: String): Boolean = overlayLock.withLock {
        applyFabricatedColorsPerAppLocked(packageName)
    }

    private fun applyFabricatedColorsPerAppLocked(packageName: String): Boolean {
        if (!isThemingEnabled() || PreviewController.isPreviewActive) return true

        ResourceUtil.clearLookupCache()
        return registerFabricatedOverlay(
            ThemeOverlays.perApp(packageName, ThemePrefs.resolve(), SystemUtil.isDarkMode)
        )
    }

    fun removeFabricatedColors(): Boolean = overlayLock.withLock { removeFabricatedColorsLocked() }

    private fun removeFabricatedColorsLocked(): Boolean {
        removeFabricatedColorsNonRoot()?.let { return it }

        return ArrayList<String>().apply {
            getSelectedFabricatedApps().filter { (_, isSelected) ->
                isSelected == java.lang.Boolean.TRUE
            }.forEach { (packageName) ->
                add(
                    String.format(
                        FABRICATED_OVERLAY_NAME_APPS,
                        packageName
                    )
                )
            }

            add(FABRICATED_OVERLAY_NAME_SYSTEM)
            add(FABRICATED_OVERLAY_NAME_SYSTEMUI)
            add(String.format(FABRICATED_OVERLAY_NAME_APPS, BLISS_LAUNCHER))
        }.map { unregisterFabricatedOverlay(it) }.all { it }
    }

    private fun applyFabricatedColorsNonRoot(): Boolean? {
        val isShizukuMode = isShizukuMode()
        val isWirelessAdbMode = isWirelessAdbMode()

        if (!isShizukuMode && !isWirelessAdbMode) return null

        val themeJson = ThemeOverlayPackage.themeCustomizationOverlayPackages.toString()
        val samsungPaletteName = "android:SemWT_G_MonetPalette"
        var success = true

        if (isShizukuMode) {
            shizukuConnectionError()?.let { error ->
                reportError(error)
                return false
            }

            try {
                val currentSettings = mShizukuConnection.currentSettings

                if (themeJson.isNotEmpty()) {
                    val output =
                        mShizukuConnection.run("cmd overlay list | grep \"$samsungPaletteName\"")
                    if (output.contains(samsungPaletteName)) {
                        val isEnabled = output.contains("[x]")
                        if (getSamsungPaletteOriginalState() == null) {
                            setSamsungPaletteOriginalState(isEnabled)
                        }
                        mShizukuConnection.run("cmd overlay ${if (isEnabled) "disable" else "enable"} $samsungPaletteName")
                    }

                    // Null on success, shell error message on failure.
                    mShizukuConnection.applyFabricatedColors(
                        ThemeOverlayPackage.mergeIntoSettings(currentSettings, themeJson)
                    )?.let { error ->
                        Log.e(TAG, "applyFabricatedColorsNonRoot: $error")
                        reportError(
                            appContext.getString(R.string.error_overlay_operation, error)
                        )
                        success = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        } else {
            if (!WifiAdbShell.ensureConnected()) {
                Log.w(TAG, "Device not connected in wireless ADB mode")
                reportError(appContext.getString(R.string.error_wireless_adb_unavailable))
                return false
            }

            try {
                val current =
                    WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES")
                if (!current.success) {
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, current.output)
                    )
                    return false
                }

                if (themeJson.isNotEmpty()) {
                    // Grep exit 1 = palette absent; only the output matters.
                    val list =
                        WifiAdbShell.exec("cmd overlay list | grep \"$samsungPaletteName\"")
                    if (list.output.contains(samsungPaletteName)) {
                        val isEnabled = list.output.contains("[x]")
                        if (getSamsungPaletteOriginalState() == null) {
                            setSamsungPaletteOriginalState(isEnabled)
                        }
                        WifiAdbShell.exec("cmd overlay ${if (isEnabled) "disable" else "enable"} $samsungPaletteName")
                    }

                    val jsonString =
                        ThemeOverlayPackage.mergeIntoSettings(current.output, themeJson)
                    val put = WifiAdbShell
                        .exec("settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES ${jsonString.shellQuoted()}")
                    if (!put.success) {
                        Log.e(TAG, "applyFabricatedColorsNonRoot: ${put.output}")
                        reportError(
                            appContext.getString(R.string.error_overlay_operation, put.output)
                        )
                        success = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        }

        // Trigger a refresh to all the viewmodels
        RefreshCoordinator.triggerRefresh()

        return success
    }

    private fun removeFabricatedColorsNonRoot(): Boolean? {
        val isShizukuMode = isShizukuMode()
        val isWirelessAdbMode = isWirelessAdbMode()

        if (!isShizukuMode && !isWirelessAdbMode) return null

        val samsungPaletteName = "android:SemWT_G_MonetPalette"
        var success = true

        if (isShizukuMode) {
            shizukuConnectionError()?.let { error ->
                reportError(error)
                return false
            }

            try {
                if (mShizukuConnection
                        .run("cmd overlay list | grep \"$samsungPaletteName\"")
                        .contains(samsungPaletteName)
                ) {
                    samsungPaletteRestoreSteps().forEach { step ->
                        mShizukuConnection.run("cmd overlay $step $samsungPaletteName")
                    }
                    setSamsungPaletteOriginalState(null)
                }

                // Null on success, shell error message on failure.
                mShizukuConnection.removeFabricatedColors()?.let { error ->
                    Log.e(TAG, "removeFabricatedColorsNonRoot: $error")
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, error)
                    )
                    success = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        } else {
            if (!WifiAdbShell.ensureConnected()) {
                Log.w(TAG, "Device not connected in wireless ADB mode")
                reportError(appContext.getString(R.string.error_wireless_adb_unavailable))
                return false
            }

            try {
                val current =
                    WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES")
                if (!current.success) {
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, current.output)
                    )
                    return false
                }

                // Grep exit 1 = palette absent; only the output matters.
                val list = WifiAdbShell.exec("cmd overlay list | grep \"$samsungPaletteName\"")
                if (list.output.contains(samsungPaletteName)) {
                    samsungPaletteRestoreSteps().forEach { step ->
                        WifiAdbShell.exec("cmd overlay $step $samsungPaletteName")
                    }
                    setSamsungPaletteOriginalState(null)
                }

                val jsonString = ThemeOverlayPackage.getOriginalSettings(current.output)
                val put = WifiAdbShell
                    .exec("settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES ${jsonString.shellQuoted()}")
                if (!put.success) {
                    Log.e(TAG, "removeFabricatedColorsNonRoot: ${put.output}")
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, put.output)
                    )
                    success = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        }

        return success
    }

    private fun samsungPaletteRestoreSteps(): List<String> =
        if (getSamsungPaletteOriginalState() == false) {
            listOf("enable", "disable")
        } else {
            listOf("disable", "enable")
        }

    private fun reportError(message: String) {
        ThemingErrorReporter.report(message)
    }

    private fun overlayError(e: Exception): String =
        appContext.getString(
            R.string.error_overlay_operation,
            e.message ?: e.javaClass.simpleName
        )
}

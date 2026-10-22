package com.drdisagree.colorblendr.utils.manager.backend

import android.os.RemoteException
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.BLISS_LAUNCHER
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEMUI
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.overlay.ThemeOverlays
import com.drdisagree.colorblendr.utils.app.ResourceUtil
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

object RootOverlayBackend : OverlayBackend {

    private const val TAG = "RootOverlayBackend"
    private val PACKAGE_NAME = Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")

    override fun apply(): Boolean {
        ResourceUtil.clearLookupCache()
        val theme = ThemePrefs.resolve()
        val isDarkMode = SystemUtil.isDarkMode

        val overlays = buildList {
            add(ThemeOverlays.framework(theme, isDarkMode))
            add(ThemeOverlays.systemUi(theme, isDarkMode))
            selectedApps()
                .filter(SystemUtil::isAppInstalled)
                .forEach { add(ThemeOverlays.perApp(it, theme, isDarkMode)) }
        }
        return overlays.map(::register).all { it }
    }

    override fun remove(): Boolean {
        val overlayNames = selectedApps().map(::perAppOverlayName) + listOf(
            FABRICATED_OVERLAY_NAME_SYSTEM,
            FABRICATED_OVERLAY_NAME_SYSTEMUI,
            perAppOverlayName(BLISS_LAUNCHER),
            perAppOverlayName(appContext.packageName)
        )
        return overlayNames.map(::unregister).all { it }
    }

    fun applyPerApp(packageName: String): Boolean {
        ResourceUtil.clearLookupCache()
        return register(ThemeOverlays.perApp(packageName, ThemePrefs.resolve(), SystemUtil.isDarkMode))
    }

    fun unregister(overlayName: String): Boolean = command(false, overlayName) {
        it.unregisterFabricatedOverlay(overlayName)
        true
    }

    fun forceStop(packageName: String): Boolean {
        if (!PACKAGE_NAME.matches(packageName)) return false
        return command(false, packageName) {
            it.runCommand(listOf("am force-stop $packageName"))
            true
        }
    }

    fun isEnabled(overlayName: String): Boolean = query(false) { it.isOverlayEnabled(overlayName) }

    fun areEnabled(overlayNames: List<String>): BooleanArray {
        if (overlayNames.isEmpty()) return BooleanArray(0)
        return query(BooleanArray(overlayNames.size)) { it.areOverlaysEnabled(overlayNames) }
    }

    private fun register(overlay: FabricatedOverlayResource): Boolean =
        command(false, overlay.overlayName) { it.registerFabricatedOverlay(overlay) }

    private fun selectedApps(): List<String> =
        getSelectedFabricatedApps()
            .filter { (packageName, selected) -> selected && packageName != appContext.packageName }
            .keys.toList()

    private fun perAppOverlayName(packageName: String) =
        String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)

    private inline fun <T> command(fallback: T, target: String, action: (IRootConnection) -> T): T {
        if (!isRootMode()) return fallback
        val connection = connection() ?: run {
            OverlayErrors.report(R.string.error_root_unavailable)
            return fallback
        }
        return try {
            action(connection)
        } catch (e: RemoteException) {
            Log.e(TAG, "Root overlay operation failed: $target", e)
            OverlayErrors.reportOperation(e)
            fallback
        }
    }

    private inline fun <T> query(fallback: T, action: (IRootConnection) -> T): T {
        if (!isRootMode()) return fallback
        val connection = connection() ?: return fallback
        return try {
            action(connection)
        } catch (e: RemoteException) {
            Log.e(TAG, "Root overlay query failed", e)
            fallback
        }
    }

    private fun connection(): IRootConnection? =
        rootConnection?.takeIf { it.asBinder().isBinderAlive }
            ?: null.also { Log.w(TAG, "Root service connection is unavailable") }
}

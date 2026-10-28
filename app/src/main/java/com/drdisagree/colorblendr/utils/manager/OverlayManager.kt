package com.drdisagree.colorblendr.utils.manager

import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbMode
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil
import com.drdisagree.colorblendr.utils.manager.backend.OverlayBackend
import com.drdisagree.colorblendr.utils.manager.backend.RootOverlayBackend
import com.drdisagree.colorblendr.utils.manager.backend.ShizukuOverlayBackend
import com.drdisagree.colorblendr.utils.manager.backend.WirelessAdbOverlayBackend
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object OverlayManager {

    private val lock = ReentrantLock()

    private val backend: OverlayBackend
        get() = when {
            isShizukuMode() -> ShizukuOverlayBackend
            isWirelessAdbMode() -> WirelessAdbOverlayBackend
            else -> RootOverlayBackend
        }

    fun applyFabricatedColors(): Boolean = lock.withLock {
        if (!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled()) {
            return@withLock true
        }
        FabricatedUtil.syncPitchBlackApps().forEach { packageName ->
            RootOverlayBackend.unregister(String.format(FABRICATED_OVERLAY_NAME_APPS, packageName))
        }
        backend.apply().also { RefreshCoordinator.triggerRefresh() }
    }

    fun removeFabricatedColors(): Boolean = lock.withLock { backend.remove() }

    fun applyFabricatedColorsPerApp(packageName: String): Boolean = lock.withLock {
        if (!isThemingEnabled() || PreviewController.isPreviewActive) return@withLock true
        RootOverlayBackend.applyPerApp(packageName)
    }

    fun unregisterFabricatedOverlay(overlayName: String): Boolean = lock.withLock {
        RootOverlayBackend.unregister(overlayName)
    }

    fun restartApp(packageName: String): Boolean {
        if (!RootOverlayBackend.forceStop(packageName)) return false
        SystemUtil.launchApp(packageName)
        return true
    }

    fun isOverlayEnabled(overlayName: String): Boolean? = lock.withLock {
        RootOverlayBackend.isEnabled(overlayName)
    }

    fun areOverlaysEnabled(overlayNames: List<String>): BooleanArray? = lock.withLock {
        RootOverlayBackend.areEnabled(overlayNames)
    }
}

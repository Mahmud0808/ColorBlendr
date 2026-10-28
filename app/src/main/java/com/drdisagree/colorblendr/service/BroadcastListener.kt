package com.drdisagree.colorblendr.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.drdisagree.colorblendr.data.common.Constant
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getLastColorAppliedTimestamp
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorJson
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.screenOffColorUpdateEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.setWallpaperColorJson
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.domain.AppEvents
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.utils.app.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.forgetPitchBlackAutoSelection
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.selectPitchBlackApp
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColorsPerApp
import com.drdisagree.colorblendr.utils.manager.OverlayManager.unregisterFabricatedOverlay
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperColorUtil.getWallpaperColors
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperShortcuts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@Suppress("DEPRECATION")
class BroadcastListener : BroadcastReceiver() {

    private val handler = Handler(Looper.getMainLooper())
    private var sleepRunnable: Runnable? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var screenOffWakeLock: PowerManager.WakeLock? = null

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: " + intent.action)

        if (isLastConfigInitialized.not()) {
            lastConfig = Configuration(context.resources.configuration)
        }

        if (intent.action == Intent.ACTION_SCREEN_OFF && screenOffColorUpdateEnabled()) {
            releaseScreenOffWakeLock()
            screenOffWakeLock = context.getSystemService(PowerManager::class.java)
                ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SCREEN_OFF_WAKE_LOCK_TAG)
                ?.apply { acquire(SCREEN_OFF_WAKE_LOCK_TIMEOUT) }
        }

        CoroutineScope(Dispatchers.Main).launch {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_LOCKED_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                "android.intent.action.QUICKBOOT_POWERON" -> {
                    handleBootCompleted(context)
                }

                Intent.ACTION_WALLPAPER_CHANGED,
                Intent.ACTION_USER_FOREGROUND -> {
                    handleWallpaperChanged(context, true)
                }

                Intent.ACTION_SCREEN_OFF -> {
                    if (screenOffColorUpdateEnabled()) {
                        sleepRunnable = Runnable {
                            coroutineScope.launch {
                                handleWallpaperChanged(context)
                            }
                        }
                        handler.postDelayed(sleepRunnable!!, 15000) // 15 seconds
                    }
                }

                Intent.ACTION_SCREEN_ON -> {
                    sleepRunnable?.let { runnable ->
                        handler.removeCallbacks(runnable)
                        sleepRunnable = null
                    }
                    releaseScreenOffWakeLock()
                }

                Intent.ACTION_CONFIGURATION_CHANGED -> {
                    delay(1000.milliseconds)

                    val newConfig = Configuration(context.resources.configuration)
                    val lastUiMode = lastConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val newUiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK

                    if (lastUiMode != newUiMode) {
                        validateRootAndUpdateColors {
                            updateAllColors(true)
                        }
                        WallpaperShortcuts.update(context)
                    }

                    lastConfig = newConfig
                }

                Intent.ACTION_PACKAGE_ADDED -> {
                    handlePackageAdded(intent)
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    handlePackageRemoved(intent)
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    handlePackageReplaced(intent)
                }
            }

            if (intent.action == Intent.ACTION_WALLPAPER_CHANGED) {
                AppEvents.emit(intent.action!!)
            }

            val packageChanged = when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED -> true
                Intent.ACTION_PACKAGE_REMOVED -> !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                else -> false
            }
            if (packageChanged) {
                intent.data?.schemeSpecificPart?.let(AppEvents::emitPackageChanged)
            }
        }
    }

    private suspend fun handleBootCompleted(context: Context) {
        if (permissionsGranted(context) && AutoStartService.isServiceNotRunning) {
            context.startForegroundService(Intent(context, AutoStartService::class.java))
        }

        validateRootAndUpdateColors {
            cooldownTime = 10000
            CoroutineScope(Dispatchers.Main).launch {
                delay(10000.milliseconds)
                cooldownTime = 5000
            }
            updateAllColors()
        }
    }

    fun onWallpaperColorsChanged(context: Context) {
        coroutineScope.launch {
            handleWallpaperChanged(context, live = true)
        }
    }

    private suspend fun handleWallpaperChanged(
        context: Context,
        force: Boolean = false,
        live: Boolean = false
    ) {
        if (permissionsGranted(context)) {
            val wallpaperColors = withContext(Dispatchers.IO) {
                getWallpaperColors(context)
            }

            val previousWallpaperColors = getWallpaperColorJson()
            val currentWallpaperColors = Constant.GSON.toJson(wallpaperColors)
            val changed = previousWallpaperColors != currentWallpaperColors

            if (live && !changed) return

            if (!requiresUpdate) {
                requiresUpdate = changed
            }

            setWallpaperColorJson(currentWallpaperColors)
            if (changed) WallpaperShortcuts.update(context)
            if (live) AppEvents.emit(Intent.ACTION_WALLPAPER_CHANGED)

            if (PreviewController.isPreviewActive) {
                if (requiresUpdate || force) PreviewController.markSystemUpdatePending()
                requiresUpdate = false
                return
            }

            if (!customColorEnabled()) {
                setSeedColorValue(wallpaperColors[0])
            }
        }

        if (live && screenOffColorUpdateEnabled() && context.isInteractive()) return

        if (requiresUpdate || force) {
            requiresUpdate = false
            validateRootAndUpdateColors {
                updateAllColors(true)
            }
        }
    }

    private suspend fun handlePackageAdded(intent: Intent) {
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return

        intent.data?.schemeSpecificPart?.let { packageName ->
            if (selectPitchBlackApp(packageName)) {
                validateRootAndUpdateColors {
                    applyFabricatedColorsPerApp(packageName)
                }
            }
        }
    }

    private suspend fun handlePackageRemoved(intent: Intent) {
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return

        intent.data?.schemeSpecificPart?.let { packageName ->
            forgetPitchBlackAutoSelection(packageName)

            val selectedApps: HashMap<String, Boolean> = getSelectedFabricatedApps()

            if (selectedApps.containsKey(packageName) && selectedApps[packageName] == true) {
                selectedApps.remove(packageName)
                setSelectedFabricatedApps(selectedApps)

                validateRootAndUpdateColors {
                    unregisterFabricatedOverlay(
                        String.format(
                            FABRICATED_OVERLAY_NAME_APPS,
                            packageName
                        )
                    )
                }
            }
        }
    }

    private suspend fun handlePackageReplaced(intent: Intent) {
        intent.data?.schemeSpecificPart?.let { packageName ->
            val selectedApps: HashMap<String, Boolean> = getSelectedFabricatedApps()

            if (selectedApps[packageName] == true || selectPitchBlackApp(packageName)) {
                validateRootAndUpdateColors {
                    applyFabricatedColorsPerApp(packageName)
                }
            }
        }
    }

    private suspend fun validateRootAndUpdateColors(method: suspend () -> Unit) {
        if (isRootMode() && RootConnectionProvider.isNotConnected) {
            RootConnectionProvider
                .builder()
                .onSuccess {
                    CoroutineScope(Dispatchers.Main).launch {
                        method()
                    }
                }
                .run()
        } else {
            method()
        }
    }

    @Synchronized
    private fun updateAllColors(force: Boolean = false) {
        if ((!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled())
            || isWorkMethodUnknown()
        ) return

        if (PreviewController.isPreviewActive) {
            PreviewController.markSystemUpdatePending()
            return
        }

        if (abs(getLastColorAppliedTimestamp() - System.currentTimeMillis()) >= cooldownTime || force) {
            updateColorAppliedTimestamp()

            applyJob?.cancel()
            applyJob = CoroutineScope(Dispatchers.IO).launch {
                delay(500.milliseconds)
                applyFabricatedColors()
            }
        }
    }

    private fun releaseScreenOffWakeLock() {
        screenOffWakeLock?.takeIf { it.isHeld }?.release()
        screenOffWakeLock = null
    }

    private fun Context.isInteractive(): Boolean =
        getSystemService(PowerManager::class.java)?.isInteractive != false

    companion object {
        private val TAG: String = "BroadcastListener"
        var requiresUpdate = false
        private var cooldownTime: Long = 5000
        private var applyJob: Job? = null
        private const val SCREEN_OFF_WAKE_LOCK_TAG = "ColorBlendr:ScreenOffUpdate"
        private const val SCREEN_OFF_WAKE_LOCK_TIMEOUT = 30_000L
        lateinit var lastConfig: Configuration
        val isLastConfigInitialized: Boolean
            get() = ::lastConfig.isInitialized
    }
}

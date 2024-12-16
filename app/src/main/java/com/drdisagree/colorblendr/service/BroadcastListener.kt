package com.drdisagree.colorblendr.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.common.Const.SCREEN_OFF_UPDATE_COLORS
import com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.common.Const.isRootMode
import com.drdisagree.colorblendr.common.Const.isUnknownMode
import com.drdisagree.colorblendr.common.Const.rootedThemingEnabled
import com.drdisagree.colorblendr.common.Const.saveSelectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.selectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.shizukuThemingEnabled
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getLong
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.config.RPrefs.putString
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.utils.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColorsPerApp
import com.drdisagree.colorblendr.utils.OverlayManager.unregisterFabricatedOverlay
import com.drdisagree.colorblendr.utils.SystemUtil.getScreenRotation
import com.drdisagree.colorblendr.utils.WallpaperColorUtil.getWallpaperColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class BroadcastListener : BroadcastReceiver() {

    private val handler = Handler(Looper.getMainLooper())
    private var sleepRunnable: Runnable? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @Suppress("deprecation")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: " + intent.action)

        if (lastOrientation == -1) {
            lastOrientation = getScreenRotation(context)
        }

        val currentOrientation = getScreenRotation(context)
        val screenOffUpdate = getBoolean(SCREEN_OFF_UPDATE_COLORS, false)

        CoroutineScope(Dispatchers.Main).launch {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                    handleBootCompleted(context)
                }

                Intent.ACTION_WALLPAPER_CHANGED -> {
                    handleWallpaperChanged(context)
                }

                Intent.ACTION_SCREEN_OFF -> {
                    if (screenOffUpdate) {
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
                }

                Intent.ACTION_CONFIGURATION_CHANGED -> {
                    if (lastOrientation == currentOrientation) {
                        validateRootAndUpdateColors(context) {
                            updateAllColors()
                        }
                    }

                    lastOrientation = currentOrientation
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    handlePackageRemoved(context, intent)
                }

                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    handlePackageReplaced(context, intent)
                }
            }

            if (intent.action in listOf(
                    Intent.ACTION_PACKAGE_ADDED,
                    Intent.ACTION_PACKAGE_REMOVED,
                    Intent.ACTION_WALLPAPER_CHANGED
                )
            ) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }
    }

    private suspend fun handleBootCompleted(context: Context) {
        if (permissionsGranted(context) && AutoStartService.isServiceNotRunning) {
            context.startForegroundService(Intent(context, AutoStartService::class.java))
        }

        validateRootAndUpdateColors(context) {
            cooldownTime = 10000
            CoroutineScope(Dispatchers.Main).launch {
                delay(10000)
                cooldownTime = 5000
            }
            updateAllColors()
        }
    }

    private suspend fun handleWallpaperChanged(context: Context) {
        if (permissionsGranted(context)) {
            val wallpaperColors = withContext(Dispatchers.IO) {
                getWallpaperColors(context)
            }
            putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors))

            if (!getBoolean(MONET_SEED_COLOR_ENABLED, false)) {
                putInt(MONET_SEED_COLOR, wallpaperColors[0])
            }
        }

        validateRootAndUpdateColors(context) {
            updateAllColors()
        }
    }

    private suspend fun handlePackageRemoved(context: Context, intent: Intent) {
        intent.data?.schemeSpecificPart?.let { packageName ->
            val selectedApps: HashMap<String, Boolean> = selectedFabricatedApps

            if (selectedApps.containsKey(packageName) && selectedApps[packageName] == true) {
                selectedApps.remove(packageName)
                saveSelectedFabricatedApps(selectedApps)

                validateRootAndUpdateColors(context) {
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

    private suspend fun handlePackageReplaced(context: Context, intent: Intent) {
        intent.data?.schemeSpecificPart?.let { packageName ->
            val selectedApps = selectedFabricatedApps

            if (selectedApps.containsKey(packageName) && selectedApps[packageName] == true) {
                validateRootAndUpdateColors(context) {
                    applyFabricatedColorsPerApp(packageName, null)
                }
            }
        }
    }

    private suspend fun validateRootAndUpdateColors(context: Context, method: suspend () -> Unit) {
        if (isRootMode && RootConnectionProvider.isNotConnected) {
            RootConnectionProvider
                .builder(context)
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

    private fun updateAllColors() {
        if ((!rootedThemingEnabled && !shizukuThemingEnabled) || isUnknownMode) return

        if (abs(
                (getLong(
                    MONET_LAST_UPDATED,
                    0
                ) - System.currentTimeMillis()).toDouble()
            ) >= cooldownTime
        ) {
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())

            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                applyFabricatedColors()
            }
        }
    }

    companion object {
        private val TAG: String = BroadcastListener::class.java.simpleName
        var lastOrientation: Int = -1
        private var cooldownTime: Long = 5000
    }
}

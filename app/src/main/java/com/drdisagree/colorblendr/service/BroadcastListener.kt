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
import com.drdisagree.colorblendr.common.Const.SHIZUKU_THEMING_ENABLED
import com.drdisagree.colorblendr.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.common.Const.saveSelectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.selectedFabricatedApps
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getLong
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.config.RPrefs.putString
import com.drdisagree.colorblendr.extension.MethodInterface
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.utils.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColorsPerApp
import com.drdisagree.colorblendr.utils.OverlayManager.unregisterFabricatedOverlay
import com.drdisagree.colorblendr.utils.SystemUtil.getScreenRotation
import com.drdisagree.colorblendr.utils.WallpaperColorUtil.getWallpaperColors
import kotlin.math.abs

class BroadcastListener : BroadcastReceiver() {
    @Suppress("deprecation")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: " + intent.action)

        if (lastOrientation == -1) {
            lastOrientation = getScreenRotation(context)
        }

        val currentOrientation = getScreenRotation(context)

        if (Intent.ACTION_BOOT_COMPLETED == intent.action ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED == intent.action
        ) {
            // Start background service on boot
            if (permissionsGranted(context) && AutoStartService.isServiceNotRunning) {
                context.startForegroundService(Intent(context, AutoStartService::class.java))
            }

            validateRootAndUpdateColors(context, object : MethodInterface() {
                override fun run() {
                    cooldownTime = 10000
                    Handler(Looper.getMainLooper()).postDelayed({
                        cooldownTime = 5000
                    }, 10000)
                    updateAllColors(context)
                }
            })
        }

        // Update wallpaper colors on wallpaper change
        if (Intent.ACTION_WALLPAPER_CHANGED == intent.action &&
            permissionsGranted(context)
        ) {
            val wallpaperColors = getWallpaperColors(context)
            putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors))

            if (!getBoolean(MONET_SEED_COLOR_ENABLED, false)) {
                putInt(MONET_SEED_COLOR, wallpaperColors[0])
            }
        }

        // Update fabricated colors on wallpaper change
        if (Intent.ACTION_WALLPAPER_CHANGED == intent.action ||
            (Intent.ACTION_CONFIGURATION_CHANGED == intent.action &&
                    lastOrientation == currentOrientation)
        ) {
            validateRootAndUpdateColors(context, object : MethodInterface() {
                override fun run() {
                    updateAllColors(context)
                }
            })
        } else if (lastOrientation != currentOrientation) {
            lastOrientation = currentOrientation
        }

        if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {
            // Remove fabricated colors for uninstalled apps
            val data = intent.data

            if (data != null) {
                val packageName = data.schemeSpecificPart
                val selectedApps: HashMap<String, Boolean> = selectedFabricatedApps

                if (selectedApps.containsKey(packageName) && java.lang.Boolean.TRUE == selectedApps[packageName]) {
                    selectedApps.remove(packageName)
                    saveSelectedFabricatedApps(selectedApps)

                    validateRootAndUpdateColors(context, object : MethodInterface() {
                        override fun run() {
                            unregisterFabricatedOverlay(
                                kotlin.String.format(
                                    FABRICATED_OVERLAY_NAME_APPS,
                                    packageName
                                )
                            )
                        }
                    })
                }
            }
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            // Update fabricated colors for updated app
            validateRootAndUpdateColors(context, object : MethodInterface() {
                override fun run() {
                    updateAllColors(context)
                }
            })
        } else if (Intent.ACTION_PACKAGE_REPLACED == intent.action) {
            // Update fabricated colors for updated app
            val data = intent.data

            if (data != null) {
                val packageName = data.schemeSpecificPart
                val selectedApps = selectedFabricatedApps

                if (selectedApps.containsKey(packageName) && java.lang.Boolean.TRUE == selectedApps[packageName]) {
                    validateRootAndUpdateColors(context, object : MethodInterface() {
                        override fun run() {
                            applyFabricatedColorsPerApp(context, packageName, null)
                        }
                    })
                }
            }
        }

        if (Intent.ACTION_PACKAGE_ADDED == intent.action ||
            Intent.ACTION_PACKAGE_REMOVED == intent.action ||
            Intent.ACTION_WALLPAPER_CHANGED == intent.action
        ) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    companion object {
        private val TAG: String = BroadcastListener::class.java.simpleName
        var lastOrientation: Int = -1
        private var cooldownTime: Long = 5000

        private fun validateRootAndUpdateColors(context: Context, method: MethodInterface) {
            if (workingMethod == Const.WorkMethod.ROOT &&
                RootConnectionProvider.isNotConnected
            ) {
                RootConnectionProvider.builder(context)
                    .runOnSuccess(method)
                    .run()
            } else {
                method.run()
            }
        }

        private fun updateAllColors(context: Context) {
            if (!getBoolean(THEMING_ENABLED, true) && !getBoolean(SHIZUKU_THEMING_ENABLED, true)) {
                return
            }

            if (abs(
                    (getLong(
                        MONET_LAST_UPDATED,
                        0
                    ) - System.currentTimeMillis()).toDouble()
                ) >= cooldownTime
            ) {
                putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
                Handler(Looper.getMainLooper()).postDelayed({
                    applyFabricatedColors(
                        context
                    )
                }, 500)
            }
        }
    }
}

package com.drdisagree.colorblendr.service;

import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.utils.AppUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.WallpaperUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class BroadcastListener extends BroadcastReceiver {

    private static final String TAG = BroadcastListener.class.getSimpleName();

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())
        ) {
            // Start background service on boot
            if (AppUtil.permissionsGranted(context)) {
                if (!Const.isBackgroundServiceRunning) {
                    context.startService(new Intent(ColorBlendr.getAppContext(), BackgroundService.class));
                }
            }

            // Start root service on boot
            if (!RootServiceProvider.isRootServiceBound()) {
                RootServiceProvider rootServiceProvider = new RootServiceProvider(context);
                rootServiceProvider.runOnSuccess(new MethodInterface() {
                    @Override
                    public void run() {
                        updateAllColors(context);
                    }
                });
                rootServiceProvider.startRootService();
            }
        }

        // Update wallpaper colors on wallpaper change
        if (Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction()) &&
                AppUtil.permissionsGranted(context)
        ) {
            ArrayList<Integer> wallpaperColors = WallpaperUtil.getWallpaperColors(context);
            RPrefs.putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors));

            if (!RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false)) {
                RPrefs.putInt(MONET_SEED_COLOR, wallpaperColors.get(0));
            }
        }

        // Update fabricated colors on wallpaper change
        if (Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction()) ||
                Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())
        ) {
            updateAllColors(context);
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            // Remove fabricated colors for uninstalled apps
            Uri data = intent.getData();

            if (data != null) {
                String packageName = data.getSchemeSpecificPart();
                HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

                if (selectedApps.containsKey(packageName) && Boolean.TRUE.equals(selectedApps.get(packageName))) {
                    OverlayManager.unregisterFabricatedOverlay(packageName);
                }
            }
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            // Update fabricated colors for updated app
            updateAllColors(context);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            // Update fabricated colors for updated app
            Uri data = intent.getData();

            if (data != null) {
                String packageName = data.getSchemeSpecificPart();
                HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

                if (selectedApps.containsKey(packageName) && Boolean.TRUE.equals(selectedApps.get(packageName))) {
                    OverlayManager.applyFabricatedColorsPerApp(context, packageName, null);
                }
            }
        }
    }

    private static void updateAllColors(Context context) {
        if (Math.abs(RPrefs.getLong(MONET_LAST_UPDATED, 0) - System.currentTimeMillis()) >= 5000) {
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(context), 3000);
        }
    }
}

package com.drdisagree.colorblendr.service;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.SHIZUKU_THEMING_ENABLED;
import static com.drdisagree.colorblendr.common.Const.THEMING_ENABLED;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootConnectionProvider;
import com.drdisagree.colorblendr.utils.AppUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.SystemUtil;
import com.drdisagree.colorblendr.utils.WallpaperUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class BroadcastListener extends BroadcastReceiver {

    private static final String TAG = BroadcastListener.class.getSimpleName();
    public static int lastOrientation = -1;
    private static long cooldownTime = 5000;

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent.getAction());

        if (lastOrientation == -1) {
            lastOrientation = SystemUtil.getScreenRotation(context);
        }

        int currentOrientation = SystemUtil.getScreenRotation(context);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())
        ) {
            // Start background service on boot
            if (AppUtil.permissionsGranted(context)) {
                if (BackgroundService.isServiceNotRunning()) {
                    context.startService(new Intent(ColorBlendr.getAppContext(), BackgroundService.class));
                }
            }

            validateRootAndUpdateColors(context, new MethodInterface() {
                @Override
                public void run() {
                    cooldownTime = 10000;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> cooldownTime = 5000, 10000);
                    updateAllColors(context);
                }
            });
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
                (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction()) &&
                        lastOrientation == currentOrientation)
        ) {
            validateRootAndUpdateColors(context, new MethodInterface() {
                @Override
                public void run() {
                    updateAllColors(context);
                }
            });
        } else if (lastOrientation != currentOrientation) {
            lastOrientation = currentOrientation;
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            // Remove fabricated colors for uninstalled apps
            Uri data = intent.getData();

            if (data != null) {
                String packageName = data.getSchemeSpecificPart();
                HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

                if (selectedApps.containsKey(packageName) && Boolean.TRUE.equals(selectedApps.get(packageName))) {
                    selectedApps.remove(packageName);
                    Const.saveSelectedFabricatedApps(selectedApps);

                    validateRootAndUpdateColors(context, new MethodInterface() {
                        @Override
                        public void run() {
                            OverlayManager.unregisterFabricatedOverlay(String.format(FABRICATED_OVERLAY_NAME_APPS, packageName));
                        }
                    });
                }
            }
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            // Update fabricated colors for updated app
            validateRootAndUpdateColors(context, new MethodInterface() {
                @Override
                public void run() {
                    updateAllColors(context);
                }
            });
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            // Update fabricated colors for updated app
            Uri data = intent.getData();

            if (data != null) {
                String packageName = data.getSchemeSpecificPart();
                HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

                if (selectedApps.containsKey(packageName) && Boolean.TRUE.equals(selectedApps.get(packageName))) {
                    validateRootAndUpdateColors(context, new MethodInterface() {
                        @Override
                        public void run() {
                            OverlayManager.applyFabricatedColorsPerApp(context, packageName, null);
                        }
                    });
                }
            }
        }

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) ||
                Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) ||
                Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction())) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private static void validateRootAndUpdateColors(Context context, MethodInterface method) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT &&
                RootConnectionProvider.isNotConnected()
        ) {
            RootConnectionProvider.builder(context)
                    .runOnSuccess(method)
                    .run();
        } else {
            method.run();
        }
    }

    private static void updateAllColors(Context context) {
        if (!RPrefs.getBoolean(THEMING_ENABLED, true) && !RPrefs.getBoolean(SHIZUKU_THEMING_ENABLED, true)) {
            return;
        }

        if (Math.abs(RPrefs.getLong(MONET_LAST_UPDATED, 0) - System.currentTimeMillis()) >= cooldownTime) {
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(context), 500);
        }
    }
}

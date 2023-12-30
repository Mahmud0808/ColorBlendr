package com.drdisagree.colorblendr.service;

import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

public class BroadcastListener extends BroadcastReceiver {

    private static final String TAG = BroadcastListener.class.getSimpleName();

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())
        ) {
            if (AppUtil.permissionsGranted(context)) {
                if (!Const.isBackgroundServiceRunning) {
                    context.startService(new Intent(ColorBlendr.getAppContext(), BackgroundService.class));
                }
            }

            if (!RootServiceProvider.isRootServiceBound()) {
                if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT) {
                    RootServiceProvider rootServiceProvider = new RootServiceProvider(context);
                    rootServiceProvider.runOnSuccess(new MethodInterface() {
                        @Override
                        public void run() {
                            if (Math.abs(RPrefs.getLong(MONET_LAST_UPDATED, 0) - System.currentTimeMillis()) >= 5000) {
                                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(context), 3000);
                            }
                        }
                    });
                    rootServiceProvider.startRootService();
                }
            }
        }

        if (Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction()) &&
                AppUtil.permissionsGranted(context)
        ) {
            ArrayList<Integer> wallpaperColors = WallpaperUtil.getWallpaperColors(context);
            RPrefs.putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors));

            if (!RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false)) {
                RPrefs.putInt(MONET_SEED_COLOR, wallpaperColors.get(0));
            }
        }

        if (Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction()) ||
                Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())
        ) {
            if (Math.abs(RPrefs.getLong(MONET_LAST_UPDATED, 0) - System.currentTimeMillis()) >= 5000) {
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(context), 3000);
            }
        }
    }
}

package com.drdisagree.colorblendr.service;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.AppUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;

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
                    context.startForegroundService(new Intent(ColorBlendr.getAppContext(), BackgroundService.class));
                }
            }
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction()) ||
                Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())
        ) {
            if (Math.abs(RPrefs.getLong(MONET_LAST_UPDATED, 0) - System.currentTimeMillis()) >= 5000) {
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> applyFabricatedColors(context), 3000);
            }
        }
    }

    private void applyFabricatedColors(Context context) {
        OverlayManager.applyFabricatedColors(
                ColorUtil.generateModifiedColors(
                        context,
                        RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                        RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                        RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                        RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                        RPrefs.getBoolean(MONET_ACCURATE_SHADES, true)
                )
        );
    }
}

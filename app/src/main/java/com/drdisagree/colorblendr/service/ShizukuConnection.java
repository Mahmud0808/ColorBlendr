package com.drdisagree.colorblendr.service;

import static com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Keep;

import com.drdisagree.colorblendr.extension.ThemeOverlayPackage;
import com.drdisagree.colorblendr.utils.RomUtil;
import com.topjohnwu.superuser.Shell;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rikka.shizuku.SystemServiceHelper;

public class ShizukuConnection extends IShizukuConnection.Stub {

    private static final String TAG = ShizukuConnection.class.getSimpleName();
    private static IOverlayManager mOMS;

    static {
        if (mOMS == null) {
            mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
        }
    }

    public ShizukuConnection() {
        Log.i(TAG, "Constructed with no arguments");
    }

    @Keep
    public ShizukuConnection(Context context) {
        Log.i(TAG, "Constructed with context: " + context.toString());
    }

    private static IOverlayManager getOMS() {
        if (mOMS == null) {
            mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
        }
        return mOMS;
    }

    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public void exit() {
        destroy();
    }

    @Override
    public void applyFabricatedColors(String jsonString) {
        final String mCommand = "settings put secure " + THEME_CUSTOMIZATION_OVERLAY_PACKAGES + " '" + jsonString + "'";
        Shell.cmd(mCommand).exec();
    }

    @Override
    public void removeFabricatedColors() {
        try {
            String currentSettings = getCurrentSettings();
            JSONObject jsonObject = new JSONObject(currentSettings);

            String[] keysToRemove = new String[]{
                    ThemeOverlayPackage.THEME_STYLE,
                    ThemeOverlayPackage.COLOR_SOURCE,
                    ThemeOverlayPackage.SYSTEM_PALETTE
            };

            for (String key : keysToRemove) {
                jsonObject.remove(key);
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                jsonObject.remove(ThemeOverlayPackage.ACCENT_COLOR);
            }

            jsonObject.putOpt(ThemeOverlayPackage.COLOR_BOTH, "0");
            jsonObject.putOpt(ThemeOverlayPackage.COLOR_SOURCE, "home_wallpaper");
            jsonObject.putOpt(ThemeOverlayPackage.APPLIED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

            applyFabricatedColors(jsonObject.toString());
        } catch (Exception e) {
            Log.e(TAG, "removeFabricatedColors: ", e);
        }
    }

    @Override
    public String getCurrentSettings() {
        final String mCommand = "settings get secure " + THEME_CUSTOMIZATION_OVERLAY_PACKAGES;
        return Shell.cmd(mCommand).exec().getOut().get(0);
    }

    @Override
    public void applySamsungColors(int[] colors) throws RemoteException {
        if (!RomUtil.isSamsung()) {
            Log.w(TAG, "applySamsungColors: Not a Samsung device. Skipping...");
            return;
        }

        List<Integer> wallpaperColors = new ArrayList<>();

        try {
            if (colors != null) {
                for (int color : colors) {
                    wallpaperColors.add(color);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply wallpaper colors", e);
        }

        if (!wallpaperColors.isEmpty()) {
            getOMS().applyWallpaperColors(wallpaperColors, 5, 13);
        }
    }
}

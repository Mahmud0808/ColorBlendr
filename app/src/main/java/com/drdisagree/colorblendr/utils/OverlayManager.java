package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_SYSTEM;
import static com.drdisagree.colorblendr.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;
import static com.drdisagree.colorblendr.common.Const.SHIZUKU_THEMING_ENABLED;
import static com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES;
import static com.drdisagree.colorblendr.common.Const.THEMING_ENABLED;
import static com.drdisagree.colorblendr.common.Const.TINT_TEXT_COLOR;
import static com.drdisagree.colorblendr.utils.ShizukuUtil.mShizukuShell;

import android.content.Context;
import android.graphics.Color;
import android.os.RemoteException;
import android.util.Log;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage;
import com.drdisagree.colorblendr.service.IRootConnection;
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class OverlayManager {

    private static final String TAG = OverlayManager.class.getSimpleName();
    private static final IRootConnection mServiceConnection = ColorBlendr.getServiceConnection();
    private static final String[][] colorNames = ColorUtil.getColorNames();

    public static void enableOverlay(String packageName) {
        if (mServiceConnection == null) return;

        try {
            mServiceConnection.enableOverlay(Collections.singletonList(packageName));
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to enable overlay: " + packageName, e);
        }
    }

    public static void disableOverlay(String packageName) {
        if (mServiceConnection == null) return;

        try {
            mServiceConnection.disableOverlay(Collections.singletonList(packageName));
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disable overlay: " + packageName, e);
        }
    }

    public static boolean isOverlayInstalled(String packageName) {
        if (mServiceConnection == null) return false;

        try {
            return mServiceConnection.isOverlayInstalled(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check if overlay is installed: " + packageName, e);
            return false;
        }
    }

    public static boolean isOverlayEnabled(String packageName) {
        if (mServiceConnection == null) return false;

        try {
            return mServiceConnection.isOverlayEnabled(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check if overlay is enabled: " + packageName, e);
            return false;
        }
    }

    public static void uninstallOverlayUpdates(String packageName) {
        if (mServiceConnection == null) return;

        try {
            mServiceConnection.uninstallOverlayUpdates(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to uninstall overlay updates: " + packageName, e);
        }
    }

    public static void registerFabricatedOverlay(FabricatedOverlayResource fabricatedOverlay) {
        if (mServiceConnection == null) return;

        try {
            mServiceConnection.registerFabricatedOverlay(fabricatedOverlay);
            mServiceConnection.enableOverlayWithIdentifier(Collections.singletonList(fabricatedOverlay.overlayName));
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register fabricated overlay: " + fabricatedOverlay.overlayName, e);
        }
    }

    public static void unregisterFabricatedOverlay(String packageName) {
        if (mServiceConnection == null) return;

        try {
            mServiceConnection.unregisterFabricatedOverlay(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to unregister fabricated overlay: " + packageName, e);
        }
    }

    public static void applyFabricatedColors(Context context) {
        if (!RPrefs.getBoolean(THEMING_ENABLED, true) || !RPrefs.getBoolean(SHIZUKU_THEMING_ENABLED, true)) {
            return;
        }

        if (applyFabricatedColorsNonRoot(context)) {
            return;
        }

        ColorSchemeUtil.MONET style = ColorSchemeUtil.stringToEnumMonetStyle(
                context,
                RPrefs.getString(MONET_STYLE, context.getString(R.string.monet_tonalspot))
        );
        int monetAccentSaturation = RPrefs.getInt(MONET_ACCENT_SATURATION, 100);
        int monetBackgroundSaturation = RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100);
        int monetBackgroundLightness = RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100);
        boolean pitchBlackTheme = RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false);
        boolean accurateShades = RPrefs.getBoolean(MONET_ACCURATE_SHADES, true);

        ArrayList<ArrayList<Integer>> paletteLight = ColorUtil.generateModifiedColors(
                context,
                style,
                monetAccentSaturation,
                monetBackgroundSaturation,
                monetBackgroundLightness,
                pitchBlackTheme,
                accurateShades,
                false,
                false
        );

        ArrayList<ArrayList<Integer>> paletteDark = ColorUtil.generateModifiedColors(
                context,
                style,
                monetAccentSaturation,
                monetBackgroundSaturation,
                monetBackgroundLightness,
                pitchBlackTheme,
                accurateShades,
                false,
                true
        );

        ArrayList<FabricatedOverlayResource> fabricatedOverlays = new ArrayList<>();
        fabricatedOverlays.add(new FabricatedOverlayResource(
                FABRICATED_OVERLAY_NAME_SYSTEM,
                FRAMEWORK_PACKAGE
        ));

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 13; j++) {
                fabricatedOverlays.get(0).setColor(colorNames[i][j], paletteDark.get(i).get(j));
            }
        }

        FabricatedUtil.createDynamicOverlay(
                fabricatedOverlays.get(0),
                paletteLight,
                paletteDark
        );

        HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

        for (String packageName : selectedApps.keySet()) {
            if (Boolean.TRUE.equals(selectedApps.get(packageName)) &&
                    SystemUtil.isAppInstalled(packageName)) {
                FabricatedOverlayResource fabricatedOverlayPerApp = getFabricatedColorsPerApp(
                        context,
                        packageName,
                        SystemUtil.isDarkMode() ?
                                paletteDark :
                                paletteLight
                );

                fabricatedOverlays.add(fabricatedOverlayPerApp);
            }
        }

        if (pitchBlackTheme) {
            fabricatedOverlays.get(0).setColor("surface_header_dark_sysui", Color.BLACK);
            fabricatedOverlays.get(0).setColor(colorNames[3][11], Color.BLACK);
            fabricatedOverlays.get(0).setColor(colorNames[4][11], Color.BLACK);
        }

        if (!RPrefs.getBoolean(TINT_TEXT_COLOR, true)) {
            fabricatedOverlays.get(0).setColor("text_color_primary_device_default_dark", Color.WHITE);
            fabricatedOverlays.get(0).setColor("text_color_secondary_device_default_dark", 0xB3FFFFFF);
            fabricatedOverlays.get(0).setColor("text_color_primary_device_default_light", Color.BLACK);
            fabricatedOverlays.get(0).setColor("text_color_secondary_device_default_light", 0xB3000000);
        }

        for (FabricatedOverlayResource fabricatedOverlay : fabricatedOverlays) {
            registerFabricatedOverlay(fabricatedOverlay);
        }
    }

    public static void applyFabricatedColorsPerApp(Context context, String packageName, ArrayList<ArrayList<Integer>> palette) {
        registerFabricatedOverlay(
                getFabricatedColorsPerApp(
                        context,
                        packageName,
                        palette
                )
        );
    }

    public static void removeFabricatedColors(Context context) {
        if (removeFabricatedColorsNonRoot(context)) {
            return;
        }

        ArrayList<String> fabricatedOverlays = new ArrayList<>();
        HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

        for (String packageName : selectedApps.keySet()) {
            if (Boolean.TRUE.equals(selectedApps.get(packageName))) {
                fabricatedOverlays.add(String.format(FABRICATED_OVERLAY_NAME_APPS, packageName));
            }
        }

        fabricatedOverlays.add(FABRICATED_OVERLAY_NAME_SYSTEM);

        for (String packageName : fabricatedOverlays) {
            unregisterFabricatedOverlay(packageName);
        }
    }

    private static FabricatedOverlayResource getFabricatedColorsPerApp(Context context, String packageName, ArrayList<ArrayList<Integer>> palette) {
        if (palette == null) {
            palette = ColorUtil.generateModifiedColors(
                    context,
                    ColorSchemeUtil.stringToEnumMonetStyle(
                            context,
                            RPrefs.getString(MONET_STYLE, context.getString(R.string.monet_tonalspot))
                    ),
                    RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                    RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                    RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                    RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                    RPrefs.getBoolean(MONET_ACCURATE_SHADES, true),
                    false
            );
        }

        FabricatedOverlayResource fabricatedOverlay = new FabricatedOverlayResource(
                String.format(FABRICATED_OVERLAY_NAME_APPS, packageName),
                packageName
        );

        FabricatedUtil.assignPerAppColorsToOverlay(fabricatedOverlay, palette);

        return fabricatedOverlay;
    }

    public static boolean applyFabricatedColorsNonRoot(Context context) {
        if (Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU) {
            return false;
        }

        if (!ShizukuUtil.isShizukuAvailable() || (ShizukuUtil.isShizukuAvailable() && !ShizukuUtil.hasShizukuPermission(context))) {
            return true;
        }

        ExecutorService mExecutors = Executors.newSingleThreadExecutor();
        mExecutors.execute(() -> {
            List<String> mResult = new ArrayList<>();
            final String jsonString = ThemeOverlayPackage.getThemeCustomizationOverlayPackages().toString();
            final String mCommand = "settings put secure " + THEME_CUSTOMIZATION_OVERLAY_PACKAGES + " '" + jsonString + "'";

            try {
                mShizukuShell = new ShizukuShell(mCommand, mResult);
                mShizukuShell.exec();

                if (mResult.isEmpty()) {
                    RPrefs.putBoolean(SHIZUKU_THEMING_ENABLED, true);
                }
            } catch (Exception e) {
                Log.d(TAG, "Command: " + mCommand);
                Log.d(TAG, "Output: " + mResult);
                Log.d(TAG, "Exception: ", e);
            } finally {
                mShizukuShell.destroy();
            }

            if (!mExecutors.isShutdown()) {
                mExecutors.shutdown();
            }
        });

        return true;
    }

    public static boolean removeFabricatedColorsNonRoot(Context context) {
        if (Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU) {
            return false;
        }

        if (!ShizukuUtil.isShizukuAvailable() || (mShizukuShell != null && mShizukuShell.isBusy())) {
            return true;
        }

        ExecutorService mExecutors = Executors.newSingleThreadExecutor();
        mExecutors.execute(() -> {
            List<String> mResult = new ArrayList<>();
            final String mCommand = "settings delete secure " + THEME_CUSTOMIZATION_OVERLAY_PACKAGES;

            try {
                mShizukuShell = new ShizukuShell(mCommand, mResult);
                mShizukuShell.exec();

                if (mResult.isEmpty()) {
                    RPrefs.putBoolean(SHIZUKU_THEMING_ENABLED, false);
                }
            } catch (Exception e) {
                Log.d(TAG, "Command: " + mCommand);
                Log.d(TAG, "Output: " + mResult);
                Log.d(TAG, "Exception: ", e);
            }

            if (!mExecutors.isShutdown()) {
                mExecutors.shutdown();
            }
        });

        return true;
    }
}

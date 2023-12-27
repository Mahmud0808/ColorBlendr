package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME;
import static com.drdisagree.colorblendr.common.Const.FRAMEWORK_PACKAGE;

import android.os.RemoteException;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.service.IRootService;
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource;

import java.util.ArrayList;
import java.util.Collections;

public class OverlayManager {

    private static final String TAG = OverlayManager.class.getSimpleName();
    private static final IRootService mRootService = ColorBlendr.getRootService();
    private static final String[][] colorNames = ColorUtil.getColorNames();

    public static void enableOverlay(String packageName) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return;

        try {
            mRootService.enableOverlay(Collections.singletonList(packageName));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void disableOverlay(String packageName) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return;

        try {
            mRootService.disableOverlay(Collections.singletonList(packageName));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isOverlayInstalled(String packageName) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return false;

        try {
            return mRootService.isOverlayInstalled(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isOverlayEnabled(String packageName) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return false;

        try {
            return mRootService.isOverlayEnabled(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void uninstallOverlayUpdates(String packageName) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return;

        try {
            mRootService.uninstallOverlayUpdates(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void registerFabricatedOverlay(FabricatedOverlayResource fabricatedOverlay) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return;

        try {
            mRootService.registerFabricatedOverlay(fabricatedOverlay);
            mRootService.enableOverlayWithIdentifier(Collections.singletonList(fabricatedOverlay.overlayName));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void unregisterFabricatedOverlay(String packageName) {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) return;

        try {
            mRootService.unregisterFabricatedOverlay(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void applyFabricatedColors(ArrayList<ArrayList<Integer>> palette) {
        if (palette == null || Const.getWorkingMethod() == Const.WORK_METHOD.XPOSED) {
            return;
        }

        FabricatedOverlayResource fabricatedOverlay = new FabricatedOverlayResource(
                FABRICATED_OVERLAY_NAME,
                FRAMEWORK_PACKAGE
        );

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 13; j++) {
                fabricatedOverlay.setColor(colorNames[i][j], palette.get(i).get(j));
            }
        }

        registerFabricatedOverlay(fabricatedOverlay);
    }
}

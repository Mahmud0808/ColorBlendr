package com.drdisagree.colorblendr.service;

import static com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import com.topjohnwu.superuser.Shell;

public class ShizukuConnection extends IShizukuConnection.Stub {

    private static final String TAG = ShizukuConnection.class.getSimpleName();

    public ShizukuConnection() {
        Log.i(TAG, "Constructed with no arguments");
    }

    @Keep
    public ShizukuConnection(Context context) {
        Log.i(TAG, "Constructed with context: " + context.toString());
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
        final String mCommand = "settings delete secure " + THEME_CUSTOMIZATION_OVERLAY_PACKAGES;
        Shell.cmd(mCommand).exec();
    }
}

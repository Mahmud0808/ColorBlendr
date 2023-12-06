package com.drdisagree.colorblendr.xposed;

import android.content.Context;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class ModPack {

    protected Context mContext;

    public ModPack(Context context) {
        mContext = context;
    }

    public abstract void updatePrefs(String... Key);

    public abstract void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable;
}
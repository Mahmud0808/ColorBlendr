package com.drdisagree.colorblendr.xposed;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class InitHook implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {

    HookRes HookRes = new HookRes();
    HookEntry HookEntry = new HookEntry();

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) {
        HookRes.handleInitPackageResources(initPackageResourcesParam);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        HookEntry.handleLoadPackage(loadPackageParam);
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        HookRes.initZygote(startupParam);
    }
}
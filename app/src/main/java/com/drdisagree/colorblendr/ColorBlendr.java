package com.drdisagree.colorblendr;

import static com.drdisagree.colorblendr.common.Const.TAB_SELECTED_INDEX;

import android.app.Application;
import android.content.Context;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.service.IRootService;
import com.google.android.material.color.DynamicColors;

import java.lang.ref.WeakReference;

public class ColorBlendr extends Application {

    private static ColorBlendr instance;
    private static WeakReference<Context> contextReference;

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
        DynamicColors.applyToActivitiesIfAvailable(this);
        RPrefs.clearPref(TAB_SELECTED_INDEX);
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(ColorBlendr.getInstance().getApplicationContext());
        }
        return contextReference.get();
    }

    private static ColorBlendr getInstance() {
        if (instance == null) {
            instance = new ColorBlendr();
        }
        return instance;
    }

    public static IRootService getRootService() {
        return RootServiceProvider.getRootServiceProvider();
    }
}
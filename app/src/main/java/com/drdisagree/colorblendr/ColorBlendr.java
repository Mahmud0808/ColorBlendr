package com.drdisagree.colorblendr;

import android.app.Application;
import android.content.Context;

import com.drdisagree.colorblendr.provider.RootConnectionProvider;
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider;
import com.drdisagree.colorblendr.service.IRootConnection;
import com.drdisagree.colorblendr.service.IShizukuConnection;
import com.google.android.material.color.DynamicColors;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.ref.WeakReference;

public class ColorBlendr extends Application {

    private static ColorBlendr instance;
    private static WeakReference<Context> contextReference;

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
        DynamicColors.applyToActivitiesIfAvailable(this);
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

    public static IRootConnection getRootConnection() {
        return RootConnectionProvider.getServiceProvider();
    }

    public static IShizukuConnection getShizukuConnection() {
        return ShizukuConnectionProvider.getServiceProvider();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HiddenApiBypass.addHiddenApiExemptions("L");
    }
}
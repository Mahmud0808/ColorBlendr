package com.drdisagree.colorblendr.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceLauncher {

    private static final String TAG = ServiceLauncher.class.getSimpleName();
    private static Intent serviceIntent = null;

    private void setServiceIntent(Context context) {
        if (serviceIntent == null) {
            serviceIntent = new Intent(context, AutoStartService.class);
        }
    }

    public void launchService(Context context) {
        if (context == null) {
            return;
        }

        setServiceIntent(context);

        if (AutoStartService.isServiceNotRunning()) {
            Log.d(TAG, "launchService: Service is starting...");
            context.startForegroundService(serviceIntent);
        }
    }
}

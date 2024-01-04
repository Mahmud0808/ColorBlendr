package com.drdisagree.colorblendr.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class ServiceDestroyedListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(new OneTimeWorkRequest.Builder(RestartServiceWorker.class).build());
    }
}

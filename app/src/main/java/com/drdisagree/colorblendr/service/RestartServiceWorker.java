package com.drdisagree.colorblendr.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RestartServiceWorker extends Worker {

    private final Context context;

    public RestartServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        if (BackgroundService.isServiceNotRunning()) {
            context.startService(new Intent(context, BackgroundService.class));
        }

        return Result.success();
    }
}

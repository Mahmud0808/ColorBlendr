package com.drdisagree.colorblendr.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import androidx.work.Configuration;

public class ScheduledJobService extends JobService {

    private static final String TAG = ScheduledJobService.class.getSimpleName();

    ScheduledJobService() {
        @SuppressWarnings("all") Configuration.Builder builder = new Configuration.Builder();
        builder.setJobSchedulerJobIdRange(0, 1000);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        ServiceLauncher serviceLauncher = new ServiceLauncher();
        serviceLauncher.launchService(this);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG, "Stopping job...");

        Intent broadcastIntent = new Intent(getApplicationContext().getPackageName());
        sendBroadcast(broadcastIntent);

        return false;
    }
}
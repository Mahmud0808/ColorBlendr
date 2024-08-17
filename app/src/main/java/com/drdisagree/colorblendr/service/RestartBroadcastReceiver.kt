package com.drdisagree.colorblendr.service;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = RestartBroadcastReceiver.class.getSimpleName();
    private static JobScheduler jobScheduler;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Service Stopped, but this is a never ending service.");
        scheduleJob(context);
    }

    public static void scheduleJob(Context context) {
        if (jobScheduler == null) {
            jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        }

        ComponentName componentName = new ComponentName(context, ScheduledJobService.class);

        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setOverrideDeadline(0)
                .setPersisted(true).build();

        jobScheduler.schedule(jobInfo);
    }
}

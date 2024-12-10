package com.drdisagree.colorblendr.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class RestartBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Service Stopped, but this is a never ending service.")
        scheduleJob(context)
    }

    companion object {
        private val TAG: String = RestartBroadcastReceiver::class.java.simpleName
        private var jobScheduler: JobScheduler? = null

        fun scheduleJob(context: Context) {
            if (jobScheduler == null) {
                jobScheduler =
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            }

            val componentName = ComponentName(context, ScheduledJobService::class.java)

            val jobInfo = JobInfo.Builder(1, componentName)
                .setOverrideDeadline(0)
                .setPersisted(true).build()

            jobScheduler!!.schedule(jobInfo)
        }
    }
}

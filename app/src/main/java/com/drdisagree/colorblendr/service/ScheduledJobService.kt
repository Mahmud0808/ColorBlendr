package com.drdisagree.colorblendr.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import androidx.work.Configuration

class ScheduledJobService internal constructor() : JobService() {

    init {
        val builder: Configuration.Builder = Configuration.Builder()
        builder.setJobSchedulerJobIdRange(0, 1000)
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        val serviceLauncher = ServiceLauncher()
        serviceLauncher.launchService(this)

        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        Log.i(TAG, "Stopping job...")

        val broadcastIntent = Intent(applicationContext.packageName)
        sendBroadcast(broadcastIntent)

        return false
    }

    companion object {
        private val TAG: String = ScheduledJobService::class.java.simpleName
    }
}
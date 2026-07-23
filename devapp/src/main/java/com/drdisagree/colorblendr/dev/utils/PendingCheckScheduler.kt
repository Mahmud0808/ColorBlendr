package com.drdisagree.colorblendr.dev.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.drdisagree.colorblendr.dev.data.config.DevPrefs
import com.drdisagree.colorblendr.dev.data.worker.PendingCheckWorker
import java.util.concurrent.TimeUnit

object PendingCheckScheduler {

    private const val WORK_NAME = "pending_check"

    fun sync(context: Context) {
        if (DevPrefs.notifyEnabled(context) && DevPrefs.adminKey(context).isNotEmpty()) {
            schedule(context, DevPrefs.notifyIntervalHours(context))
        } else {
            cancel(context)
        }
    }

    fun schedule(context: Context, intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<PendingCheckWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(intervalHours.toLong(), TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
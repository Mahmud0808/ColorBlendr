package com.drdisagree.colorblendr.dev.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.drdisagree.colorblendr.dev.data.api.AdminApi
import com.drdisagree.colorblendr.dev.data.api.ApiResult
import com.drdisagree.colorblendr.dev.data.config.DevPrefs
import com.drdisagree.colorblendr.dev.utils.PendingNotifier

class PendingCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val key = DevPrefs.adminKey(applicationContext)
        if (key.isEmpty() || !DevPrefs.notifyEnabled(applicationContext)) {
            return Result.success()
        }

        return when (val result = AdminApi.fetchPending(key)) {
            is ApiResult.Success -> {
                DevPrefs.setLastCheck(applicationContext, System.currentTimeMillis())
                PendingNotifier.notifyPending(applicationContext, result.data.size)
                Result.success()
            }

            is ApiResult.Failure -> Result.retry()
        }
    }
}
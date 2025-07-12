package com.drdisagree.colorblendr.utils.wifiadb

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.drdisagree.colorblendr.service.AdbPairingNotification

class AdbPairingNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(
    context,
    params
) {

    override fun doWork(): Result {
        val context = applicationContext
        val pairingCode = inputData.getString("pairingCode")

        context.startForegroundService(
            Intent(context, AdbPairingNotification::class.java).apply {
                putExtra("pairingCode", pairingCode)
            }
        )

        return Result.success()
    }
}
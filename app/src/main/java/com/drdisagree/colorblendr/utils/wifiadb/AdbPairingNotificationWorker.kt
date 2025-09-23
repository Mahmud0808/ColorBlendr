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
        val pairingCode = inputData.getString(PAIRING_CODE_KEY)
        val message = inputData.getString("message")

        context.startForegroundService(
            Intent(context, AdbPairingNotification::class.java).apply {
                putExtra(PAIRING_CODE_KEY, pairingCode)
                message?.let { putExtra("message", it) }
            }
        )

        return Result.success()
    }

    companion object {
        const val PAIRING_CODE_KEY = "pairingCode"
    }
}
package com.drdisagree.colorblendr.utils.wifiadb

import android.app.ForegroundServiceStartNotAllowedException
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

        return try {
            context.startForegroundService(
                Intent(context, AdbPairingNotification::class.java).apply {
                    putExtra(PAIRING_CODE_KEY, pairingCode)
                    message?.let { putExtra("message", it) }
                }
            )
            Result.success()
        } catch (_: ForegroundServiceStartNotAllowedException) {
            Result.failure()
        }
    }

    companion object {
        const val PAIRING_CODE_KEY = "pairingCode"
    }
}
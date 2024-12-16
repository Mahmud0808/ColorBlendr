package com.drdisagree.colorblendr.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.ColorBlendr.Companion.shizukuConnection
import com.drdisagree.colorblendr.common.Const.isRootMode
import com.drdisagree.colorblendr.common.Const.isShizukuMode

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("RebootReceiver", "onReceive: Rebooting...")

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

        notificationManager.cancel(REBOOT_REMINDER_NOTIFICATION_ID)

        try {
            if (isRootMode) {
                rootConnection!!.rebootDevice()
            } else if (isShizukuMode) {
                shizukuConnection!!.rebootDevice()
            }
        } catch (e: Exception) {
            Log.e("RebootReceiver", "onReceive: Failed to reboot device", e)
        }
    }

    companion object {
        const val REBOOT_REMINDER_NOTIFICATION_ID = 2
    }
}
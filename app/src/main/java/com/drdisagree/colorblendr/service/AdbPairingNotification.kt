package com.drdisagree.colorblendr.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.ADB_CONNECTING_PORT
import com.drdisagree.colorblendr.data.common.Constant.ADB_IP
import com.drdisagree.colorblendr.data.common.Constant.ADB_PAIRING_CODE
import com.drdisagree.colorblendr.data.common.Constant.ADB_PAIRING_PORT
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbConnect
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbPair

class AdbPairingNotification : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        requestPairingCodeInput()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        requestPairingCodeInput()

        if (intent != null) {
            if (ACTION_STOP_SERVICE == intent.action) {
                stopNotification()
                stopSelf()
                return START_NOT_STICKY
            }

            if (!this.isNotificationActive) {
                requestPairingCodeInput()
            }

            handleUserInput(intent)
        }

        return START_STICKY
    }

    private fun handleUserInput(intent: Intent) {
        val remoteInput: Bundle = RemoteInput.getResultsFromIntent(intent) ?: return

        if (ACTION_SUBMIT_CODE == intent.action) {
            val code = remoteInput.getString(PAIRING_CODE, "")

            if (!code.isNullOrEmpty()) {
                storeAdbCredentials(code)
            } else {
                requestPairingCodeInput()
            }
        }
    }

    private fun requestPairingCodeInput() {
        sendNotification(
            getString(R.string.enter_pairing_code),
            getString(R.string.enter_pairing_code)
        )
    }

    private fun storeAdbCredentials(code: String) {
        Prefs.putString(ADB_PAIRING_CODE, code)

        val ip = Prefs.getString(ADB_IP, null)
            ?: throw IllegalStateException("ADB IP not set")
        val port = Prefs.getString(ADB_PAIRING_PORT, null)
            ?: throw IllegalStateException("ADB Port not set")

        WifiAdbPair.pair(
            this,
            ip,
            port,
            code,
            object : WifiAdbPair.PairingCallback {
                override fun onSuccess() {
                    connectAdb()
                }

                @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
                override fun onFailure(errorMessage: String?) {
                    showFinalNotification(
                        getString(R.string.adb_connection_failed_title),
                        if (errorMessage.isNullOrEmpty()) getString(R.string.adb_connection_failed_message) else errorMessage
                    )
                }
            }
        )

        stopNotification()
        stopSelf()
    }

    private fun connectAdb() {
        val ip = Prefs.getString(ADB_IP, null)
            ?: throw IllegalStateException("ADB IP not set")
        val port = Prefs.getString(ADB_CONNECTING_PORT, null)
            ?: throw IllegalStateException("ADB Port not set")

        WifiAdbConnect.connect(
            ip,
            port,
            object : WifiAdbConnect.ConnectingCallback {
                @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
                override fun onSuccess() {
                    showFinalNotification(
                        getString(R.string.adb_connection_success_title),
                        getString(R.string.adb_connection_success_message)
                    )
                }

                @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
                override fun onFailure(errorMessage: String?) {
                    showFinalNotification(
                        getString(R.string.adb_connection_failed_title),
                        if (errorMessage.isNullOrEmpty()) getString(R.string.adb_connection_failed_message) else errorMessage
                    )
                }
            }
        )
    }

    private fun sendNotification(title: String, label: String) {
        cancelNotification()

        val remoteInput = RemoteInput.Builder(PAIRING_CODE)
            .setLabel(label)
            .build()

        val submitIntent = PendingIntent.getService(
            this,
            0,
            Intent(
                this,
                AdbPairingNotification::class.java
            ).setAction(ACTION_SUBMIT_CODE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val stopServiceIntent = PendingIntent.getService(
            this,
            0,
            Intent(
                this,
                AdbPairingNotification::class.java
            ).setAction(ACTION_STOP_SERVICE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val inputAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_adb,
            getString(R.string.submit),
            submitIntent
        ).addRemoteInput(remoteInput)
            .build()

        val stopAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_stop,
            getString(R.string.dismiss),
            stopServiceIntent
        ).build()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_notification)
            .setContentTitle(title)
            .setContentText(getString(R.string.enter_six_digit_pairing_code))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .addAction(inputAction)
            .addAction(stopAction)
            .setOngoing(true)
            .build()

        startForeground(PAIRING_NOTIFICATION_ID, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showFinalNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(false)
            .build()

        // Update the foreground notification
        NotificationManagerCompat.from(this).apply {
            notify(PAIRING_NOTIFICATION_ID, notification)
        }

        // Stop foreground mode, but keep the notification
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun stopNotification() {
        NotificationManagerCompat.from(this).apply {
            cancel(PAIRING_NOTIFICATION_ID)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun cancelNotification() {
        NotificationManagerCompat.from(this).apply {
            cancel(PAIRING_NOTIFICATION_ID)
        }
    }

    private val isNotificationActive: Boolean
        get() {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            for (notification in manager.getActiveNotifications()) {
                if (notification.id == PAIRING_NOTIFICATION_ID) {
                    return true // Notification is still active
                }
            }
            return false // Notification was dismissed/swiped away
        }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ADB Pairing Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for ADB Pairing notifications"
            enableVibration(true)
            enableLights(true)
            setSound(null, null)
            setShowBadge(false)
            setAllowBubbles(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val CHANNEL_ID = "adb_pairing_channel"
        private const val PAIRING_NOTIFICATION_ID = 101
        private const val PAIRING_CODE = "pairing_code"
        private const val ACTION_SUBMIT_CODE = "action_submit_code"
        private const val ACTION_STOP_SERVICE = "action_stop_service"
    }
}
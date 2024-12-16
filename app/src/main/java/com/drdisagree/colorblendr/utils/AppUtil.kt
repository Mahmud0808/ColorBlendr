package com.drdisagree.colorblendr.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.service.RebootReceiver
import com.drdisagree.colorblendr.service.RebootReceiver.Companion.REBOOT_REMINDER_NOTIFICATION_ID
import com.drdisagree.colorblendr.ui.activities.SplashActivity
import com.drdisagree.colorblendr.utils.ColorUtil.getAccentColor
import java.io.BufferedReader
import java.io.InputStreamReader

object AppUtil {

    val REQUIRED_PERMISSIONS: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES
        ) else arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    fun permissionsGranted(context: Context): Boolean {
        if (!hasStoragePermission()) {
            return false
        }

        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    fun hasStoragePermission(): Boolean {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy()
    }

    fun requestStoragePermission(context: Context) {
        val intent = Intent()
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null))
        (context as Activity).startActivityForResult(intent, 0)

        ActivityCompat.requestPermissions(
            context, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ), 0
        )
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.setData(uri)
        context.startActivity(intent)
    }

    fun readJsonFileFromAssets(fileName: String): String {
        val stringBuilder = StringBuilder()
        val inputStream = appContext.assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()
        return stringBuilder.toString()
    }

    fun showRebootReminderNotification() {
        val notificationManager = appContext.getSystemService(NotificationManager::class.java)
            ?: return
        val notificationChannelId = "Reboot Reminder"

        notificationManager.createNotificationChannel(
            NotificationChannel(
                notificationChannelId,
                appContext.getString(R.string.reboot_reminder_notification_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    appContext.getString(R.string.reboot_reminder_notification_channel_text)
            }
        )

        val rebootIntent = Intent(appContext, RebootReceiver::class.java)
        val rebootPendingIntent = PendingIntent.getBroadcast(
            appContext,
            100,
            rebootIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainActivityIntent = Intent(appContext, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            500,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(appContext, notificationChannelId)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_power)
            .setContentTitle(appContext.getString(R.string.reboot_reminder_notification_title))
            .setContentText(appContext.getString(R.string.reboot_reminder_notification_text))
            .setContentIntent(pendingIntent)
            .setColor(getAccentColor(appContext))
            .setSound(defaultSoundUri, AudioManager.STREAM_NOTIFICATION)
            .addAction(
                R.drawable.ic_power,
                appContext.getString(R.string.reboot_reminder_button),
                rebootPendingIntent
            )
            .build()

        notificationManager.notify(REBOOT_REMINDER_NOTIFICATION_ID, notification)
    }
}

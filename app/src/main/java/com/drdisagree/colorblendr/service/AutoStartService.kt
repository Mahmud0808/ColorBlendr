package com.drdisagree.colorblendr.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.utils.annotations.Test
import com.drdisagree.colorblendr.utils.colors.ColorUtil.getAccentColor
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.hasShizukuPermission
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.isShizukuAvailable
import java.util.Timer
import java.util.TimerTask

class AutoStartService : Service() {

    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        isRunning = true
        registerSystemServices()
        createNotificationChannel()
        showNotification()
        registerReceivers()

        if (BroadcastListener.isLastConfigInitialized.not()) {
            BroadcastListener.lastConfig = Configuration(resources.configuration)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        setupSystemUIRestartListener()

        if (isTestingService) {
            // Testing purposes only
            startTimer(this)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        isRunning = false
        Log.i(TAG, "onDestroy: Service is destroyed :(")

        try {
            unregisterReceiver(myReceiver)
        } catch (_: Exception) {
            // Receiver was probably never registered
        }

        val broadcastIntent = Intent(
            this,
            RestartBroadcastReceiver::class.java
        )
        sendBroadcast(broadcastIntent)

        if (isTestingService) {
            // Testing purposes only
            stopTimer()
        }
    }

    private fun registerSystemServices() {
        if (notificationManager == null) {
            notificationManager = getSystemService(NotificationManager::class.java)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.background_service_notification_channel_title),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.background_service_notification_channel_text)
        }
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun showNotification() {
        val notificationIntent = Intent().apply {
            setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        )
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_notification)
            .setContentTitle(getString(R.string.background_service_notification_title))
            .setContentText(getString(R.string.background_service_notification_text))
            .setContentIntent(pendingIntent)
            .setSound(null, AudioManager.STREAM_NOTIFICATION)
            .setColor(getAccentColor(this))
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }
    }

    private fun registerReceivers() {
        val intentFilterWithoutScheme = IntentFilter().apply {
            @Suppress("DEPRECATION")
            addAction(Intent.ACTION_WALLPAPER_CHANGED)
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
        }

        val intentFilterWithScheme = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        registerReceiver(myReceiver, intentFilterWithoutScheme)
        registerReceiver(myReceiver, intentFilterWithScheme)
    }

    private fun setupSystemUIRestartListener() {
        if (isRootMode() && RootConnectionProvider.isNotConnected) {
            RootConnectionProvider.builder(appContext)
                .onSuccess { initSystemUIRestartListener() }
                .run()
        } else if (isShizukuMode() &&
            ShizukuConnectionProvider.isNotConnected &&
            isShizukuAvailable &&
            hasShizukuPermission()
        ) {
            bindUserService(
                getUserServiceArgs(ShizukuConnection::class.java),
                ShizukuConnectionProvider.serviceConnection
            )
        } else if (isRootMode()) {
            initSystemUIRestartListener()
        }
    }

    private fun initSystemUIRestartListener() {
        try {
            rootConnection?.setSystemUIRestartListener()
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to set SystemUI restart listener", e)
        }
    }

    @Test
    private val testBackgroundService = false

    @Test
    private val isTestingService = BuildConfig.DEBUG && testBackgroundService

    @Test
    var counter: Int = 0

    @Test
    private var timer: Timer? = null

    init {
        isRunning = false
        myReceiver = BroadcastListener()
    }

    @Test
    fun startTimer(context: Context) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                Log.i(TEST_TAG, "Timer is running " + counter++)
                broadcastActionTest(context, counter.toString())
            }
        }, 1000, 1000)
    }

    @Test
    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    companion object {
        private val TAG: String = AutoStartService::class.java.simpleName
        private var isRunning = false
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "Background Service"
        private lateinit var myReceiver: BroadcastListener
        val isServiceNotRunning: Boolean
            get() = !isRunning

        /*
         * The following fields and methods are for testing purposes only
         */
        @Test
        private val TEST_TAG = AutoStartService::class.java.simpleName + "_TEST"

        @Test
        private val packageName: String = appContext.packageName

        @Test
        val ACTION_FOO: String = "$packageName.FOO"

        @Test
        val EXTRA_PARAM_A: String = "$packageName.PARAM_A"

        @Test
        fun broadcastActionTest(context: Context, param: String?) {
            val intent = Intent(ACTION_FOO)
            intent.putExtra(EXTRA_PARAM_A, param)
            val bm = LocalBroadcastManager.getInstance(context)
            bm.sendBroadcast(intent)
        }
    }
}

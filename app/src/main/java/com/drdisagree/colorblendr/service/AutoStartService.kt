package com.drdisagree.colorblendr.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
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
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.extension.MethodInterface
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.utils.ColorUtil.getAccentColor
import com.drdisagree.colorblendr.utils.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.ShizukuUtil.hasShizukuPermission
import com.drdisagree.colorblendr.utils.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.SystemUtil.getScreenRotation
import com.drdisagree.colorblendr.utils.SystemUtil.sensorEventListener
import com.drdisagree.colorblendr.utils.annotations.TestingOnly
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

        if (BroadcastListener.Companion.lastOrientation == -1) {
            BroadcastListener.Companion.lastOrientation = getScreenRotation(
                this
            )
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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
        } catch (ignored: Exception) {
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

        if (sensorManager == null) {
            sensorManager = getSystemService(SensorManager::class.java)

            if (sensorManager != null) {
                sensorManager!!.registerListener(
                    sensorEventListener,
                    sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.background_service_notification_channel_title),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = getString(R.string.background_service_notification_channel_text)
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun showNotification() {
        val notificationIntent = Intent()
        notificationIntent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        notificationIntent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        notificationIntent.putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)

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

        startForeground(NOTIFICATION_ID, notification)
    }

    @Suppress("DEPRECATION")
    private fun registerReceivers() {
        val intentFilterWithoutScheme = IntentFilter()
        intentFilterWithoutScheme.addAction(Intent.ACTION_WALLPAPER_CHANGED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_SCREEN_OFF)
        intentFilterWithoutScheme.addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_REPLACED)

        val intentFilterWithScheme = IntentFilter()
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilterWithScheme.addDataScheme("package")

        registerReceiver(myReceiver, intentFilterWithoutScheme)
        registerReceiver(myReceiver, intentFilterWithScheme)
    }

    private fun setupSystemUIRestartListener() {
        if (workingMethod == Const.WorkMethod.ROOT &&
            RootConnectionProvider.isNotConnected
        ) {
            RootConnectionProvider.builder(appContext)
                .runOnSuccess(object : MethodInterface() {
                    override fun run() {
                        initSystemUIRestartListener()
                    }
                })
                .run()
        } else if (workingMethod == Const.WorkMethod.SHIZUKU &&
            ShizukuConnectionProvider.isNotConnected &&
            isShizukuAvailable &&
            hasShizukuPermission(appContext)
        ) {
            bindUserService(
                getUserServiceArgs(ShizukuConnection::class.java),
                ShizukuConnectionProvider.serviceConnection
            )
        } else if (workingMethod == Const.WorkMethod.ROOT) {
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

    @TestingOnly
    private val testBackgroundService = false

    @TestingOnly
    private val isTestingService = BuildConfig.DEBUG && testBackgroundService

    @TestingOnly
    var counter: Int = 0

    @TestingOnly
    private var timer: Timer? = null

    init {
        isRunning = false
        myReceiver = BroadcastListener()
    }

    @TestingOnly
    fun startTimer(context: Context) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                Log.i(TEST_TAG, "Timer is running " + counter++)
                broadcastActionTest(context, counter.toString())
            }
        }, 1000, 1000)
    }

    @TestingOnly
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
        var sensorManager: SensorManager? = null

        @JvmStatic
        val isServiceNotRunning: Boolean
            get() = !isRunning

        /*
    * The following fields and methods are for testing purposes only
    */
        @TestingOnly
        private val TEST_TAG = AutoStartService::class.java.simpleName + "_TEST"

        @TestingOnly
        private val packageName: String = appContext.packageName

        @TestingOnly
        val ACTION_FOO: String = "$packageName.FOO"

        @TestingOnly
        val EXTRA_PARAM_A: String = "$packageName.PARAM_A"

        @TestingOnly
        fun broadcastActionTest(context: Context, param: String?) {
            val intent = Intent(ACTION_FOO)
            intent.putExtra(EXTRA_PARAM_A, param)
            val bm = LocalBroadcastManager.getInstance(context)
            bm.sendBroadcast(intent)
        }
    }
}

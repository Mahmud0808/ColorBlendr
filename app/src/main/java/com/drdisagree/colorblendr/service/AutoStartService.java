package com.drdisagree.colorblendr.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.utils.annotations.TestingOnly;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootConnectionProvider;
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.ShizukuUtil;
import com.drdisagree.colorblendr.utils.SystemUtil;

import java.util.Timer;
import java.util.TimerTask;

public class AutoStartService extends Service {

    private static final String TAG = AutoStartService.class.getSimpleName();
    private static boolean isRunning = false;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "Background Service";
    private static BroadcastListener myReceiver;
    private NotificationManager notificationManager;

    public AutoStartService() {
        isRunning = false;
        myReceiver = new BroadcastListener();
    }

    public static boolean isServiceNotRunning() {
        return !isRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;
        registerSystemServices();
        createNotificationChannel();
        showNotification();
        registerReceivers();

        if (BroadcastListener.lastOrientation == -1) {
            BroadcastListener.lastOrientation = SystemUtil.getScreenRotation(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        setupSystemUIRestartListener();

        if (isTestingService) {
            // Testing purposes only
            startTimer(this);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRunning = false;
        Log.i(TAG, "onDestroy: Service is destroyed :(");

        try {
            unregisterReceiver(myReceiver);
        } catch (Exception ignored) {
            // Receiver was probably never registered
        }

        Intent broadcastIntent = new Intent(this, RestartBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

        if (isTestingService) {
            // Testing purposes only
            stopTimer();
        }
    }

    private void registerSystemServices() {
        if (notificationManager == null) {
            notificationManager = getSystemService(NotificationManager.class);
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.background_service_notification_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(getString(R.string.background_service_notification_channel_text));
        notificationManager.createNotificationChannel(channel);
    }

    private void showNotification() {
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        notificationIntent.putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_ID
        )
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle(getString(R.string.background_service_notification_title))
                .setContentText(getString(R.string.background_service_notification_text))
                .setContentIntent(pendingIntent)
                .setSound(null, AudioManager.STREAM_NOTIFICATION)
                .setColor(ColorUtil.getAccentColor(this))
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @SuppressWarnings("all")
    private void registerReceivers() {
        IntentFilter intentFilterWithoutScheme = new IntentFilter();
        intentFilterWithoutScheme.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        intentFilterWithoutScheme.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        intentFilterWithoutScheme.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilterWithoutScheme.addAction(Intent.ACTION_MY_PACKAGE_REPLACED);
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_REPLACED);

        IntentFilter intentFilterWithScheme = new IntentFilter();
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilterWithScheme.addDataScheme("package");

        registerReceiver(myReceiver, intentFilterWithoutScheme);
        registerReceiver(myReceiver, intentFilterWithScheme);
    }

    private void setupSystemUIRestartListener() {
        if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT &&
                RootConnectionProvider.isNotConnected()
        ) {
            RootConnectionProvider.builder(ColorBlendr.getAppContext())
                    .runOnSuccess(new MethodInterface() {
                        @Override
                        public void run() {
                            initSystemUIRestartListener();
                        }
                    })
                    .run();
        } else if (Const.getWorkingMethod() == Const.WORK_METHOD.SHIZUKU &&
                ShizukuConnectionProvider.isNotConnected() &&
                ShizukuUtil.isShizukuAvailable() &&
                ShizukuUtil.hasShizukuPermission(ColorBlendr.getAppContext())
        ) {
            ShizukuUtil.bindUserService(
                    ShizukuUtil.getUserServiceArgs(ShizukuConnection.class),
                    ShizukuConnectionProvider.serviceConnection
            );
        } else if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT) {
            initSystemUIRestartListener();
        }
    }

    private void initSystemUIRestartListener() {
        try {
            ColorBlendr.getRootConnection().setSystemUIRestartListener();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set SystemUI restart listener", e);
        }
    }

    /*
     * The following fields and methods are for testing purposes only
     */
    @TestingOnly
    private static final String TEST_TAG = AutoStartService.class.getSimpleName() + "_TEST";
    @TestingOnly
    private final boolean TEST_BACKGROUND_SERVICE = false;
    @TestingOnly
    private final boolean isTestingService = BuildConfig.DEBUG && TEST_BACKGROUND_SERVICE;
    @TestingOnly
    public int counter = 0;
    @TestingOnly
    private Timer timer;
    @TestingOnly
    private static final String packageName = ColorBlendr.getAppContext().getPackageName();
    @TestingOnly
    public static final String ACTION_FOO = packageName + ".FOO";
    @TestingOnly
    public static final String EXTRA_PARAM_A = packageName + ".PARAM_A";

    @TestingOnly
    public void startTimer(Context context) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TEST_TAG, "Timer is running " + counter++);
                broadcastActionTest(context, String.valueOf(counter));
            }
        }, 1000, 1000);
    }

    @TestingOnly
    public static void broadcastActionTest(Context context, String param) {
        Intent intent = new Intent(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM_A, param);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(context);
        bm.sendBroadcast(intent);
    }

    @TestingOnly
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}

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

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.utils.ColorUtil;

public class BackgroundService extends Service {

    private static final String TAG = BackgroundService.class.getSimpleName();
    private static boolean isRunning = false;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "Background Service";
    private static BroadcastListener myReceiver;

    public BackgroundService() {
        isRunning = false;
        myReceiver = new BroadcastListener();
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
        createNotificationChannel();
        registerReceivers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        setupSystemUIRestartListener();

        return START_STICKY;
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

    @SuppressWarnings("deprecation")
    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_MY_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");

        registerReceiver(myReceiver, intentFilter);
    }

    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.background_service_notification_channel_title),
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription(getString(R.string.background_service_notification_channel_text));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void setupSystemUIRestartListener() {
        if (RootServiceProvider.isNotConnected()) {
            RootServiceProvider rootServiceProvider = new RootServiceProvider(ColorBlendr.getAppContext());
            rootServiceProvider.runOnSuccess(new MethodInterface() {
                @Override
                public void run() {
                    setupSysUIRestartListener();
                }
            });
            rootServiceProvider.startRootService();
        } else {
            setupSysUIRestartListener();
        }
    }

    private void setupSysUIRestartListener() {
        try {
            ColorBlendr.getRootService().setSystemUIRestartListener();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set SystemUI restart listener", e);
        }
    }

    public static boolean isServiceNotRunning() {
        return !isRunning;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        stopForeground(true);
        try {
            unregisterReceiver(myReceiver);
        } catch (Exception ignored) {
            // Receiver was probably never registered
        }
        Intent broadcastIntent = new Intent(this, ServiceDestroyedListener.class);
        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }
}

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
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.utils.ColorUtil;

public class BackgroundService extends Service {

    private static final String TAG = BackgroundService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "Background Service";
    private static final BroadcastListener myReceiver = new BroadcastListener();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Const.isBackgroundServiceRunning = true;

        registerReceivers();
        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        showNotification();
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @SuppressWarnings("deprecation")
    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        registerReceiver(myReceiver, intentFilter);
    }

    public void createChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.background_service_notification_channel_title),
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription(getString(R.string.background_service_notification_channel_text));
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(myReceiver);
        } catch (Exception ignored) {
            // Receiver was probably never registered
        }
        super.onDestroy();
    }
}

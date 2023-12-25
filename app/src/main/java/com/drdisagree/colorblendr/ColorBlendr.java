package com.drdisagree.colorblendr;

import static com.drdisagree.colorblendr.common.Const.TAB_SELECTED_INDEX;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.service.IRootServiceProvider;
import com.drdisagree.colorblendr.service.RootServiceProvider;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.ipc.RootService;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ColorBlendr extends Application {

    private static final String TAG = ColorBlendr.class.getSimpleName();
    private static ColorBlendr instance;
    private static WeakReference<Context> contextReference;
    private static IRootServiceProvider mRootServiceProvider;
    private static final CountDownLatch rootServiceLatch = new CountDownLatch(1);

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
        DynamicColors.applyToActivitiesIfAvailable(this);
        RPrefs.clearPref(TAB_SELECTED_INDEX);
        startRootService();
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(ColorBlendr.getInstance().getApplicationContext());
        }
        return contextReference.get();
    }

    private static ColorBlendr getInstance() {
        if (instance == null) {
            instance = new ColorBlendr();
        }
        return instance;
    }

    public static IRootServiceProvider getRootService() {
        if (mRootServiceProvider == null) {
            new Handler(Looper.getMainLooper()).post(ColorBlendr::startRootService);
        }

        return mRootServiceProvider;
    }

    private static void startRootService() {
        Log.i(TAG, "Starting RootService...");

        RootService.bind(
                new Intent(getAppContext(), RootServiceProvider.class),
                new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        mRootServiceProvider = IRootServiceProvider.Stub.asInterface(service);
                        Log.i(TAG, "RootService connected.");
                        rootServiceLatch.countDown();
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        mRootServiceProvider = null;
                        Log.i(TAG, "RootService disconnected.");
                        rootServiceLatch.countDown();
                    }
                }
        );
    }

    private static void waitForRootService() {
        try {
            boolean success = rootServiceLatch.await(5, TimeUnit.SECONDS);
            if (!success) {
                Log.e(TAG, "Timeout waiting for RootService connection.");
                Toast.makeText(
                        ColorBlendr.getAppContext(),
                        R.string.root_service_timeout,
                        Toast.LENGTH_LONG
                ).show();
                System.exit(0);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for RootService connection.");
            Thread.currentThread().interrupt();
            Toast.makeText(
                    ColorBlendr.getAppContext(),
                    R.string.root_service_interrupted,
                    Toast.LENGTH_LONG
            ).show();
            System.exit(0);
        }
    }
}
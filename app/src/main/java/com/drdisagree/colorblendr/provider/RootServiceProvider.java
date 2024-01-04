package com.drdisagree.colorblendr.provider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.service.IRootService;
import com.drdisagree.colorblendr.service.RootService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RootServiceProvider implements ServiceConnection {

    private static final String TAG = RootServiceProvider.class.getSimpleName();
    private final Context context;
    private static IRootService rootServiceProviderIPC;
    private static boolean isRootServiceBound = false;
    private static MethodInterface methodRunOnSuccess;
    private static MethodInterface methodRunOnFailure;
    private static final CountDownLatch mRootServiceConnectionTimer = new CountDownLatch(1);

    public RootServiceProvider(Context context) {
        this.context = context;
    }

    public static IRootService getRootServiceProvider() {
        return rootServiceProviderIPC;
    }

    public static boolean isNotConnected() {
        return !isRootServiceBound;
    }

    public void startRootService() {
        new RootServiceThread().start();
    }

    private void bindRootService() {
        if (isRootServiceBound) {
            mRootServiceConnectionTimer.countDown();
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> RootService.bind(
                new Intent(context, RootService.class),
                this
        ));
    }

    public class RootServiceThread extends Thread {
        @Override
        public void run() {
            try {
                bindRootService();
                boolean success = mRootServiceConnectionTimer.await(10, TimeUnit.SECONDS);
                new Handler(Looper.getMainLooper()).post(
                        success ?
                                new SuccessRunnable() :
                                new FailureRunnable()
                );
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(
                        ColorBlendr.getAppContext(),
                        R.string.something_went_wrong,
                        Toast.LENGTH_LONG
                ).show());
                Log.e(TAG, "Error starting root service", e);
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        rootServiceProviderIPC = IRootService.Stub.asInterface(binder);
        isRootServiceBound = true;
        mRootServiceConnectionTimer.countDown();
        Log.d(TAG, "Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        rootServiceProviderIPC = null;
        isRootServiceBound = false;
        Log.d(TAG, "Service disconnected");
        bindRootService();
    }

    static class FailureRunnable implements Runnable {
        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (methodRunOnFailure != null) {
                    methodRunOnFailure.run();
                } else {
                    Toast.makeText(
                            ColorBlendr.getAppContext(),
                            R.string.root_service_not_found,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
    }

    static class SuccessRunnable implements Runnable {
        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (methodRunOnSuccess != null) {
                    methodRunOnSuccess.run();
                }
            });
        }
    }

    public void runOnSuccess(MethodInterface method) {
        methodRunOnSuccess = method;
    }

    public void runOnFailure(MethodInterface method) {
        methodRunOnFailure = method;
    }
}
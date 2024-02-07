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
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.service.IRootConnection;
import com.drdisagree.colorblendr.service.RootConnection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RootConnectionProvider implements ServiceConnection {

    private static final String TAG = RootConnectionProvider.class.getSimpleName();
    private final Context context;
    private static IRootConnection serviceProviderIPC;
    private static boolean isServiceConnected = false;
    private MethodInterface methodRunOnSuccess;
    private MethodInterface methodRunOnFailure;
    private static final CountDownLatch mServiceConnectionTimer = new CountDownLatch(1);

    private RootConnectionProvider(Context context) {
        this.context = context;
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static IRootConnection getServiceProvider() {
        return serviceProviderIPC;
    }

    public static boolean isNotConnected() {
        return !isServiceConnected;
    }

    private void bindServiceConnection() {
        if (isServiceConnected) {
            mServiceConnectionTimer.countDown();
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> RootConnection.bind(
                new Intent(context, RootConnection.class),
                this
        ));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        if (binder == null || !binder.pingBinder()) {
            Log.w(TAG, "Service binder is null or not alive");
            return;
        }

        serviceProviderIPC = IRootConnection.Stub.asInterface(binder);
        isServiceConnected = true;
        mServiceConnectionTimer.countDown();
        Log.i(TAG, "Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serviceProviderIPC = null;
        isServiceConnected = false;
        Log.w(TAG, "Service disconnected");
        bindServiceConnection();
    }

    public static class ServiceConnectionThread extends Thread {
        private final RootConnectionProvider instance;

        public ServiceConnectionThread(RootConnectionProvider instance) {
            this.instance = instance;
        }

        @Override
        public void run() {
            try {
                instance.bindServiceConnection();
                boolean success = mServiceConnectionTimer.await(10, TimeUnit.SECONDS);
                new Handler(Looper.getMainLooper()).post(
                        success ?
                                new SuccessRunnable(instance) :
                                new FailureRunnable(instance)
                );
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(
                        ColorBlendr.getAppContext(),
                        R.string.something_went_wrong,
                        Toast.LENGTH_LONG
                ).show());
                Log.e(TAG, "Error starting service connection", e);
            }
        }
    }

    private record SuccessRunnable(RootConnectionProvider instance) implements Runnable {
        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (instance.methodRunOnSuccess != null) {
                    instance.methodRunOnSuccess.run();
                }
            });
        }
    }

    private record FailureRunnable(RootConnectionProvider instance) implements Runnable {
        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (instance.methodRunOnFailure != null) {
                    instance.methodRunOnFailure.run();
                } else {
                    if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT ||
                            Const.WORKING_METHOD == Const.WORK_METHOD.ROOT
                    ) {
                        Toast.makeText(
                                ColorBlendr.getAppContext(),
                                R.string.root_service_not_found,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            });
        }
    }

    @SuppressWarnings("all")
    public static class Builder {
        private final RootConnectionProvider instance;

        public Builder(Context context) {
            instance = new RootConnectionProvider(context);
        }

        public Builder runOnSuccess(MethodInterface method) {
            instance.methodRunOnSuccess = method;
            return this;
        }

        public Builder runOnFailure(MethodInterface method) {
            instance.methodRunOnFailure = method;
            return this;
        }

        public void run() {
            new ServiceConnectionThread(instance).start();
        }
    }
}

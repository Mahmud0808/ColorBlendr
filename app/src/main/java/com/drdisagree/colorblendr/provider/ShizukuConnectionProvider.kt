package com.drdisagree.colorblendr.provider;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.drdisagree.colorblendr.service.IShizukuConnection;
import com.drdisagree.colorblendr.service.ShizukuConnection;
import com.drdisagree.colorblendr.utils.ShizukuUtil;

public class ShizukuConnectionProvider {

    private static final String TAG = ShizukuConnectionProvider.class.getSimpleName();
    private static IShizukuConnection serviceProviderIPC;
    private static boolean isServiceConnected = false;

    public static final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder == null || !binder.pingBinder()) {
                Log.w(TAG, "Service binder is null or not alive");
                return;
            }

            serviceProviderIPC = IShizukuConnection.Stub.asInterface(binder);
            isServiceConnected = true;
            Log.i(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceProviderIPC = null;
            isServiceConnected = false;
            Log.w(TAG, "Service disconnected");
            bindServiceConnection();
        }
    };

    public static IShizukuConnection getServiceProvider() {
        return serviceProviderIPC;
    }

    public static boolean isNotConnected() {
        return !isServiceConnected;
    }

    private static void bindServiceConnection() {
        if (isServiceConnected) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> ShizukuUtil.bindUserService(
                ShizukuUtil.getUserServiceArgs(ShizukuConnection.class),
                serviceConnection
        ));
    }
}

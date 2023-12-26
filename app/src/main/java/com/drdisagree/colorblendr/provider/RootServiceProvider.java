package com.drdisagree.colorblendr.provider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.service.IRootService;
import com.drdisagree.colorblendr.service.RootService;
import com.drdisagree.colorblendr.ui.fragments.HomeFragment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RootServiceProvider implements ServiceConnection {

    private static final String TAG = RootServiceProvider.class.getSimpleName();
    private final Context context;
    private final FragmentManager fragmentManager;
    private final int containerId;
    private static IRootService rootServiceProviderIPC;
    private static boolean isRootServiceBound = false;
    private static final CountDownLatch mRootServiceConnectionTimer = new CountDownLatch(1);

    public RootServiceProvider(Context context, FragmentManager fragmentManager, @IdRes int containerId) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    public static IRootService getRootServiceProvider() {
        return rootServiceProviderIPC;
    }

    public static boolean isRootServiceBound() {
        return isRootServiceBound;
    }

    public void startRootService() {
        new RootServiceThread().start();
    }

    private void bindRootService() {
        ((Activity) context).runOnUiThread(() -> RootService.bind(
                new Intent(context, RootService.class),
                this
        ));
    }

    public class RootServiceThread extends Thread {
        @Override
        public void run() {
            try {
                bindRootService();
                boolean success = mRootServiceConnectionTimer.await(5, TimeUnit.SECONDS);
                ((Activity) context).runOnUiThread(success ? new SuccessRunnable() : new ErrorRunnable());
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(
                        ColorBlendr.getAppContext(),
                        R.string.something_went_wrong,
                        Toast.LENGTH_LONG
                ).show());
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        rootServiceProviderIPC = IRootService.Stub.asInterface(service);
        isRootServiceBound = true;
        mRootServiceConnectionTimer.countDown();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        rootServiceProviderIPC = null;
        isRootServiceBound = false;
        bindRootService();
    }

    static class ErrorRunnable implements Runnable {
        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(
                    ColorBlendr.getAppContext(),
                    R.string.root_service_not_found,
                    Toast.LENGTH_LONG
            ).show());
        }
    }

    class SuccessRunnable implements Runnable {
        @Override
        public void run() {
            Const.saveWorkingMethod();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(
                    containerId,
                    new HomeFragment(),
                    HomeFragment.class.getSimpleName()
            );
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.commit();
        }
    }
}

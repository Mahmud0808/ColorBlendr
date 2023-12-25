package com.drdisagree.colorblendr.ui.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.service.IRootServiceProvider;
import com.drdisagree.colorblendr.service.RootServiceProvider;
import com.google.android.material.color.DynamicColors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements ServiceConnection {

    private final static String TAG = SplashActivity.class.getSimpleName();
    private boolean keepShowing = true;
    private static IRootServiceProvider rootServiceProviderIPC;
    private final CountDownLatch mRootServiceConnectionTimer = new CountDownLatch(1);
    private final Runnable runner = () -> {
        keepShowing = false;
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        new RootServiceThread().start();
    }

    public static IRootServiceProvider getRootServiceProvider() {
        return rootServiceProviderIPC;
    }

    private boolean connectRootService() {
        try {
            bindRootService();
            return mRootServiceConnectionTimer.await(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void bindRootService() {
        this.runOnUiThread(() -> RootServiceProvider.bind(
                new Intent(this, RootServiceProvider.class),
                this
        ));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        rootServiceProviderIPC = IRootServiceProvider.Stub.asInterface(service);
        mRootServiceConnectionTimer.countDown();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        rootServiceProviderIPC = null;
        bindRootService();
    }

    class RootServiceThread extends Thread {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 2; i++) {
                    if (connectRootService()) {
                        break;
                    }
                }

                if (mRootServiceConnectionTimer.getCount() == 0) {
                    new Thread(runner).start();
                } else {
                    runOnUiThread(new ErrorRunnable());
                }
            } catch (Exception e) {
                Toast.makeText(
                        ColorBlendr.getAppContext(),
                        R.string.root_service_interrupted,
                        Toast.LENGTH_LONG
                ).show();
                Log.e(TAG, e.toString());
            }
        }
    }

    class ErrorRunnable implements Runnable {
        @Override
        public void run() {
            Toast.makeText(
                    ColorBlendr.getAppContext(),
                    R.string.root_service_timeout,
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }
    }
}

package com.drdisagree.colorblendr.ui.activities;

import static com.drdisagree.colorblendr.common.Const.FIRST_RUN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.utils.FabricatedUtil;
import com.drdisagree.colorblendr.utils.WallpaperUtil;
import com.google.android.material.color.DynamicColors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private boolean keepShowing = true;
    private final Runnable runnable = () -> {
        final AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        if (!RPrefs.getBoolean(FIRST_RUN, true)) {
            RootServiceProvider rootServiceProvider = new RootServiceProvider(ColorBlendr.getAppContext());
            rootServiceProvider.runOnSuccess(new MethodInterface() {
                @Override
                public void run() {
                    WallpaperUtil.getAndSaveWallpaperColors(getApplicationContext());
                    FabricatedUtil.getAndSaveSelectedFabricatedApps(getApplicationContext());
                    success.set(true);
                    keepShowing = false;
                    countDownLatch.countDown();
                }
            });
            rootServiceProvider.runOnFailure(new MethodInterface() {
                @Override
                public void run() {
                    success.set(false);
                    keepShowing = false;
                    countDownLatch.countDown();
                }
            });
            rootServiceProvider.startRootService();
        } else {
            keepShowing = false;
            countDownLatch.countDown();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("success", success.get());
        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);

        new Thread(runnable).start();
    }
}

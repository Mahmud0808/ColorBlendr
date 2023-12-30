package com.drdisagree.colorblendr.ui.activities;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.utils.AppUtil;
import com.drdisagree.colorblendr.utils.FabricatedUtil;
import com.drdisagree.colorblendr.utils.WallpaperUtil;
import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private boolean keepShowing = true;
    private final Runnable runnable = () -> {
        saveWallpaperColors();

        final boolean[] success = new boolean[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);

        if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT) {
            RootServiceProvider rootServiceProvider = new RootServiceProvider(ColorBlendr.getAppContext());
            rootServiceProvider.runOnSuccess(new MethodInterface() {
                @Override
                public void run() {
                    FabricatedUtil.getAndSaveSelectedFabricatedApps(getApplicationContext());
                    success[0] = true;
                    keepShowing = false;
                    countDownLatch.countDown();
                }
            });
            rootServiceProvider.runOnFailure(new MethodInterface() {
                @Override
                public void run() {
                    success[0] = false;
                    keepShowing = false;
                    countDownLatch.countDown();
                }
            });
            rootServiceProvider.startRootService();
        } else {
            keepShowing = false;
            success[0] = true;
            countDownLatch.countDown();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("success", success[0]);
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

    private void saveWallpaperColors() {
        if (RPrefs.getInt(MONET_SEED_COLOR, -1) == -1 &&
                AppUtil.permissionsGranted(getApplicationContext())
        ) {
            ArrayList<Integer> wallpaperColors = WallpaperUtil.getWallpaperColors(getApplicationContext());
            RPrefs.putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors));
            RPrefs.putInt(MONET_SEED_COLOR, wallpaperColors.get(0));
        }
    }
}

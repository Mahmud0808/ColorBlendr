package com.drdisagree.colorblendr.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.google.android.material.color.DynamicColors;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private boolean keepShowing = true;
    private final Runnable runnable = () -> {
        final boolean[] success = new boolean[1];

        if (Const.getWorkingMethod() == Const.WORK_METHOD.ROOT) {
            RootServiceProvider rootServiceProvider = new RootServiceProvider(ColorBlendr.getAppContext());
            rootServiceProvider.runOnSuccess(new MethodInterface() {
                @Override
                public void run() {
                    success[0] = true;
                    keepShowing = false;
                }
            });
            rootServiceProvider.runOnFailure(new MethodInterface() {
                @Override
                public void run() {
                    success[0] = false;
                    keepShowing = false;
                }
            });
            rootServiceProvider.startRootService();
        } else {
            keepShowing = false;
            success[0] = true;
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
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        new Thread(runnable).start();
    }
}

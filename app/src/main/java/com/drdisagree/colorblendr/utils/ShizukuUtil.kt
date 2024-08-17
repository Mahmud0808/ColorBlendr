package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.SHIZUKU_PERMISSION_REQUEST_ID;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.drdisagree.colorblendr.BuildConfig;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuProvider;

public class ShizukuUtil {

    public static boolean isShizukuAvailable() {
        return Shizuku.pingBinder();
    }

    public static boolean hasShizukuPermission(Context context) {
        if (!isShizukuAvailable()) {
            return false;
        }

        if (Shizuku.getVersion() >= 11 && !Shizuku.isPreV11()) {
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } else {
            return context.checkCallingOrSelfPermission(ShizukuProvider.PERMISSION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestShizukuPermission(ComponentActivity activity, ShizukuPermissionCallback callback) {
        if (Shizuku.getVersion() >= 11 && !Shizuku.isPreV11()) {
            Shizuku.addRequestPermissionResultListener(new Shizuku.OnRequestPermissionResultListener() {
                @Override
                public void onRequestPermissionResult(int requestCode, int grantResult) {
                    Shizuku.removeRequestPermissionResultListener(this);
                    callback.onPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED);
                }
            });
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_ID);
        } else {
            ActivityResultLauncher<String> permCallback = activity.registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    callback::onPermissionResult
            );

            permCallback.launch(ShizukuProvider.PERMISSION);
        }
    }

    public interface ShizukuPermissionCallback {
        void onPermissionResult(boolean granted);
    }

    public static Shizuku.UserServiceArgs getUserServiceArgs(Class<?> className) {
        return new Shizuku.UserServiceArgs(new ComponentName(
                BuildConfig.APPLICATION_ID,
                className.getName()
        ))
                .daemon(false)
                .processNameSuffix("user_service")
                .debuggable(BuildConfig.DEBUG)
                .version(BuildConfig.VERSION_CODE);
    }

    public static void bindUserService(Shizuku.UserServiceArgs userServiceArgs, ServiceConnection serviceConnection) {
        Shizuku.bindUserService(userServiceArgs, serviceConnection);
    }

    public static void unbindUserService(Shizuku.UserServiceArgs userServiceArgs, ServiceConnection serviceConnection) {
        Shizuku.unbindUserService(userServiceArgs, serviceConnection, true);
    }
}

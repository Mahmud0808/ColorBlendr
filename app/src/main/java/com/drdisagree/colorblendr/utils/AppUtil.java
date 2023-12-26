package com.drdisagree.colorblendr.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.drdisagree.colorblendr.BuildConfig;

public class AppUtil {

    public static final String[] REQUIRED_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
            new String[]{
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES
            } :
            new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };

    public static boolean permissionsGranted(Context context) {
        if (!hasStoragePermission()) {
            return false;
        }

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasStoragePermission() {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy();
    }

    public static void requestStoragePermission(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        ((Activity) context).startActivityForResult(intent, 0);

        ActivityCompat.requestPermissions((Activity) context, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }, 0);
    }
}

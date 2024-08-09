package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.screenOrientation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.drdisagree.colorblendr.ColorBlendr;

public class SystemUtil {

    public static boolean isDarkMode() {
        return (ColorBlendr.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isAppInstalled(String package_name) {
        try {
            ColorBlendr.getAppContext()
                    .getPackageManager()
                    .getPackageInfo(package_name, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static int getScreenRotation(Context context) {
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int rotation = switch (display.getRotation()) {
            case Surface.ROTATION_0 -> 0;
            case Surface.ROTATION_90 -> 90;
            case Surface.ROTATION_180 -> 180;
            case Surface.ROTATION_270 -> 270;
            default -> -1;
        };

        if (rotation == -1) {
            rotation = screenOrientation.get();
        }

        return rotation;
    }

    public static final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (Math.abs(event.values[1]) > Math.abs(event.values[0]) && Math.abs(event.values[1]) > Math.abs(event.values[2])) { // Vertical
                if (event.values[1] > 0) {
                    screenOrientation.set(0); // Head Up
                } else {
                    screenOrientation.set(180); // Head Down
                }
            } else if (Math.abs(event.values[0]) > Math.abs(event.values[1]) && Math.abs(event.values[0]) > Math.abs(event.values[2])) { // Horizontal
                if (event.values[0] > 0) {
                    screenOrientation.set(90); // Left
                } else {
                    screenOrientation.set(270); // Right
                }
            } else { // Flat
                screenOrientation.set(0);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}

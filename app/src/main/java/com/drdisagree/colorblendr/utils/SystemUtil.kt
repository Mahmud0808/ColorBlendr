package com.drdisagree.colorblendr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.view.Surface
import android.view.WindowManager
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.common.Const
import kotlin.math.abs

object SystemUtil {

    val isDarkMode: Boolean
        get() = (appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

    fun isAppInstalled(packageName: String): Boolean {
        try {
            appContext
                .packageManager
                .getPackageInfo(packageName, PackageManager.GET_META_DATA)
            return true
        } catch (ignored: PackageManager.NameNotFoundException) {
            return false
        }
    }

    @Suppress("deprecation")
    fun getScreenRotation(context: Context): Int {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        var rotation = when (display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> -1
        }

        if (rotation == -1) {
            rotation = Const.screenOrientation.get()
        }

        return rotation
    }

    val sensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (abs(event.values[1].toDouble()) > abs(event.values[0].toDouble()) && abs(
                    event.values[1].toDouble()
                ) > abs(event.values[2].toDouble())
            ) { // Vertical
                if (event.values[1] > 0) {
                    Const.screenOrientation.set(0) // Head Up
                } else {
                    Const.screenOrientation.set(180) // Head Down
                }
            } else if (abs(event.values[0].toDouble()) > abs(event.values[1].toDouble()) && abs(
                    event.values[0].toDouble()
                ) > abs(event.values[2].toDouble())
            ) { // Horizontal
                if (event.values[0] > 0) {
                    Const.screenOrientation.set(90) // Left
                } else {
                    Const.screenOrientation.set(270) // Right
                }
            } else { // Flat
                Const.screenOrientation.set(0)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }
}

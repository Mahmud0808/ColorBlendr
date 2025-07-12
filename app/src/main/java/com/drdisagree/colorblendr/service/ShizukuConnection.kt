package com.drdisagree.colorblendr.service

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.drdisagree.colorblendr.data.common.Constant.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.topjohnwu.superuser.Shell
import org.json.JSONObject
import kotlin.system.exitProcess

class ShizukuConnection : IShizukuConnection.Stub {

    companion object {
        private val TAG: String = ShizukuConnection::class.java.simpleName
    }

    @Suppress("unused")
    constructor() {
        Log.i(TAG, "Constructed with no arguments")
    }

    @Suppress("unused")
    @Keep
    constructor(context: Context) {
        Log.i(TAG, "Constructed with context: $context")
    }

    /**
     * Destroys the application by terminating the current process.
     *
     * This function calls `exitProcess(0)` which immediately terminates the
     * running process with an exit code of 0, indicating successful execution.
     * Any cleanup or resource release should be performed before calling this function.
     */
    override fun destroy() {
        exitProcess(0)
    }

    /**
     * Exits the current context, performing any necessary cleanup or destruction.
     *
     * This function is called when the context is no longer needed and should be terminated.
     * It delegates the cleanup process to the `destroy` function.
     */
    override fun exit() {
        destroy()
    }

    /**
     * Applies fabricated color themes to the system by setting the `THEME_CUSTOMIZATION_OVERLAY_PACKAGES` secure setting.
     *
     * This function takes a JSON string representing the desired theme customizations and applies them
     * by executing a shell command that updates the secure setting.
     *
     * @param jsonString The JSON string containing the theme customization data. This string is expected to
     *                   be in the format required by the `THEME_CUSTOMIZATION_OVERLAY_PACKAGES` setting.
     */
    override fun applyFabricatedColors(jsonString: String) {
        Shell.cmd(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'"
        ).exec()
    }

    /**
     * Removes the fabricated colors applied to the UI and restores the original colors.
     *
     * This function attempts to revert any color modifications made by `applyFabricatedColors`
     * by reapplying the original color settings stored in `originalSettings`.
     *
     * In case of any errors during the color restoration process, the exception is logged
     * with an "ERROR" tag to the console.
     */
    override fun removeFabricatedColors() {
        try {
            applyFabricatedColors(
                ThemeOverlayPackage.getOriginalSettings(currentSettings).toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "removeFabricatedColors: ", e)
        }
    }

    /**
     * Retrieves the current theme customization settings from the device.
     *
     * This function executes a shell command to get the value of the
     * `THEME_CUSTOMIZATION_OVERLAY_PACKAGES` secure setting.
     * If the setting is not set (value is "null"), it returns an empty JSON object as a string.
     * Otherwise, it returns the current value of the setting as a string.
     *
     * @return A string representation of the current theme customization settings.
     *         If the settings are not set, an empty JSON object is returned as a string.
     */
    override fun getCurrentSettings(): String {
        val currentSettings = Shell.cmd(
            "settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES"
        ).exec().out[0]

        return if (currentSettings == "null") {
            JSONObject().toString()
        } else {
            currentSettings
        }
    }
}

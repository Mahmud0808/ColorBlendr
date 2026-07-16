package com.drdisagree.colorblendr.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.toColorInt
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCENT_SATURATION_LIGHT
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_LIGHTNESS_LIGHT
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_SATURATION_LIGHT
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.modeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.screenOffColorUpdateEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.setAccurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setModeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setPitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.taskerIntegrationEnabled
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.enums.MONET.Companion.toEnumMonet
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

class TaskerIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_APPLY_CONFIG) return

        if (!taskerIntegrationEnabled()) {
            Log.w(TAG, "Ignored broadcast; Tasker integration disabled")
            return
        }

        if (isWorkMethodUnknown() ||
            (!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled())
        ) return

        if (PreviewController.isPreviewActive) return

        val config = parseConfig(intent)
        var changed = false

        if (config.optBoolean(EXTRA_WALLPAPER_COLORS)) {
            getWallpaperColorList().firstOrNull()?.let { wallpaperSeed ->
                setCustomColorEnabled(false)
                setSeedColorValue(wallpaperSeed)
                changed = true
            }
        }

        config.optString(EXTRA_SEED_COLOR).takeIf { it.isNotBlank() }?.let { hex ->
            runCatching { hex.toColorInt() }.getOrNull()?.let { seed ->
                setCustomColorEnabled(true)
                setSeedColorValue(seed)
                changed = true
            }
        }

        if (config.optBoolean(EXTRA_RANDOM_COLOR)) {
            val seed = Color.HSVToColor(
                floatArrayOf(Random.nextFloat() * 360f, 0.6f + Random.nextFloat() * 0.4f, 0.9f)
            )
            setCustomColorEnabled(true)
            setSeedColorValue(seed)
            changed = true
        }

        config.optString(EXTRA_MONET_STYLE).takeIf { it.isNotBlank() }?.let { style ->
            resetCustomStyleIfNotNull()
            setCurrentMonetStyle(style.toEnumMonet())
            changed = true
        }

        val hasSliders = arrayOf(
            EXTRA_ACCENT_SATURATION, EXTRA_BACKGROUND_SATURATION, EXTRA_BACKGROUND_LIGHTNESS
        ).any { config.has(it) }
        if (hasSliders && modeSpecificThemesEnabled()) {
            setModeSpecificThemesEnabled(false)
            Prefs.clearPref(MONET_ACCENT_SATURATION_LIGHT)
            Prefs.clearPref(MONET_BACKGROUND_SATURATION_LIGHT)
            Prefs.clearPref(MONET_BACKGROUND_LIGHTNESS_LIGHT)
            changed = true
        }

        sliderValue(config, EXTRA_ACCENT_SATURATION)?.let {
            setAccentSaturation(it)
            changed = true
        }
        sliderValue(config, EXTRA_BACKGROUND_SATURATION)?.let {
            setBackgroundSaturation(it)
            changed = true
        }
        sliderValue(config, EXTRA_BACKGROUND_LIGHTNESS)?.let {
            setBackgroundLightness(it)
            changed = true
        }

        if (config.has(EXTRA_PITCH_BLACK)) {
            setPitchBlackThemeEnabled(config.optBoolean(EXTRA_PITCH_BLACK))
            changed = true
        }
        if (config.has(EXTRA_ACCURATE_SHADES)) {
            setAccurateShadesEnabled(config.optBoolean(EXTRA_ACCURATE_SHADES, true))
            changed = true
        }

        if (!changed) return

        if (screenOffColorUpdateEnabled()) {
            BroadcastListener.requiresUpdate = true
            return
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.Main).launch {
            if (isRootMode() && RootConnectionProvider.isNotConnected) {
                RootConnectionProvider
                    .builder(context)
                    .onSuccess {
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                applyFabricatedColors()
                            } finally {
                                pending.finish()
                            }
                        }
                    }
                    .onFailure { pending.finish() }
                    .run()
            } else {
                try {
                    applyFabricatedColors()
                } finally {
                    pending.finish()
                }
            }
        }
    }

    private fun parseConfig(intent: Intent): JSONObject {
        val config = intent.getStringExtra(EXTRA_CONFIG)?.let { json ->
            runCatching { JSONObject(json) }.getOrNull()
        } ?: JSONObject()
        arrayOf(EXTRA_SEED_COLOR, EXTRA_MONET_STYLE).forEach { key ->
            intent.getStringExtra(key)?.let { config.put(key, it) }
        }
        arrayOf(
            EXTRA_WALLPAPER_COLORS, EXTRA_RANDOM_COLOR,
            EXTRA_PITCH_BLACK, EXTRA_ACCURATE_SHADES
        ).forEach { key ->
            if (intent.hasExtra(key)) config.put(key, intent.getBooleanExtra(key, false))
        }
        arrayOf(
            EXTRA_ACCENT_SATURATION, EXTRA_BACKGROUND_SATURATION, EXTRA_BACKGROUND_LIGHTNESS
        ).forEach { key ->
            if (intent.hasExtra(key)) config.put(key, intent.getIntExtra(key, -1))
        }
        return config
    }

    private fun sliderValue(config: JSONObject, key: String): Int? {
        return config.optInt(key, -1).takeIf { it in 0..200 }
    }

    companion object {
        private val TAG: String = TaskerIntentReceiver::class.java.simpleName
        const val ACTION_APPLY_CONFIG = "com.drdisagree.colorblendr.action.APPLY_CONFIG"
        private const val EXTRA_CONFIG = "config"
        private const val EXTRA_SEED_COLOR = "seedColor"
        private const val EXTRA_RANDOM_COLOR = "randomColor"
        private const val EXTRA_WALLPAPER_COLORS = "wallpaperColors"
        private const val EXTRA_MONET_STYLE = "monetStyle"
        private const val EXTRA_ACCENT_SATURATION = "accentSaturation"
        private const val EXTRA_BACKGROUND_SATURATION = "backgroundSaturation"
        private const val EXTRA_BACKGROUND_LIGHTNESS = "backgroundLightness"
        private const val EXTRA_PITCH_BLACK = "pitchBlack"
        private const val EXTRA_ACCURATE_SHADES = "accurateShades"
    }
}
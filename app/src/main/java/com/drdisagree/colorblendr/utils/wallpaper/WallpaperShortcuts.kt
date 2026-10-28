package com.drdisagree.colorblendr.utils.wallpaper

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.UserManager
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.domain.AppScope
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.ui.activities.ShortcutActivity
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

object WallpaperShortcuts {

    const val ACTION_APPLY_WALLPAPER_COLOR = "com.drdisagree.colorblendr.action.APPLY_WALLPAPER_COLOR"
    const val EXTRA_SEED_COLOR = "seedColor"
    private const val TAG = "WallpaperShortcuts"
    private const val ID_PREFIX = "wallpaper_color_"
    private const val MAX_SHORTCUTS = 4
    private const val ICON_SIZE_DP = 108
    private const val SWATCH_RADIUS = 0.3f
    private const val CENTER_RADIUS = 10f / 18f

    suspend fun update(context: Context) {
        if (isFirstRun() || isWorkMethodUnknown()) return
        withContext(Dispatchers.Default) {
            runCatching { publish(context) }
                .onFailure { Log.e(TAG, "Failed to update wallpaper shortcuts", it) }
        }
    }

    private fun publish(context: Context) {
        if (!context.getSystemService(UserManager::class.java).isUserUnlocked) return

        val isDark = SystemUtil.isDarkMode
        val limit = min(MAX_SHORTCUTS, ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
        val colors = getWallpaperColorList().distinct().take(limit)
        val ids = colors.map { shortcutId(it, isDark) }

        val current = ShortcutManagerCompat.getDynamicShortcuts(context)
            .sortedBy { it.rank }
            .map { it.id }
        if (current == ids) return

        val adjustments = ThemePrefs.adjustments(isDark, includeOverrides = false)
        val shortcuts = colors.mapIndexed { index, color ->
            ShortcutInfoCompat.Builder(context, ids[index])
                .setShortLabel(hexLabel(color))
                .setIcon(icon(context, color, isDark, adjustments))
                .setIntent(
                    Intent(ACTION_APPLY_WALLPAPER_COLOR)
                        .setClass(context, ShortcutActivity::class.java)
                        .putExtra(EXTRA_SEED_COLOR, color)
                )
                .setRank(index)
                .build()
        }

        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    fun applySeed(@ColorInt color: Int): Boolean {
        if (isWorkMethodUnknown() || PreviewController.isPreviewActive) return false
        if (!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled()) {
            return false
        }

        if (customColorEnabled() || getSeedColorValue() != color) {
            resetCustomStyleIfNotNull()
        }
        setCustomColorEnabled(false)
        setSeedColorValue(color)

        AppScope.launch {
            if (isRootMode() && RootConnectionProvider.isNotConnected) {
                RootConnectionProvider
                    .builder()
                    .onSuccess {
                        AppScope.launch(Dispatchers.IO) { applyColors() }
                    }
                    .run()
            } else {
                withContext(Dispatchers.IO) { applyColors() }
            }
        }
        return true
    }

    private fun applyColors() {
        runCatching { applyFabricatedColors() }
            .onFailure { Log.e(TAG, "Failed to apply wallpaper color", it) }
    }

    private fun shortcutId(@ColorInt color: Int, isDark: Boolean): String =
        ID_PREFIX + hexLabel(color).drop(1) + if (isDark) "_dark" else "_light"

    private fun hexLabel(@ColorInt color: Int): String =
        String.format("#%06X", color and 0xFFFFFF)

    private fun icon(
        context: Context,
        @ColorInt color: Int,
        isDark: Boolean,
        adjustments: ThemeAdjustments
    ): IconCompat {
        val palette = ThemePrefs.scheme(
            isDark = isDark,
            source = ThemePrefs.source(seedColor = color),
            adjustments = adjustments
        ).shades

        val size = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val center = size / 2f
        val radius = size * SWATCH_RADIUS
        val oval = RectF(center - radius, center - radius, center + radius, center + radius)

        canvas.drawColor(palette[4][if (isDark) 9 else 3])

        fun arc(@ColorInt arcColor: Int, startAngle: Float, sweepAngle: Float) {
            paint.color = arcColor
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint)
        }

        arc(palette[0][4], 180f, 180f)
        arc(palette[2][5], 90f, 90f)
        arc(palette[1][6], 0f, 90f)

        paint.color = color
        canvas.drawCircle(center, center, radius * CENTER_RADIUS, paint)

        return IconCompat.createWithAdaptiveBitmap(bitmap)
    }
}

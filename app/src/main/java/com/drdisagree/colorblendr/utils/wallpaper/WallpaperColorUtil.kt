package com.drdisagree.colorblendr.utils.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.Size
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withClip
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorJson
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setWallpaperColorJson
import com.drdisagree.colorblendr.service.BroadcastListener
import com.drdisagree.colorblendr.utils.app.AppUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.drdisagree.materialcolorutilities.quantize.QuantizerCelebi
import com.drdisagree.materialcolorutilities.score.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.sqrt

object WallpaperColorUtil {

    private val TAG: String = "WallpaperColorUtil"
    private const val SMALL_SIDE = 128
    private const val MAX_BITMAP_SIZE = 112
    private const val MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE

    suspend fun updateWallpaperColorList(context: Context) = withContext(Dispatchers.Default) {
        if (!AppUtil.permissionsGranted(context)) return@withContext

        val wallpaperColors = getWallpaperColors(context)
        val currentWallpaperColors = Constant.GSON.toJson(wallpaperColors)

        if (getWallpaperColorJson() != currentWallpaperColors) {
            BroadcastListener.requiresUpdate = true
            setWallpaperColorJson(currentWallpaperColors)

            if (!customColorEnabled()) {
                setSeedColorValue(wallpaperColors[0])
            }
        }
    }

    suspend fun getWallpaperColors(context: Context): ArrayList<Int> = withContext(Dispatchers.Default) {
        val mergedColors = mutableSetOf<Int>()
        val wallpaperManager = WallpaperManager.getInstance(context)
        val isLiveWallpaper = wallpaperManager.wallpaperInfo != null

        if (isLiveWallpaper) {
            mergedColors.addSystemWallpaperColors(wallpaperManager)
        }

        if (AppUtil.permissionsGranted(context) && (!isLiveWallpaper || mergedColors.isEmpty())) {
            try {
                withContext(Dispatchers.IO) {
                    WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM)
                }?.let { bitmap ->
                    mergedColors.addAll(bitmap.extractColors())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting wallpaper color", e)
            }
        }

        if (mergedColors.isEmpty()) {
            mergedColors.addSystemWallpaperColors(wallpaperManager)
        }

        if (mergedColors.isNotEmpty()) {
            ArrayList(mergedColors)
        } else {
            getWallpaperColorList().takeIf { it.isNotEmpty() } ?: ColorUtil.monetAccentColors
        }
    }

    private fun MutableSet<Int>.addSystemWallpaperColors(wallpaperManager: WallpaperManager) {
        try {
            wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.let { colors ->
                add(colors.primaryColor.toArgb())
                colors.secondaryColor?.let { add(it.toArgb()) }
                colors.tertiaryColor?.let { add(it.toArgb()) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system wallpaper colors", e)
        }
    }

    private fun createMiniBitmap(bitmap: Bitmap): Bitmap {
        val smallestSide = min(bitmap.width.toDouble(), bitmap.height.toDouble()).toInt()
        val scale = min(1.0, (SMALL_SIDE.toFloat() / smallestSide).toDouble()).toFloat()
        return createMiniBitmap(
            bitmap,
            (scale * bitmap.width).toInt(),
            (scale * bitmap.height).toInt()
        )
    }

    private fun createMiniBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return bitmap.scale(width, height, false)
    }

    private fun calculateOptimalSize(width: Int, height: Int): Size {
        val requestedArea = width * height
        var scale = 1.0

        if (requestedArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            scale = sqrt(MAX_WALLPAPER_EXTRACTION_AREA / requestedArea.toDouble())
        }

        var newWidth = (width * scale).toInt()
        var newHeight = (height * scale).toInt()

        if (newWidth == 0) {
            newWidth = 1
        }
        if (newHeight == 0) {
            newHeight = 1
        }

        return Size(newWidth, newHeight)
    }

    private fun Bitmap?.extractColors(): ArrayList<Int> {
        var bitmapTemp = this ?: return ColorUtil.monetAccentColors

        val bitmapArea = bitmapTemp.width * bitmapTemp.height
        if (bitmapArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            val optimalSize = calculateOptimalSize(
                bitmapTemp.width,
                bitmapTemp.height
            )
            bitmapTemp = bitmapTemp.scale(optimalSize.width, optimalSize.height, false)
        }

        val width = bitmapTemp.width
        val height = bitmapTemp.height
        val pixels = IntArray(width * height)

        bitmapTemp.getPixels(pixels, 0, width, 0, 0, width, height)

        val wallpaperColors = ArrayList(
            Score.score(QuantizerCelebi.quantize(pixels, 128), 12)
        )

        return if (wallpaperColors.isEmpty()) ColorUtil.monetAccentColors else wallpaperColors
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable && bitmap != null) {
            return bitmap
        }

        val intrinsicWidth = intrinsicWidth
        val intrinsicHeight = intrinsicHeight

        return if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            val colors = ColorUtil.monetAccentColors
            val colorCount = colors.size

            createBitmap(colorCount, 1).apply {
                val canvas = Canvas(this)
                val rectWidth = canvas.width / colorCount

                colors.forEachIndexed { colorIndex, color ->
                    canvas.withClip(
                        left = colorIndex * rectWidth,
                        top = 0,
                        right = (colorIndex + 1) * rectWidth,
                        bottom = canvas.height
                    ) {
                        drawColor(color)
                    }
                }
            }
        } else {
            createMiniBitmap(
                createBitmap(intrinsicWidth, intrinsicHeight).apply {
                    val canvas = Canvas(this)
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    draw(canvas)
                }
            )
        }
    }

    private object WallpaperLoader {

        private val TAG: String = "WallpaperLoader"

        suspend fun loadWallpaperAsync(
            context: Context,
            which: Int
        ): Bitmap? = withContext(Dispatchers.IO) {
            loadWallpaper(context, which)
        }

        private fun loadWallpaper(context: Context, which: Int): Bitmap? {
            return try {
                val wallpaperManager = WallpaperManager.getInstance(context)

                // Live wallpaper
                wallpaperManager.wallpaperInfo?.let { info ->
                    return info.loadThumbnail(appContext.packageManager).toBitmap()
                }

                // Static wallpaper
                wallpaperManager.getWallpaperFile(which)?.use { file ->
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFileDescriptor(file.fileDescriptor, null, bounds)

                    val smallestSide = min(bounds.outWidth, bounds.outHeight)
                    var sampleSize = 1
                    while (smallestSide / (sampleSize * 2) >= SMALL_SIDE) sampleSize *= 2

                    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeFileDescriptor(file.fileDescriptor, null, options)
                        ?.let { return createMiniBitmap(it) }
                }

                // Built-in wallpaper (fallback)
                wallpaperManager.getBuiltInDrawable(which)?.let { drawable ->
                    return drawable.toBitmap()
                }

                Log.e(TAG, "Error getting wallpaper bitmap: all sources returned null")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting wallpaper bitmap", e)
                null
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory decoding wallpaper bitmap", e)
                null
            }
        }
    }
}

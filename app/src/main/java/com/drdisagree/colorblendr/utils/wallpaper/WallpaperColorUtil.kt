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
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorJson
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setWallpaperColorJson
import com.drdisagree.colorblendr.service.BroadcastListener
import com.drdisagree.colorblendr.utils.app.AppUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.drdisagree.colorblendr.utils.monet.quantize.QuantizerCelebi
import com.drdisagree.colorblendr.utils.monet.score.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.min
import kotlin.math.sqrt

object WallpaperColorUtil {

    private val TAG: String = WallpaperColorUtil::class.java.simpleName
    private const val SMALL_SIDE = 128
    private const val MAX_BITMAP_SIZE = 112
    private const val MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE

    suspend fun updateWallpaperColorList(context: Context) {
        if (AppUtil.permissionsGranted(context)) {
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
    }

    suspend fun getWallpaperColor(context: Context): Int {
        return getWallpaperColors(context)[0]
    }

    suspend fun getWallpaperColors(context: Context): ArrayList<Int> {
        if (!AppUtil.permissionsGranted(context)) {
            return ColorUtil.monetAccentColors
        }

        val mergedColors = mutableSetOf<Int>()

        val wallpaperManager = WallpaperManager.getInstance(context)
        var isLiveWallpaper = false

        wallpaperManager.wallpaperInfo?.let {
            isLiveWallpaper = true
            wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?.let { wallpaperColors ->
                    mergedColors.add(wallpaperColors.primaryColor.toArgb())
                    wallpaperColors.secondaryColor?.let { mergedColors.add(it.toArgb()) }
                    wallpaperColors.tertiaryColor?.let { mergedColors.add(it.toArgb()) }
                }
        }

        return try {
            if (!isLiveWallpaper || mergedColors.isEmpty()) {
                withContext(Dispatchers.IO) {
                    WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM)
                }?.let { bitmap ->
                    mergedColors.addAll(getWallpaperColors(bitmap))
                }
            }

            if (mergedColors.isEmpty()) ColorUtil.monetAccentColors else ArrayList(mergedColors)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wallpaper color", e)
            ColorUtil.monetAccentColors
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
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
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

    private fun getWallpaperColors(bitmap: Bitmap?): ArrayList<Int> {
        var bitmapTemp = bitmap ?: return ColorUtil.monetAccentColors

        val bitmapArea = bitmapTemp.width * bitmapTemp.height
        if (bitmapArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            val optimalSize = calculateOptimalSize(
                bitmapTemp.width,
                bitmapTemp.height
            )
            bitmapTemp = Bitmap.createScaledBitmap(
                bitmapTemp,
                optimalSize.width,
                optimalSize.height,
                false
            )
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

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap: Bitmap

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight

        bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            val colors = ColorUtil.monetAccentColors
            val colorCount = colors.size

            Bitmap.createBitmap(colorCount, 1, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                val rectWidth = canvas.width / colorCount
                colors.forEachIndexed { colorIndex, color ->
                    canvas.save()
                    canvas.clipRect(
                        colorIndex * rectWidth,
                        0,
                        (colorIndex + 1) * rectWidth,
                        canvas.height
                    )
                    canvas.drawColor(color)
                    canvas.restore()
                }
            }
        } else {
            createMiniBitmap(
                Bitmap.createBitmap(
                    intrinsicWidth,
                    intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                ).apply {
                    val canvas = Canvas(this)
                    drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    drawable.draw(canvas)
                }
            )
        }

        return bitmap
    }

    private object WallpaperLoader {

        private val TAG: String = WallpaperLoader::class.java.simpleName
        private val ioDispatcher = Dispatchers.IO

        suspend fun loadWallpaperAsync(
            context: Context,
            which: Int
        ): Bitmap? = withContext(ioDispatcher) {
            loadWallpaper(context, which)
        }

        private fun loadWallpaper(context: Context, which: Int): Bitmap? {
            return try {
                val wallpaperManager = WallpaperManager.getInstance(context)

                if (wallpaperManager.wallpaperInfo != null) {
                    drawableToBitmap(
                        wallpaperManager.wallpaperInfo.loadThumbnail(
                            appContext.packageManager
                        )
                    )
                } else {
                    val wallpaperFile = if (which == WallpaperManager.FLAG_SYSTEM) {
                        wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
                    } else {
                        wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
                    }

                    wallpaperFile?.let {
                        val decodeFileDescriptor = createMiniBitmap(
                            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                        )
                        it.close()
                        decodeFileDescriptor
                    } ?: run {
                        Log.e(TAG, "Error getting wallpaper bitmap: wallpaperFile is null")
                        null
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error getting wallpaper bitmap", e)
                null
            }
        }
    }
}

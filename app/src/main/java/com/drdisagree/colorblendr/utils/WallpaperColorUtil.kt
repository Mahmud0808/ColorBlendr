package com.drdisagree.colorblendr.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.Size
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.utils.monet.quantize.QuantizerCelebi
import com.drdisagree.colorblendr.utils.monet.score.Score
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.min
import kotlin.math.sqrt

object WallpaperColorUtil {
    private val TAG: String = WallpaperColorUtil::class.java.simpleName
    private const val SMALL_SIDE = 128
    private const val MAX_BITMAP_SIZE = 112
    private const val MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE

    fun getAndSaveWallpaperColors(context: Context) {
        if (RPrefs.getInt(Const.MONET_SEED_COLOR, Int.MIN_VALUE) == Int.MIN_VALUE &&
            AppUtil.permissionsGranted(context)
        ) {
            val wallpaperColors = getWallpaperColors(context)
            RPrefs.putString(Const.WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors))
            RPrefs.putInt(
                Const.MONET_SEED_COLOR,
                wallpaperColors[0]
            )
        }
    }

    @ColorInt
    fun getWallpaperColor(context: Context): Int {
        return getWallpaperColors(context)[0]
    }

    fun getWallpaperColors(context: Context): ArrayList<Int> {
        if (!AppUtil.permissionsGranted(context)) {
            return ColorUtil.monetAccentColors
        }

        val wallpaperFuture =
            WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM, null)

        try {
            val wallpaperBitmap = wallpaperFuture.get()
            if (wallpaperBitmap != null) {
                return getWallpaperColors(wallpaperBitmap)
            }
        } catch (e: ExecutionException) {
            Log.e(TAG, "Error getting wallpaper color", e)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error getting wallpaper color", e)
        }

        return ColorUtil.monetAccentColors
    }

    private fun createMiniBitmap(bitmap: Bitmap): Bitmap {
        val smallestSide =
            min(bitmap.width.toDouble(), bitmap.height.toDouble()).toInt()
        val scale = min(
            1.0,
            (SMALL_SIDE.toFloat() / smallestSide).toDouble()
        ).toFloat()
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
            val optimalSize = calculateOptimalSize(bitmapTemp.width, bitmapTemp.height)
            bitmapTemp =
                Bitmap.createScaledBitmap(bitmapTemp, optimalSize.width, optimalSize.height, false)
        }

        val width = bitmapTemp.width
        val height = bitmapTemp.height
        val pixels = IntArray(width * height)

        bitmapTemp.getPixels(pixels, 0, width, 0, 0, width, height)

        val wallpaperColors = ArrayList(
            Score.score(
                QuantizerCelebi.quantize(pixels, 25)
            )
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

        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            val colors = ColorUtil.monetAccentColors
            val colorCount = colors.size

            bitmap = Bitmap.createBitmap(colorCount, 1, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            var colorIndex = 0
            if (colorCount > 0) {
                val rectWidth = canvas.width / colorCount
                for (color in colors) {
                    canvas.save()
                    canvas.clipRect(
                        colorIndex * rectWidth,
                        0,
                        (colorIndex + 1) * rectWidth,
                        canvas.height
                    )
                    canvas.drawColor(color)
                    canvas.restore()
                    colorIndex++
                }
            }
        } else {
            bitmap = createMiniBitmap(
                Bitmap.createBitmap(
                    intrinsicWidth,
                    intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            drawable.draw(canvas)
        }

        return bitmap
    }

    private object WallpaperLoader {
        private val TAG: String = WallpaperLoader::class.java.simpleName
        private val executorService: ExecutorService = Executors.newFixedThreadPool(2)
        private val mainHandler = Handler(Looper.getMainLooper())

        fun loadWallpaperAsync(
            context: Context?,
            which: Int,
            listener: WallpaperLoadListener?
        ): Future<Bitmap?> {
            val callable =
                Callable { loadWallpaper(context, which) }
            val future = executorService.submit(callable)

            if (listener != null) {
                executorService.execute {
                    try {
                        val result = future.get()
                        notifyOnMainThread(listener, result)
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error getting wallpaper bitmap async",
                            e
                        )
                    }
                }
            }

            return future
        }

        fun loadWallpaper(context: Context?, which: Int): Bitmap? {
            try {
                val wallpaperFile: ParcelFileDescriptor?
                val wallpaperManager = WallpaperManager.getInstance(context)

                if (wallpaperManager.wallpaperInfo != null) {
                    return drawableToBitmap(
                        wallpaperManager.wallpaperInfo.loadThumbnail(
                            appContext.packageManager
                        )
                    )
                }

                wallpaperFile = if (which == WallpaperManager.FLAG_SYSTEM) {
                    wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
                } else {
                    wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)
                }

                if (wallpaperFile == null) {
                    Log.e(TAG, "Error getting wallpaper bitmap: wallpaperFile is null")
                    return null
                }

                val decodeFileDescriptor = createMiniBitmap(
                    BitmapFactory.decodeFileDescriptor(
                        wallpaperFile.fileDescriptor
                    )
                )
                wallpaperFile.close()

                return decodeFileDescriptor
            } catch (e: IOException) {
                Log.e(TAG, "Error getting wallpaper bitmap", e)
                return null
            }
        }

        fun notifyOnMainThread(listener: WallpaperLoadListener?, bitmap: Bitmap?) {
            mainHandler.post {
                listener?.onWallpaperLoaded(bitmap)
            }
        }

        interface WallpaperLoadListener {
            fun onWallpaperLoaded(bitmap: Bitmap?)
        }
    }
}

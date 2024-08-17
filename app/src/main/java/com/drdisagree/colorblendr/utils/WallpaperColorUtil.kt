package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Size;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.monet.quantize.QuantizerCelebi;
import com.drdisagree.colorblendr.utils.monet.score.Score;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WallpaperColorUtil {

    private static final String TAG = WallpaperColorUtil.class.getSimpleName();
    private static final int SMALL_SIDE = 128;
    private static final int MAX_BITMAP_SIZE = 112;
    private static final int MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE;

    public static void getAndSaveWallpaperColors(Context context) {
        if (RPrefs.getInt(MONET_SEED_COLOR, Integer.MIN_VALUE) == Integer.MIN_VALUE &&
                AppUtil.permissionsGranted(context)
        ) {
            ArrayList<Integer> wallpaperColors = getWallpaperColors(context);
            RPrefs.putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors));
            RPrefs.putInt(MONET_SEED_COLOR, wallpaperColors.get(0));
        }
    }

    public static @ColorInt int getWallpaperColor(Context context) {
        return getWallpaperColors(context).get(0);
    }

    public static ArrayList<Integer> getWallpaperColors(Context context) {
        if (!AppUtil.permissionsGranted(context)) {
            return ColorUtil.getMonetAccentColors();
        }

        Future<Bitmap> wallpaperFuture = WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM, null);

        try {
            Bitmap wallpaperBitmap = wallpaperFuture.get();
            if (wallpaperBitmap != null) {
                return WallpaperColorUtil.getWallpaperColors(wallpaperBitmap);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting wallpaper color", e);
        }

        return ColorUtil.getMonetAccentColors();
    }

    private static class WallpaperLoader {

        private static final String TAG = WallpaperLoader.class.getSimpleName();
        private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
        private static final Handler mainHandler = new Handler(Looper.getMainLooper());

        public interface WallpaperLoadListener {
            void onWallpaperLoaded(Bitmap bitmap);
        }

        public static Future<Bitmap> loadWallpaperAsync(Context context, int which, WallpaperLoadListener listener) {
            Callable<Bitmap> callable = () -> loadWallpaper(context, which);
            Future<Bitmap> future = executorService.submit(callable);

            if (listener != null) {
                executorService.execute(() -> {
                    try {
                        Bitmap result = future.get();
                        notifyOnMainThread(listener, result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting wallpaper bitmap async", e);
                    }
                });
            }

            return future;
        }

        private static Bitmap loadWallpaper(Context context, int which) {
            try {
                ParcelFileDescriptor wallpaperFile;
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

                if (wallpaperManager.getWallpaperInfo() != null) {
                    return drawableToBitmap(
                            wallpaperManager.getWallpaperInfo().loadThumbnail(
                                    ColorBlendr.getAppContext().getPackageManager()
                            )
                    );
                }

                if (which == WallpaperManager.FLAG_SYSTEM) {
                    wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                } else {
                    wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
                }

                if (wallpaperFile == null) {
                    Log.e(TAG, "Error getting wallpaper bitmap: wallpaperFile is null");
                    return null;
                }

                Bitmap decodeFileDescriptor = createMiniBitmap(
                        BitmapFactory.decodeFileDescriptor(
                                wallpaperFile.getFileDescriptor()
                        )
                );
                wallpaperFile.close();

                return decodeFileDescriptor;
            } catch (IOException e) {
                Log.e(TAG, "Error getting wallpaper bitmap", e);
                return null;
            }
        }

        private static void notifyOnMainThread(WallpaperLoadListener listener, Bitmap bitmap) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onWallpaperLoaded(bitmap);
                }
            });
        }
    }

    private static Bitmap createMiniBitmap(@NonNull Bitmap bitmap) {
        int smallestSide = Math.min(bitmap.getWidth(), bitmap.getHeight());
        float scale = Math.min(1.0f, (float) SMALL_SIDE / smallestSide);
        return createMiniBitmap(bitmap,
                (int) (scale * bitmap.getWidth()),
                (int) (scale * bitmap.getHeight()));
    }

    private static Bitmap createMiniBitmap(@NonNull Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private static Size calculateOptimalSize(int width, int height) {
        final int requestedArea = width * height;
        double scale = 1;

        if (requestedArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            scale = Math.sqrt(MAX_WALLPAPER_EXTRACTION_AREA / (double) requestedArea);
        }

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        if (newWidth == 0) {
            newWidth = 1;
        }
        if (newHeight == 0) {
            newHeight = 1;
        }

        return new Size(newWidth, newHeight);
    }

    private static ArrayList<Integer> getWallpaperColors(Bitmap bitmap) {
        if (bitmap == null) {
            return ColorUtil.getMonetAccentColors();
        }

        int bitmapArea = bitmap.getWidth() * bitmap.getHeight();
        if (bitmapArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            Size optimalSize = calculateOptimalSize(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, optimalSize.getWidth(), optimalSize.getHeight(), false);
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        ArrayList<Integer> wallpaperColors = new ArrayList<>(
                Score.score(
                        QuantizerCelebi.quantize(pixels, 25)
                )
        );

        return wallpaperColors.isEmpty() ? ColorUtil.getMonetAccentColors() : wallpaperColors;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            ArrayList<Integer> colors = ColorUtil.getMonetAccentColors();
            int colorCount = colors.size();

            bitmap = Bitmap.createBitmap(colorCount, 1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            int colorIndex = 0;
            if (colorCount > 0) {
                int rectWidth = canvas.getWidth() / colorCount;
                for (Integer color : colors) {
                    canvas.save();
                    canvas.clipRect(colorIndex * rectWidth, 0, (colorIndex + 1) * rectWidth, canvas.getHeight());
                    canvas.drawColor(color);
                    canvas.restore();
                    colorIndex++;
                }
            }
        } else {
            bitmap = createMiniBitmap(
                    Bitmap.createBitmap(
                            intrinsicWidth,
                            intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                    )
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            drawable.draw(canvas);
        }

        return bitmap;
    }
}

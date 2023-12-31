package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Size;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WallpaperUtil {

    private static final String TAG = WallpaperUtil.class.getSimpleName();
    private static final int SMALL_SIDE = 128;
    private static final int MAX_BITMAP_SIZE = 112;
    private static final int MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE;
    private static final double MINIMUM_DARKNESS = 0.2;
    private static final double MAXIMUM_DARKNESS = 0.8;
    private static final float HUE_THRESHOLD = 10f;

    public static void getAndSaveWallpaperColors(Context context) {
        if (RPrefs.getInt(MONET_SEED_COLOR, -1) == -1 &&
                AppUtil.permissionsGranted(context)
        ) {
            ArrayList<Integer> wallpaperColors = WallpaperUtil.getWallpaperColors(context);
            RPrefs.putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors));
            RPrefs.putInt(MONET_SEED_COLOR, wallpaperColors.get(0));
        }
    }

    public static ArrayList<Integer> getWallpaperColors(Context context) {
        Future<Bitmap> wallpaperFuture = WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM, null);

        try {
            Bitmap wallpaperBitmap = wallpaperFuture.get();
            if (wallpaperBitmap != null) {
                return WallpaperUtil.getDominantColors(wallpaperBitmap);
            } else {
                return new ArrayList<>(Color.BLUE);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting wallpaper color", e);
            return new ArrayList<>(Color.BLUE);
        }
    }

    public static @ColorInt int getWallpaperColor(Context context) {
        Future<Bitmap> wallpaperFuture = WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM, null);

        try {
            Bitmap wallpaperBitmap = wallpaperFuture.get();
            if (wallpaperBitmap != null) {
                return WallpaperUtil.getDominantColor(wallpaperBitmap);
            } else {
                return Color.BLUE;
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting wallpaper color", e);
            return Color.BLUE;
        }
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

                if (which == WallpaperManager.FLAG_SYSTEM) {
                    wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
                } else {
                    wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
                }

                if (wallpaperFile == null) {
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

    public static Bitmap createMiniBitmap(@NonNull Bitmap bitmap) {
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

    private static @ColorInt int getDominantColor(Bitmap bitmap) {
        if (bitmap == null) {
            return Color.BLUE;
        }

        return getDominantColors(bitmap).get(0);
    }

    private static ArrayList<Integer> getDominantColors(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        final int bitmapArea = bitmap.getWidth() * bitmap.getHeight();

        if (bitmapArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            Size optimalSize = calculateOptimalSize(
                    bitmap.getWidth(),
                    bitmap.getHeight()
            );
            bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    optimalSize.getWidth(),
                    optimalSize.getHeight(),
                    false /* filter */
            );
        }

        List<Palette.Swatch> swatchesTemp = Palette
                .from(bitmap)
                .maximumColorCount(25)
                .resizeBitmapArea(MAX_WALLPAPER_EXTRACTION_AREA)
                .generate()
                .getSwatches();

        ArrayList<Palette.Swatch> swatches = new ArrayList<>(swatchesTemp);
        swatches.sort((swatch1, swatch2) -> swatch2.getPopulation() - swatch1.getPopulation());
        List<Palette.Swatch> filteredSwatches = filterColors(swatches);

        ArrayList<Integer> wallpaperColors = new ArrayList<>();
        for (Palette.Swatch swatch : filteredSwatches) {
            wallpaperColors.add(swatch.getRgb());
        }

        ArrayList<Integer> colorsEmpty = new ArrayList<>();
        colorsEmpty.add(Color.BLUE);

        return wallpaperColors.size() > 0 ? wallpaperColors : colorsEmpty;
    }

    private static List<Palette.Swatch> filterColors(List<Palette.Swatch> swatches) {
        List<Palette.Swatch> filteredSwatches = new ArrayList<>();
        Set<Float> addedHues = new HashSet<>();

        for (Palette.Swatch swatch : swatches) {
            int color = swatch.getRgb();
            if (isColorInRange(color) && !hasSimilarHue(addedHues, color)) {
                filteredSwatches.add(swatch);
                addedHues.add(ColorUtil.getHue(color));
            }
        }

        return filteredSwatches;
    }

    private static boolean hasSimilarHue(Set<Float> addedHues, int color) {
        float hue = ColorUtil.getHue(color);

        for (float addedHue : addedHues) {
            if (Math.abs(hue - addedHue) < HUE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }

    private static boolean isColorInRange(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= MINIMUM_DARKNESS && darkness <= MAXIMUM_DARKNESS;
    }
}

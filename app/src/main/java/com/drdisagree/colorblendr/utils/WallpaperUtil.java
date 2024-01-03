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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WallpaperUtil {

    private static final String TAG = WallpaperUtil.class.getSimpleName();
    private static final int SMALL_SIDE = 128;
    private static final int MAX_BITMAP_SIZE = 112;
    private static final int MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE;
    private static final double MINIMUM_DARKNESS = 0.1;
    private static final double MAXIMUM_DARKNESS = 0.8;
    private static final float HUE_THRESHOLD = 25f;

    public static void getAndSaveWallpaperColors(Context context) {
        if (RPrefs.getInt(MONET_SEED_COLOR, Integer.MIN_VALUE) == Integer.MIN_VALUE &&
                AppUtil.permissionsGranted(context)
        ) {
            ArrayList<Integer> wallpaperColors = WallpaperUtil.getWallpaperColors(context);
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
                return WallpaperUtil.getDominantColors(wallpaperBitmap);
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

    private static ArrayList<Integer> getDominantColors(Bitmap bitmap) {
        if (bitmap == null) {
            return new ArrayList<>(Collections.singletonList(Color.BLUE));
        }

        int bitmapArea = bitmap.getWidth() * bitmap.getHeight();
        if (bitmapArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            Size optimalSize = calculateOptimalSize(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, optimalSize.getWidth(), optimalSize.getHeight(), false);
        }

        Palette palette = createPalette(bitmap);
        List<Palette.Swatch> sortedSwatches = sortSwatches(palette.getSwatches());

        List<Palette.Swatch> uniqueSwatches = getUniqueSwatches(palette);
        sortedSwatches.addAll(0, uniqueSwatches);

        List<Palette.Swatch> filteredSwatches = filterColors(sortedSwatches);

        ArrayList<Integer> wallpaperColors = filteredSwatches.stream().map(Palette.Swatch::getRgb).collect(Collectors.toCollection(ArrayList::new));

        return wallpaperColors.isEmpty() ? new ArrayList<>(Collections.singletonList(Color.BLUE)) : wallpaperColors;
    }

    private static Palette createPalette(Bitmap bitmap) {
        return Palette.from(bitmap)
                .maximumColorCount(25)
                .resizeBitmapArea(MAX_WALLPAPER_EXTRACTION_AREA)
                .generate();
    }

    private static List<Palette.Swatch> sortSwatches(List<Palette.Swatch> swatches) {
        List<Palette.Swatch> sortedSwatches = new ArrayList<>(swatches);
        sortedSwatches.sort(Comparator.comparingInt(Palette.Swatch::getPopulation).reversed());
        return sortedSwatches;
    }

    private static List<Palette.Swatch> getUniqueSwatches(Palette palette) {
        return Stream.of(
                palette.getVibrantSwatch(),
                palette.getDarkVibrantSwatch(),
                palette.getLightVibrantSwatch(),
                palette.getMutedSwatch(),
                palette.getDarkMutedSwatch(),
                palette.getLightMutedSwatch()
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static List<Palette.Swatch> filterColors(List<Palette.Swatch> swatches) {
        Set<Float> addedHues = new HashSet<>();
        return swatches.stream()
                .filter(swatch -> isColorInRange(swatch.getRgb()) && !hasSimilarHue(addedHues, swatch.getRgb()))
                .peek(swatch -> addedHues.add(ColorUtil.getHue(swatch.getRgb())))
                .collect(Collectors.toList());
    }

    private static boolean hasSimilarHue(Set<Float> addedHues, int color) {
        float hue = ColorUtil.getHue(color);
        return addedHues.stream().anyMatch(addedHue -> Math.abs(hue - addedHue) < HUE_THRESHOLD);
    }

    private static boolean isColorInRange(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= MINIMUM_DARKNESS && darkness <= MAXIMUM_DARKNESS;
    }
}

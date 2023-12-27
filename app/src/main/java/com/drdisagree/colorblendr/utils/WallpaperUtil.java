package com.drdisagree.colorblendr.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.ColorInt;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WallpaperUtil {

    private static final String TAG = WallpaperUtil.class.getSimpleName();

    public static @ColorInt int getWallpaperColor(Context context) {
        Future<Bitmap> wallpaperFuture = WallpaperLoader.loadWallpaperAsync(context, WallpaperManager.FLAG_SYSTEM, null);

        try {
            Bitmap wallpaperBitmap = wallpaperFuture.get();
            if (wallpaperBitmap != null) {
                return ColorUtil.getDominantColor(wallpaperBitmap);
            } else {
                return Color.BLUE;
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting wallpaper color", e);
            return Color.BLUE;
        }
    }

    public static class WallpaperLoader {

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

                Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(wallpaperFile.getFileDescriptor());
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
}

package com.drdisagree.colorblendr.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.List;

public class WallpaperUtil {

    public static @ColorInt int getWallpaperColor(Context context) {
        Bitmap bitmap = getBitmapFromWallpaperManager(context, WallpaperManager.FLAG_SYSTEM);
        if (bitmap == null) {
            return Color.BLUE;
        }

        List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
        List<Palette.Swatch> swatches = new ArrayList<>(swatchesTemp);
        swatches.sort((swatch1, swatch2) -> swatch2.getPopulation() - swatch1.getPopulation());
        return swatches.size() > 0 ? swatches.get(0).getRgb() : Color.BLUE;
    }

    public static Bitmap getBitmapFromWallpaperManager(Context context, int which) {
        ParcelFileDescriptor wallpaperFile;
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            try {
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

                return new BitmapDrawable(context.getResources(), decodeFileDescriptor).getBitmap();
            } catch (Exception e) {
                Log.e("WallpaperUtil", "Error getting wallpaper bitmap", e);
                return null;
            }
        } catch (Exception exception) {
            Log.e("WallpaperUtil", "Error getting wallpaper instance", exception);
            return null;
        }
    }
}

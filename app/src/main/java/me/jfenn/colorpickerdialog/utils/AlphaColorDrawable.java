package me.jfenn.colorpickerdialog.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.jfenn.androidutils.DimenUtilsKt;

/**
 * The AlphaColorDrawable draws a color with a tiled
 * white/gray background representing alpha or transparency.
 * Aside from the tiled background, it functions the same
 * as any ColorDrawable.
 */
public class AlphaColorDrawable extends Drawable {

    public static Bitmap tile;
    private final Paint bitmapPaint;
    private final Paint paint;

    public AlphaColorDrawable(@ColorInt int color) {
        int size = DimenUtilsKt.dpToPx(8);
        bitmapPaint = new Paint();
        bitmapPaint.setColor(Color.LTGRAY);

        if (tile == null || tile.isRecycled()) {
            tile = Bitmap.createBitmap(size * 4, size * 4, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(tile);
            canvas.drawColor(Color.WHITE);
            for (int x = 0; x < canvas.getWidth(); x += size) {
                for (int y = x % (size * 2) == 0 ? 0 : size; y < canvas.getWidth(); y += size * 2) {
                    canvas.drawRect(x, y, x + size, y + size, bitmapPaint);
                }
            }
        }

        paint = new Paint();
        paint.setColor(color);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();

        if (paint.getAlpha() < 255) {
            for (int x = b.left; x < b.right; x += tile.getWidth()) {
                for (int y = b.top; y < b.bottom; y += tile.getHeight()) {
                    canvas.drawBitmap(tile, x, y, bitmapPaint);
                }
            }
        }

        canvas.drawRect(b.left, b.top, b.right, b.bottom, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        int color = bitmapPaint.getColor();
        bitmapPaint.setColor(Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        ));
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}

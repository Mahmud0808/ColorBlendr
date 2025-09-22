package me.jfenn.colorpickerdialog.utils;

import android.R;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.appcompat.widget.AppCompatSeekBar;

import me.jfenn.androidutils.seekbar.SeekBarBackgroundDrawable;
import me.jfenn.androidutils.seekbar.SeekBarDrawable;

public class MiscUtil {

    public static void setProgressBarDrawable(AppCompatSeekBar seekBar, Drawable drawable, int handleColor) {
        // Create the background layer with semi-transparency
        SeekBarBackgroundDrawable background = new SeekBarBackgroundDrawable(drawable.mutate().getConstantState().newDrawable());
        background.setAlpha(255);

        // Create the progress layer
        SeekBarDrawable progressLayer = new SeekBarDrawable(drawable);

        // Combine into a LayerDrawable
        LayerDrawable layers = new LayerDrawable(new Drawable[]{progressLayer, background});
        layers.setId(0, R.id.progress);
        layers.setId(1, R.id.background);

        // Set as SeekBar's progress drawable
        seekBar.setProgressDrawable(layers);

        // Apply color filter to thumb
        if (seekBar.getThumb() != null) {
            seekBar.getThumb().setColorFilter(handleColor, PorterDuff.Mode.SRC_IN);
        }
    }
}

package com.drdisagree.colorblendr.ui.repositories;

import static com.drdisagree.colorblendr.common.Const.GSON;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.Context;
import android.graphics.Color;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.WallpaperColorUtil;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ColorRepository {

    private final Context context;

    public ColorRepository(Context context) {
        this.context = context;
    }

    public int getMonetSeedColor() {
        return RPrefs.getInt(MONET_SEED_COLOR, WallpaperColorUtil.getWallpaperColor(context));
    }

    public void setMonetSeedColor(int color) {
        RPrefs.putInt(MONET_SEED_COLOR, color);
    }

    public boolean isMonetSeedColorEnabled() {
        return RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false);
    }

    public void setMonetSeedColorEnabled(boolean enabled) {
        RPrefs.putBoolean(MONET_SEED_COLOR_ENABLED, enabled);
    }

    public void setMonetLastUpdated() {
        RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
    }

    public List<Integer> getWallpaperColorList() {
        String wallpaperColors = RPrefs.getString(WALLPAPER_COLOR_LIST, null);
        if (wallpaperColors != null) {
            return GSON.fromJson(wallpaperColors, new TypeToken<List<Integer>>() {
            }.getType());
        } else {
            return ColorUtil.getMonetAccentColors();
        }
    }

    public List<Integer> getBasicColors() {
        String[] basicColors = context.getResources().getStringArray(R.array.basic_color_codes);
        return Arrays.stream(basicColors).map(Color::parseColor).collect(Collectors.toList());
    }
}

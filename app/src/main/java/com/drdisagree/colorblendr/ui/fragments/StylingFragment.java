package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentStylingBinding;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Objects;

public class StylingFragment extends Fragment {

    private FragmentStylingBinding binding;
    private LinearLayout[] colorTableRows;
    private static final int[] colorCodes = {
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
    };
    private static String selectedStyle;
    private static boolean accurateShades = RPrefs.getBoolean(MONET_ACCURATE_SHADES, true);
    int[] monetAccentSaturation = new int[]{RPrefs.getInt(MONET_ACCENT_SATURATION, 100)};
    int[] monetBackgroundSaturation = new int[]{RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    int[] monetBackgroundLightness = new int[]{RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStylingBinding.inflate(inflater, container, false);

        colorTableRows = new LinearLayout[]{
                binding.colorPreview.systemAccent1,
                binding.colorPreview.systemAccent2,
                binding.colorPreview.systemAccent3,
                binding.colorPreview.systemNeutral1,
                binding.colorPreview.systemNeutral2
        };

        selectedStyle = RPrefs.getString(
                MONET_STYLE,
                ColorBlendr
                        .getAppContext()
                        .getResources()
                        .getString(R.string.monet_tonalspot)
        );

        assignStockColorsToPalette();

        // Monet primary accent saturation
        binding.accentSaturation.setSliderValue(RPrefs.getInt(MONET_ACCENT_SATURATION, 100));

        binding.accentSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetAccentSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.accentSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetAccentSaturation[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_ACCENT_SATURATION, monetAccentSaturation[0]);
            }
        });

        // Long Click Reset
        binding.accentSaturation.setResetClickListener(v -> {
            monetAccentSaturation[0] = 100;
            assignCustomColorsToPalette();
            RPrefs.clearPref(MONET_ACCENT_SATURATION);
            return true;
        });

        // Monet background saturation
        binding.backgroundSaturation.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100));

        binding.backgroundSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.backgroundSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundSaturation[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0]);
            }
        });

        // Reset button
        binding.backgroundSaturation.setResetClickListener(v -> {
            monetBackgroundSaturation[0] = 100;
            assignCustomColorsToPalette();
            RPrefs.clearPref(MONET_BACKGROUND_SATURATION);
            return true;
        });

        // Monet background lightness
        binding.backgroundLightness.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));

        binding.backgroundLightness.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundLightness[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.backgroundLightness.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundLightness[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0]);
            }
        });

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener(v -> {
            monetBackgroundLightness[0] = 100;
            assignCustomColorsToPalette();
            RPrefs.clearPref(MONET_BACKGROUND_LIGHTNESS);
            return true;
        });

        return binding.getRoot();
    }

    private void assignStockColorsToPalette() {
        int[][] systemColors = ColorUtil.getSystemColors(requireContext());

        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                colorTableRows[i].getChildAt(j).getBackground().setTint(systemColors[i][j]);
                colorTableRows[i].getChildAt(j).setTag(systemColors[i][j]);

                TextView textView = new TextView(requireContext());
                textView.setText(String.valueOf(colorCodes[j]));
                textView.setRotation(270);
                textView.setTextColor(ColorUtil.calculateTextColor(systemColors[i][j]));
                textView.setTextSize(10);
                textView.setAlpha(0.8f);

                ((ViewGroup) colorTableRows[i].getChildAt(j)).addView(textView);
                ((LinearLayout) colorTableRows[i].getChildAt(j)).setGravity(Gravity.CENTER);
            }
        }
    }

    private void assignCustomColorsToPalette() {
        ArrayList<ArrayList<Integer>> palette = convertIntArrayToList(ColorUtil.getSystemColors(requireContext()));

        if (!Objects.equals(selectedStyle, ColorBlendr.getAppContext().getResources().getString(R.string.monet_monochrome))) {
            // Set accent saturation
            palette.get(0).replaceAll(o -> ColorUtil.modifySaturation(o, monetAccentSaturation[0]));
            palette.get(1).replaceAll(o -> ColorUtil.modifySaturation(o, monetAccentSaturation[0]));
            palette.get(2).replaceAll(o -> ColorUtil.modifySaturation(o, monetAccentSaturation[0]));

            // Set background saturation
            palette.get(3).replaceAll(o -> ColorUtil.modifySaturation(o, monetBackgroundSaturation[0]));
            palette.get(4).replaceAll(o -> ColorUtil.modifySaturation(o, monetBackgroundSaturation[0]));
        }

        // Set background lightness
        for (int i = Objects.equals(selectedStyle, ColorBlendr.getAppContext().getResources().getString(R.string.monet_monochrome)) ? 0 : 3; i < palette.size(); i++) {
            for (int j = 0; j < palette.get(i).size(); j++) {
                palette.get(i).set(j, ColorUtil.modifyLightness(palette.get(i).get(j), monetBackgroundLightness[0], j));
            }
        }

        // Update preview colors
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                colorTableRows[i].getChildAt(j).getBackground().setTint(palette.get(i).get(j));
                colorTableRows[i].getChildAt(j).setTag(palette.get(i).get(j));
            }
        }
    }

    private static ArrayList<ArrayList<Integer>> convertIntArrayToList(int[][] array) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();

        for (int[] row : array) {
            ArrayList<Integer> rowList = new ArrayList<>();
            for (int value : row) {
                rowList.add(value);
            }

            result.add(rowList);
        }

        return result;
    }
}
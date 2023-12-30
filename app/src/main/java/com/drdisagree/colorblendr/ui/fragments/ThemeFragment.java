package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.google.android.material.slider.Slider;

public class ThemeFragment extends Fragment {

    private FragmentThemeBinding binding;
    private final int[] monetAccentSaturation = new int[]{RPrefs.getInt(MONET_ACCENT_SATURATION, 100)};
    private final int[] monetBackgroundSaturation = new int[]{RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    private final int[] monetBackgroundLightness = new int[]{RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentThemeBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.theme, true, binding.header.toolbar);

        // Monet primary accent saturation
        binding.accentSaturation.setSliderValue(RPrefs.getInt(MONET_ACCENT_SATURATION, 100));

        binding.accentSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetAccentSaturation[0] = (int) value;
        });

        binding.accentSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetAccentSaturation[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_ACCENT_SATURATION, monetAccentSaturation[0]);
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 200);
            }
        });

        // Long Click Reset
        binding.accentSaturation.setResetClickListener(v -> {
            monetAccentSaturation[0] = 100;
            RPrefs.clearPref(MONET_ACCENT_SATURATION);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 200);
            return true;
        });

        // Monet background saturation
        binding.backgroundSaturation.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100));

        binding.backgroundSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundSaturation[0] = (int) value;
        });

        binding.backgroundSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundSaturation[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0]);
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 200);
            }
        });

        // Reset button
        binding.backgroundSaturation.setResetClickListener(v -> {
            monetBackgroundSaturation[0] = 100;
            RPrefs.clearPref(MONET_BACKGROUND_SATURATION);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 200);
            return true;
        });

        // Monet background lightness
        binding.backgroundLightness.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));

        binding.backgroundLightness.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundLightness[0] = (int) value;
        });

        binding.backgroundLightness.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundLightness[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0]);
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 200);
            }
        });

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener(v -> {
            monetBackgroundLightness[0] = 100;
            RPrefs.clearPref(MONET_BACKGROUND_LIGHTNESS);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 200);
            return true;
        });

        return binding.getRoot();
    }
}
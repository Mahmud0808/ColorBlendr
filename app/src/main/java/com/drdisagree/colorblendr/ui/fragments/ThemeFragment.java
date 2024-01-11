package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding;
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.SystemUtil;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class ThemeFragment extends Fragment {

    private static final String TAG = ThemeFragment.class.getSimpleName();
    private FragmentThemeBinding binding;
    private final int[] monetAccentSaturation = new int[]{RPrefs.getInt(MONET_ACCENT_SATURATION, 100)};
    private final int[] monetBackgroundSaturation = new int[]{RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    private final int[] monetBackgroundLightness = new int[]{RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentThemeBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.theme, true, binding.header.toolbar);

        // Color preview titles
        binding.colorAccent1.title.setText(R.string.primary);
        binding.colorAccent2.title.setText(R.string.secondary);
        binding.colorAccent3.title.setText(R.string.tertiary);
        binding.colorNeutral1.title.setText(R.string.neutral_1);
        binding.colorNeutral2.title.setText(R.string.neutral_2);

        // Monet primary accent saturation
        binding.accentSaturation.setSliderValue(RPrefs.getInt(MONET_ACCENT_SATURATION, 100));

        binding.accentSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetAccentSaturation[0] = (int) value;
            updatePreviewColors();
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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        OverlayManager.applyFabricatedColors(requireContext());
                    } catch (Exception ignored) {
                    }
                }, 200);
            }
        });

        // Long Click Reset
        binding.accentSaturation.setResetClickListener(v -> {
            monetAccentSaturation[0] = 100;
            updatePreviewColors();
            RPrefs.clearPref(MONET_ACCENT_SATURATION);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    OverlayManager.applyFabricatedColors(requireContext());
                } catch (Exception ignored) {
                }
            }, 200);
            return true;
        });

        // Monet background saturation
        binding.backgroundSaturation.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100));

        binding.backgroundSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundSaturation[0] = (int) value;
            updatePreviewColors();
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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        OverlayManager.applyFabricatedColors(requireContext());
                    } catch (Exception ignored) {
                    }
                }, 200);
            }
        });

        // Reset button
        binding.backgroundSaturation.setResetClickListener(v -> {
            monetBackgroundSaturation[0] = 100;
            updatePreviewColors();
            RPrefs.clearPref(MONET_BACKGROUND_SATURATION);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    OverlayManager.applyFabricatedColors(requireContext());
                } catch (Exception ignored) {
                }
            }, 200);
            return true;
        });

        // Monet background lightness
        binding.backgroundLightness.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));

        binding.backgroundLightness.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundLightness[0] = (int) value;
            updatePreviewColors();
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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        OverlayManager.applyFabricatedColors(requireContext());
                    } catch (Exception ignored) {
                    }
                }, 200);
            }
        });

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener(v -> {
            monetBackgroundLightness[0] = 100;
            updatePreviewColors();
            RPrefs.clearPref(MONET_BACKGROUND_LIGHTNESS);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    OverlayManager.applyFabricatedColors(requireContext());
                } catch (Exception ignored) {
                }
            }, 200);
            return true;
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updatePreviewColors();
    }

    private void updatePreviewColors() {
        ArrayList<ArrayList<Integer>> colors = generateModifiedColors(
                monetAccentSaturation[0],
                monetBackgroundSaturation[0],
                monetBackgroundLightness[0],
                RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                RPrefs.getBoolean(MONET_ACCURATE_SHADES, true)
        );

        if (colors != null) {
            boolean isDark = SystemUtil.isDarkMode();

            binding.colorAccent1.colorPreviewBg.getBackground().setTint(colors.get(0).get(isDark ? 4 : 8));
            binding.colorAccent1.colorPreviewFg.getBackground().setTint(colors.get(0).get(isDark ? 4 : 8));
            binding.colorAccent2.colorPreviewBg.getBackground().setTint(colors.get(1).get(isDark ? 4 : 8));
            binding.colorAccent2.colorPreviewFg.getBackground().setTint(colors.get(1).get(isDark ? 4 : 8));
            binding.colorAccent3.colorPreviewBg.getBackground().setTint(colors.get(2).get(isDark ? 4 : 8));
            binding.colorAccent3.colorPreviewFg.getBackground().setTint(colors.get(2).get(isDark ? 4 : 8));
            binding.colorNeutral1.colorPreviewBg.getBackground().setTint(colors.get(3).get(isDark ? 9 : 3));
            binding.colorNeutral1.colorPreviewFg.getBackground().setTint(colors.get(3).get(isDark ? 9 : 3));
            binding.colorNeutral2.colorPreviewBg.getBackground().setTint(colors.get(4).get(isDark ? 9 : 3));
            binding.colorNeutral2.colorPreviewFg.getBackground().setTint(colors.get(4).get(isDark ? 9 : 3));
        }
    }

    private ArrayList<ArrayList<Integer>> generateModifiedColors(
            int monetAccentSaturation,
            int monetBackgroundSaturation,
            int monetBackgroundLightness,
            boolean pitchBlackTheme,
            boolean accurateShades
    ) {
        try {
            return ColorUtil.generateModifiedColors(
                    requireContext(),
                    ColorSchemeUtil.stringToEnumMonetStyle(
                            requireContext(),
                            RPrefs.getString(MONET_STYLE, getString(R.string.monet_tonalspot))
                    ),
                    monetAccentSaturation,
                    monetBackgroundSaturation,
                    monetBackgroundLightness,
                    pitchBlackTheme,
                    accurateShades
            );
        } catch (Exception e) {
            Log.e(TAG, "Error generating modified colors", e);
            return null;
        }
    }
}
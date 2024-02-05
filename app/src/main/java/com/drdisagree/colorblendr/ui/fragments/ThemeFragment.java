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
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding;
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.SystemUtil;

import java.util.ArrayList;

public class ThemeFragment extends Fragment {

    private static final String TAG = ThemeFragment.class.getSimpleName();
    private FragmentThemeBinding binding;
    private final int[] monetAccentSaturation = new int[]{RPrefs.getInt(MONET_ACCENT_SATURATION, 100)};
    private final int[] monetBackgroundSaturation = new int[]{RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    private final int[] monetBackgroundLightness = new int[]{RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};
    private final boolean notShizukuMode = Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;

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
        binding.accentSaturation.setSeekbarProgress(RPrefs.getInt(MONET_ACCENT_SATURATION, 100));
        binding.accentSaturation.setOnSeekbarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.accentSaturation.setSelectedProgress();
                monetAccentSaturation[0] = progress;
                updatePreviewColors();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                monetAccentSaturation[0] = seekBar.getProgress();
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
        binding.accentSaturation.setEnabled(notShizukuMode);

        // Monet background saturation
        binding.backgroundSaturation.setSeekbarProgress(RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100));
        binding.backgroundSaturation.setOnSeekbarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.backgroundSaturation.setSelectedProgress();
                monetBackgroundSaturation[0] = progress;
                updatePreviewColors();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                monetBackgroundSaturation[0] = seekBar.getProgress();
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
        binding.backgroundSaturation.setEnabled(notShizukuMode);

        // Monet background lightness
        binding.backgroundLightness.setSeekbarProgress(RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));
        binding.backgroundLightness.setOnSeekbarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
                binding.backgroundLightness.setSelectedProgress();
                monetBackgroundLightness[0] = progress;
                updatePreviewColors();
            }

            @Override
            public void onStartTrackingTouch(@NonNull SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                monetBackgroundLightness[0] = seekBar.getProgress();
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
        binding.backgroundLightness.setEnabled(notShizukuMode);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updatePreviewColors();
    }

    private void updatePreviewColors() {
        ArrayList<ArrayList<Integer>> colorPalette = generateModifiedColors(
                monetAccentSaturation[0],
                monetBackgroundSaturation[0],
                monetBackgroundLightness[0],
                RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                RPrefs.getBoolean(MONET_ACCURATE_SHADES, true)
        );

        if (colorPalette != null) {
            boolean isDarkMode = SystemUtil.isDarkMode();

            binding.colorAccent1.colorContainer.setHalfCircleColor(colorPalette.get(0).get(4));
            binding.colorAccent1.colorContainer.setFirstQuarterCircleColor(colorPalette.get(0).get(5));
            binding.colorAccent1.colorContainer.setSecondQuarterCircleColor(colorPalette.get(0).get(6));
            binding.colorAccent1.colorContainer.setSquareColor(colorPalette.get(0).get(!isDarkMode ? 3 : 9));
            binding.colorAccent1.colorContainer.setPadding(8);
            binding.colorAccent1.colorContainer.invalidateColors();

            binding.colorAccent2.colorContainer.setHalfCircleColor(colorPalette.get(1).get(4));
            binding.colorAccent2.colorContainer.setFirstQuarterCircleColor(colorPalette.get(1).get(5));
            binding.colorAccent2.colorContainer.setSecondQuarterCircleColor(colorPalette.get(1).get(6));
            binding.colorAccent2.colorContainer.setSquareColor(colorPalette.get(1).get(!isDarkMode ? 3 : 9));
            binding.colorAccent2.colorContainer.setPadding(8);
            binding.colorAccent2.colorContainer.invalidateColors();

            binding.colorAccent3.colorContainer.setHalfCircleColor(colorPalette.get(2).get(4));
            binding.colorAccent3.colorContainer.setFirstQuarterCircleColor(colorPalette.get(2).get(5));
            binding.colorAccent3.colorContainer.setSecondQuarterCircleColor(colorPalette.get(2).get(6));
            binding.colorAccent3.colorContainer.setSquareColor(colorPalette.get(2).get(!isDarkMode ? 3 : 9));
            binding.colorAccent3.colorContainer.setPadding(8);
            binding.colorAccent3.colorContainer.invalidateColors();

            binding.colorNeutral1.colorContainer.setHalfCircleColor(colorPalette.get(3).get(4));
            binding.colorNeutral1.colorContainer.setFirstQuarterCircleColor(colorPalette.get(3).get(5));
            binding.colorNeutral1.colorContainer.setSecondQuarterCircleColor(colorPalette.get(3).get(6));
            binding.colorNeutral1.colorContainer.setSquareColor(colorPalette.get(3).get(!isDarkMode ? 3 : 9));
            binding.colorNeutral1.colorContainer.setPadding(8);
            binding.colorNeutral1.colorContainer.invalidateColors();

            binding.colorNeutral2.colorContainer.setHalfCircleColor(colorPalette.get(4).get(4));
            binding.colorNeutral2.colorContainer.setFirstQuarterCircleColor(colorPalette.get(4).get(5));
            binding.colorNeutral2.colorContainer.setSecondQuarterCircleColor(colorPalette.get(4).get(6));
            binding.colorNeutral2.colorContainer.setSquareColor(colorPalette.get(4).get(!isDarkMode ? 3 : 9));
            binding.colorNeutral2.colorContainer.setPadding(8);
            binding.colorNeutral2.colorContainer.invalidateColors();
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
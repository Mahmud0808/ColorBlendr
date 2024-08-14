package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding;
import com.drdisagree.colorblendr.ui.viewmodels.ThemeViewModel;
import com.drdisagree.colorblendr.ui.views.ColorPreview;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.SystemUtil;

import java.util.ArrayList;

public class ThemeFragment extends Fragment {

    private FragmentThemeBinding binding;
    private ThemeViewModel themeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        themeViewModel = new ViewModelProvider(requireActivity()).get(ThemeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentThemeBinding.inflate(inflater, container, false);

        setupViews();
        observeViewModel();

        return binding.getRoot();
    }

    private void setupViews() {
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
                themeViewModel.updateAccentSaturation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                themeViewModel.setAccentSaturation(seekBar.getProgress());
            }
        });

        // Long Click Reset
        binding.accentSaturation.setResetClickListener(v -> {
            themeViewModel.resetAccentSaturation();
            return true;
        });
        binding.accentSaturation.setEnabled(themeViewModel.isNotShizukuMode());

        // Monet background saturation
        binding.backgroundSaturation.setSeekbarProgress(RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100));
        binding.backgroundSaturation.setOnSeekbarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.backgroundSaturation.setSelectedProgress();
                themeViewModel.updateBackgroundSaturation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                themeViewModel.setBackgroundSaturation(seekBar.getProgress());
            }
        });

        // Reset button
        binding.backgroundSaturation.setResetClickListener(v -> {
            themeViewModel.resetBackgroundSaturation();
            return true;
        });
        binding.backgroundSaturation.setEnabled(themeViewModel.isNotShizukuMode());

        // Monet background lightness
        binding.backgroundLightness.setSeekbarProgress(RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));
        binding.backgroundLightness.setOnSeekbarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
                binding.backgroundLightness.setSelectedProgress();
                themeViewModel.updateBackgroundLightness(progress);
            }

            @Override
            public void onStartTrackingTouch(@NonNull SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                themeViewModel.setBackgroundLightness(seekBar.getProgress());
            }
        });

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener(v -> {
            themeViewModel.resetBackgroundLightness();
            return true;
        });
        binding.backgroundLightness.setEnabled(themeViewModel.isNotShizukuMode());
    }

    private void observeViewModel() {
        themeViewModel.getColorPaletteLiveData().observe(getViewLifecycleOwner(), this::updatePreviewColors);
    }

    private void updatePreviewColors(ArrayList<ArrayList<Integer>> colorPalette) {
        if (colorPalette == null) return;

        boolean isDarkMode = SystemUtil.isDarkMode();

        for (int i = 0; i < 5; i++) {
            ColorPreview colorContainer = switch (i) {
                case 0 -> binding.colorAccent1.colorContainer;
                case 1 -> binding.colorAccent2.colorContainer;
                case 2 -> binding.colorAccent3.colorContainer;
                case 3 -> binding.colorNeutral1.colorContainer;
                case 4 -> binding.colorNeutral2.colorContainer;
                default -> null;
            };

            colorContainer.setHalfCircleColor(colorPalette.get(i).get(4));
            colorContainer.setFirstQuarterCircleColor(colorPalette.get(i).get(5));
            colorContainer.setSecondQuarterCircleColor(colorPalette.get(i).get(6));
            colorContainer.setSquareColor(colorPalette.get(i).get(isDarkMode ? 9 : 3));
            colorContainer.setPadding(8);
            colorContainer.invalidateColors();
        }
    }
}
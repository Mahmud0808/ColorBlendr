package com.drdisagree.colorblendr.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.databinding.FragmentColorsBinding;
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel;
import com.drdisagree.colorblendr.ui.views.WallColorPreview;
import com.drdisagree.colorblendr.utils.MiscUtil;

import java.util.List;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView;

@SuppressWarnings("deprecation")
public class ColorsFragment extends Fragment {

    private FragmentColorsBinding binding;
    private ColorsViewModel colorsViewModel;
    private int monetSeedColor;
    private final boolean notShizukuMode = Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;

    private final BroadcastReceiver wallpaperChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (binding.colorsToggleGroup.getCheckedButtonId() == R.id.wallpaper_colors_button) {
                colorsViewModel.loadWallpaperColors();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        colorsViewModel = new ViewModelProvider(requireActivity()).get(ColorsViewModel.class);

        if (!notShizukuMode) {
            SettingsFragment.clearCustomColors();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColorsBinding.inflate(inflater, container, false);

        setupViews();
        observeViewModel();

        return binding.getRoot();
    }

    private void setupViews() {
        MiscUtil.setToolbarTitle(requireContext(), R.string.app_name, false, binding.header.toolbar);

        // Color preview container
        binding.colorsToggleGroup.check(
                colorsViewModel.isMonetSeedColorEnabled() ?
                        R.id.basic_colors_button :
                        R.id.wallpaper_colors_button
        );
        binding.colorsToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.wallpaper_colors_button) {
                    binding.wallpaperColorsContainer.setVisibility(View.VISIBLE);
                    binding.basicColorsContainer.setVisibility(View.GONE);
                } else {
                    binding.wallpaperColorsContainer.setVisibility(View.GONE);
                    binding.basicColorsContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        if (binding.colorsToggleGroup.getCheckedButtonId() == R.id.wallpaper_colors_button) {
            binding.wallpaperColorsContainer.setVisibility(View.VISIBLE);
            binding.basicColorsContainer.setVisibility(View.GONE);
        } else {
            binding.wallpaperColorsContainer.setVisibility(View.GONE);
            binding.basicColorsContainer.setVisibility(View.VISIBLE);
        }

        // Primary color picker
        binding.seedColorPicker.setOnClickListener(v -> new ColorPickerDialog()
                .withCornerRadius(10)
                .withColor(monetSeedColor)
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView.class)
                .withListener((pickerView, color) -> {
                    if (monetSeedColor != color) {
                        monetSeedColor = color;
                        binding.seedColorPicker.setPreviewColor(color);
                        colorsViewModel.setMonetSeedColor(color);
                    }
                })
                .show(getChildFragmentManager(), "seedColorPicker")
        );

        reinitSeedColorPickerVisibility();

        // Color palette
        binding.colorPalette.setOnClickListener(v -> HomeFragment.replaceFragment(new ColorPaletteFragment()));

        // Force per app theme
        binding.perAppTheme.setOnClickListener(v -> HomeFragment.replaceFragment(new PerAppThemeFragment()));
        binding.perAppTheme.setEnabled(notShizukuMode);
    }

    private void observeViewModel() {
        colorsViewModel.getMonetSeedColorLiveData().observe(getViewLifecycleOwner(), color -> {
            monetSeedColor = color;
            binding.seedColorPicker.setPreviewColor(color);
        });

        colorsViewModel.getWallpaperColorsLiveData().observe(getViewLifecycleOwner(), this::addWallpaperColorsToContainer);
        colorsViewModel.getBasicColorsLiveData().observe(getViewLifecycleOwner(), this::addBasicColorsToContainer);
    }

    private void addWallpaperColorsToContainer(List<Integer> colorList) {
        binding.wallpaperColorsContainer.removeAllViews();

        for (int color : colorList) {
            WallColorPreview colorPreview = createWallColorPreview(color, true);
            binding.wallpaperColorsContainer.addView(colorPreview);
        }
    }

    private void addBasicColorsToContainer(List<Integer> colorList) {
        binding.basicColorsContainer.removeAllViews();

        for (int color : colorList) {
            WallColorPreview colorPreview = createWallColorPreview(color, false);
            binding.basicColorsContainer.addView(colorPreview);
        }
    }

    @SuppressWarnings("all")
    private @NonNull WallColorPreview createWallColorPreview(int color, boolean isWallpaperColor) {
        WallColorPreview colorPreview = new WallColorPreview(requireContext());

        int size = (int) (48 * getResources().getDisplayMetrics().density);
        int margin = (int) (12 * getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.setMargins(margin, margin, margin, margin);

        colorPreview.setLayoutParams(layoutParams);
        colorPreview.setMainColor(color);
        colorPreview.setTag(color);
        colorPreview.setSelected(color == colorsViewModel.getMonetSeedColorLiveData().getValue());

        colorPreview.setOnClickListener(v -> {
            updateColorSelection(color, isWallpaperColor);
            binding.seedColorPicker.setPreviewColor(color);
            colorsViewModel.setMonetSeedColorEnabled(!isWallpaperColor);
            reinitSeedColorPickerVisibility();
            colorsViewModel.setMonetSeedColor(color);
        });

        return colorPreview;
    }

    private void updateColorSelection(int selectedColor, boolean isWallpaperColor) {
        ViewGroup parent = isWallpaperColor ?
                binding.wallpaperColorsContainer :
                binding.basicColorsContainer;
        ViewGroup otherParent = isWallpaperColor ?
                binding.basicColorsContainer :
                binding.wallpaperColorsContainer;

        int count = parent.getChildCount();
        int otherCount = otherParent.getChildCount();

        for (int i = 0; i < count; i++) {
            if (parent.getChildAt(i) instanceof WallColorPreview colorPreview) {
                colorPreview.setSelected((Integer) colorPreview.getTag() == selectedColor);
            }
        }

        for (int i = 0; i < otherCount; i++) {
            if (otherParent.getChildAt(i) instanceof WallColorPreview colorPreview) {
                colorPreview.setSelected(false);
            }
        }
    }

    private void reinitSeedColorPickerVisibility() {
        binding.seedColorPicker.setVisibility(
                colorsViewModel.isMonetSeedColorEnabled() ?
                        View.VISIBLE :
                        View.GONE
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(wallpaperChangedReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(wallpaperChangedReceiver);
        } catch (Exception ignored) {
            // Receiver was not registered
        }
        super.onDestroy();
    }
}

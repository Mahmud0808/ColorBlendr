package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentColorsBinding;
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel;
import com.drdisagree.colorblendr.ui.views.WallColorPreview;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.WallpaperUtil;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView;

@SuppressWarnings("deprecation")
public class ColorsFragment extends Fragment {

    private static final String TAG = ColorsFragment.class.getSimpleName();
    private FragmentColorsBinding binding;
    private int[] monetSeedColor;
    private SharedViewModel sharedViewModel;
    private final boolean notShizukuMode = Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;
    private final BroadcastReceiver wallpaperChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (binding.colorsToggleGroup.getCheckedButtonId() == R.id.wallpaper_colors_button) {
                addWallpaperColorItems();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        if (!notShizukuMode) {
            SettingsFragment.clearCustomColors();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColorsBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.app_name, false, binding.header.toolbar);

        monetSeedColor = new int[]{RPrefs.getInt(
                MONET_SEED_COLOR,
                WallpaperUtil.getWallpaperColor(requireContext())
        )};

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel.getVisibilityStates().observe(getViewLifecycleOwner(), this::updateViewVisibility);

        // Color codes
        binding.colorsToggleGroup.check(
                RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false) ?
                        R.id.basic_colors_button :
                        R.id.wallpaper_colors_button
        );
        binding.colorsToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.wallpaper_colors_button) {
                    addWallpaperColorItems();
                } else {
                    addBasicColorItems();
                }
            }
        });
        if (RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false)) {
            addBasicColorItems();
        } else {
            addWallpaperColorItems();
        }

        // Primary color
        binding.seedColorPicker.setPreviewColor(RPrefs.getInt(
                MONET_SEED_COLOR,
                monetSeedColor[0]
        ));
        binding.seedColorPicker.setOnClickListener(v -> new ColorPickerDialog()
                .withCornerRadius(10)
                .withColor(monetSeedColor[0])
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView.class)
                .withListener((pickerView, color) -> {
                    if (monetSeedColor[0] != color) {
                        monetSeedColor[0] = color;
                        binding.seedColorPicker.setPreviewColor(color);
                        RPrefs.putInt(MONET_SEED_COLOR, monetSeedColor[0]);
                        RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                OverlayManager.applyFabricatedColors(requireContext());
                            } catch (Exception ignored) {
                            }
                        }, 300);
                    }
                })
                .show(getChildFragmentManager(), "seedColorPicker")
        );
        binding.seedColorPicker.setVisibility(
                RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false) ?
                        View.VISIBLE :
                        View.GONE
        );

        // Color palette
        binding.colorPalette.setOnClickListener(v -> HomeFragment.replaceFragment(new ColorPaletteFragment()));

        // Force per app theme
        binding.perAppTheme.setOnClickListener(v -> HomeFragment.replaceFragment(new PerAppThemeFragment()));
        binding.perAppTheme.setEnabled(notShizukuMode);
    }

    private void updateViewVisibility(Map<String, Integer> visibilityStates) {
        Integer seedColorVisibility = visibilityStates.get(MONET_SEED_COLOR_ENABLED);
        if (seedColorVisibility != null && binding.seedColorPicker.getVisibility() != seedColorVisibility) {
            binding.seedColorPicker.setVisibility(seedColorVisibility);

            String wallpaperColors = RPrefs.getString(WALLPAPER_COLOR_LIST, null);
            ArrayList<Integer> wallpaperColorList = Const.GSON.fromJson(
                    wallpaperColors,
                    new TypeToken<ArrayList<Integer>>() {
                    }.getType()
            );

            if (seedColorVisibility == View.GONE) {
                monetSeedColor = new int[]{wallpaperColorList.get(0)};
                binding.seedColorPicker.setPreviewColor(monetSeedColor[0]);
            } else {
                monetSeedColor = new int[]{RPrefs.getInt(
                        MONET_SEED_COLOR,
                        wallpaperColorList.get(0)
                )};
                binding.seedColorPicker.setPreviewColor(monetSeedColor[0]);
            }
        }
    }

    private void addWallpaperColorItems() {
        String wallpaperColors = RPrefs.getString(WALLPAPER_COLOR_LIST, null);
        ArrayList<Integer> wallpaperColorList;

        if (wallpaperColors != null) {
            wallpaperColorList = Const.GSON.fromJson(
                    wallpaperColors,
                    new TypeToken<ArrayList<Integer>>() {
                    }.getType()
            );
        } else {
            wallpaperColorList = ColorUtil.getMonetAccentColors();
        }

        addColorsToContainer(wallpaperColorList, true);
    }

    @SuppressWarnings("all")
    private void addBasicColorItems() {
        String[] basicColors = getResources().getStringArray(R.array.basic_color_codes);
        List<Integer> basicColorList = Arrays.stream(basicColors)
                .map(Color::parseColor)
                .collect(Collectors.toList());

        addColorsToContainer(new ArrayList<>(basicColorList), false);
    }

    private void addColorsToContainer(ArrayList<Integer> colorList, boolean isWallpaperColors) {
        binding.colorsContainer.removeAllViews();

        for (int i = 0; i < colorList.size(); i++) {
            int size = (int) (48 * getResources().getDisplayMetrics().density);
            int margin = (int) (12 * getResources().getDisplayMetrics().density);

            WallColorPreview colorPreview = new WallColorPreview(requireContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
            layoutParams.setMargins(margin, margin, margin, margin);
            colorPreview.setLayoutParams(layoutParams);
            colorPreview.setMainColor(colorList.get(i));
            colorPreview.setTag(colorList.get(i));
            colorPreview.setSelected(colorList.get(i) == RPrefs.getInt(MONET_SEED_COLOR, Integer.MIN_VALUE));

            colorPreview.setOnClickListener(v -> {
                RPrefs.putInt(MONET_SEED_COLOR, (Integer) colorPreview.getTag());
                RPrefs.putBoolean(MONET_SEED_COLOR_ENABLED, !isWallpaperColors);
                binding.seedColorPicker.setPreviewColor((Integer) colorPreview.getTag());
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                OverlayManager.applyFabricatedColors(requireContext());
            });

            binding.colorsContainer.addView(colorPreview);
        }
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
package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.WallpaperUtil;
import com.google.android.material.snackbar.Snackbar;
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
    private LinearLayout[] colorTableRows;
    private SharedViewModel sharedViewModel;
    private final String[][] colorNames = ColorUtil.getColorNames();
    private final boolean notShizukuMode = Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;
    private static final int[] colorCodes = {
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
    };
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

        colorTableRows = new LinearLayout[]{
                binding.colorPreview.systemAccent1,
                binding.colorPreview.systemAccent2,
                binding.colorPreview.systemAccent3,
                binding.colorPreview.systemNeutral1,
                binding.colorPreview.systemNeutral2
        };

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel.getBooleanStates().observe(getViewLifecycleOwner(), this::updateBooleanStates);
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

        // Color table preview
        initColorTablePreview(colorTableRows);

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
                        updatePreviewColors(
                                colorTableRows,
                                generateModifiedColors()
                        );
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

        // Force per app theme
        binding.perAppTheme.setOnClickListener(v -> HomeFragment.replaceFragment(new PerAppThemeFragment()));
        binding.perAppTheme.setEnabled(notShizukuMode);
    }

    private void updateBooleanStates(Map<String, Boolean> stringBooleanMap) {
        Boolean accurateShades = stringBooleanMap.get(MONET_ACCURATE_SHADES);
        if (accurateShades != null) {
            try {
                updatePreviewColors(
                        colorTableRows,
                        generateModifiedColors()
                );
            } catch (Exception ignored) {
            }
        }
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

    private void updatePreviewColors(LinearLayout[] colorTableRows, ArrayList<ArrayList<Integer>> palette) {
        if (palette == null) return;

        // Update preview colors
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                colorTableRows[i].getChildAt(j).getBackground().setTint(palette.get(i).get(j));
                colorTableRows[i].getChildAt(j).setTag(palette.get(i).get(j));
                ((TextView) ((ViewGroup) colorTableRows[i].getChildAt(j))
                        .getChildAt(0))
                        .setTextColor(ColorUtil.calculateTextColor(palette.get(i).get(j)));
            }
        }
    }

    private void initColorTablePreview(LinearLayout[] colorTableRows) {
        new Thread(() -> {
            try {
                ArrayList<ArrayList<Integer>> palette = generateModifiedColors();

                int[][] systemColors = palette == null ?
                        ColorUtil.getSystemColors(requireContext()) :
                        MiscUtil.convertListToIntArray(palette);

                for (int i = 0; i < colorTableRows.length; i++) {
                    for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                        colorTableRows[i].getChildAt(j).getBackground().setTint(systemColors[i][j]);
                        colorTableRows[i].getChildAt(j).setTag(systemColors[i][j]);

                        if (RPrefs.getInt(colorNames[i][j], Integer.MIN_VALUE) != Integer.MIN_VALUE) {
                            colorTableRows[i].getChildAt(j).getBackground().setTint(RPrefs.getInt(colorNames[i][j], 0));
                        }

                        TextView textView = new TextView(requireContext());
                        textView.setText(String.valueOf(colorCodes[j]));
                        textView.setRotation(270);
                        textView.setTextColor(ColorUtil.calculateTextColor(systemColors[i][j]));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        textView.setAlpha(0.8f);
                        textView.setMaxLines(1);
                        textView.setSingleLine(true);
                        textView.setAutoSizeTextTypeUniformWithConfiguration(
                                1,
                                20,
                                1,
                                TypedValue.COMPLEX_UNIT_SP
                        );

                        int finalI = i;
                        int finalJ = j;
                        requireActivity().runOnUiThread(() -> {
                            ((ViewGroup) colorTableRows[finalI].getChildAt(finalJ)).addView(textView);
                            ((LinearLayout) colorTableRows[finalI].getChildAt(finalJ)).setGravity(Gravity.CENTER);
                        });
                    }
                }

                requireActivity().runOnUiThread(() -> enablePaletteOnClickListener(colorTableRows));
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void enablePaletteOnClickListener(LinearLayout[] colorTableRows) {
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                int finalI = i;
                int finalJ = j;

                colorTableRows[i].getChildAt(j).setOnClickListener(v -> {
                    boolean manualOverride = RPrefs.getBoolean(MANUAL_OVERRIDE_COLORS, false);
                    String snackbarButton = getString(manualOverride ? R.string.override : R.string.copy);

                    Snackbar.make(
                                    requireView(),
                                    getString(R.string.color_code, ColorUtil.intToHexColor((Integer) v.getTag())),
                                    Snackbar.LENGTH_INDEFINITE)
                            .setAction(snackbarButton, v1 -> {
                                if (!manualOverride || !notShizukuMode) {
                                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(ColorUtil.getColorNames()[finalI][finalJ], ColorUtil.intToHexColor((Integer) v.getTag()));
                                    clipboard.setPrimaryClip(clip);
                                    return;
                                }

                                if (finalJ == 0 || finalJ == 12) {
                                    Snackbar.make(
                                                    requireView(),
                                                    getString(R.string.cannot_override_color),
                                                    Snackbar.LENGTH_SHORT
                                            )
                                            .setAction(getString(R.string.override), v2 -> {
                                            })
                                            .show();
                                    return;
                                }

                                new ColorPickerDialog()
                                        .withCornerRadius(10)
                                        .withColor((Integer) v.getTag())
                                        .withAlphaEnabled(false)
                                        .withPicker(ImagePickerView.class)
                                        .withListener((pickerView, color) -> {
                                            if ((Integer) v.getTag() != color) {
                                                v.setTag(color);
                                                v.getBackground().setTint(color);
                                                ((TextView) ((ViewGroup) v)
                                                        .getChildAt(0))
                                                        .setTextColor(ColorUtil.calculateTextColor(color));
                                                RPrefs.putInt(colorNames[finalI][finalJ], color);

                                                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                    try {
                                                        OverlayManager.applyFabricatedColors(requireContext());
                                                    } catch (Exception ignored) {
                                                    }
                                                }, 200);
                                            }
                                        })
                                        .show(getChildFragmentManager(), "overrideColorPicker" + finalI + finalJ);
                            })
                            .show();
                });

                colorTableRows[i].getChildAt(j).setOnLongClickListener(v -> {
                    if (finalJ == 0 || finalJ == 12) {
                        return true;
                    }

                    RPrefs.clearPref(colorNames[finalI][finalJ]);

                    try {
                        OverlayManager.applyFabricatedColors(requireContext());
                    } catch (Exception ignored) {
                    }
                    return true;
                });
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
                ArrayList<ArrayList<Integer>> modifiedColors = generateModifiedColors();
                updatePreviewColors(
                        colorTableRows,
                        modifiedColors
                );
                binding.seedColorPicker.setPreviewColor((Integer) colorPreview.getTag());
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                OverlayManager.applyFabricatedColors(requireContext());
            });

            binding.colorsContainer.addView(colorPreview);
        }
    }

    private ArrayList<ArrayList<Integer>> generateModifiedColors() {
        try {
            return ColorUtil.generateModifiedColors(
                    requireContext(),
                    ColorSchemeUtil.stringToEnumMonetStyle(
                            requireContext(),
                            RPrefs.getString(MONET_STYLE, getString(R.string.monet_tonalspot))
                    ),
                    RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                    RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                    RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                    RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                    RPrefs.getBoolean(MONET_ACCURATE_SHADES, true)
            );
        } catch (Exception e) {
            Log.e(TAG, "Error generating modified colors", e);
            return null;
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
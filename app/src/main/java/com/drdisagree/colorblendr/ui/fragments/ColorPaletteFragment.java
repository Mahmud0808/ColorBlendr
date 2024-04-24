package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentColorPaletteBinding;
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel;
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Map;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView;

public class ColorPaletteFragment extends Fragment {

    private static final String TAG = ColorPaletteFragment.class.getSimpleName();
    private FragmentColorPaletteBinding binding;
    private LinearLayout[] colorTableRows;
    private SharedViewModel sharedViewModel;
    private final String[][] colorNames = ColorUtil.getColorNames();
    private final boolean notShizukuMode = Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;
    private static final int[] colorCodes = {
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColorPaletteBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.color_palette_title, true, binding.header.toolbar);

        colorTableRows = new LinearLayout[]{
                binding.colorPreview.systemAccent1,
                binding.colorPreview.systemAccent2,
                binding.colorPreview.systemAccent3,
                binding.colorPreview.systemNeutral1,
                binding.colorPreview.systemNeutral2
        };

        // Warning message
        boolean isOverrideAvailable = notShizukuMode &&
                RPrefs.getBoolean(MANUAL_OVERRIDE_COLORS, false);

        binding.warn.warningText.setText(isOverrideAvailable ?
                R.string.color_palette_root_warn :
                R.string.color_palette_rootless_warn
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel.getBooleanStates().observe(getViewLifecycleOwner(), this::updateBooleanStates);

        // Color table preview
        initColorTablePreview(colorTableRows);
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
}
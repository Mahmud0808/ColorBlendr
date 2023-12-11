package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentToolsBinding;
import com.drdisagree.colorblendr.ui.viewmodel.SharedViewModel;

public class ToolsFragment extends Fragment {

    private FragmentToolsBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);

        binding.accurateShades.setSwitchChecked(RPrefs.getBoolean(MONET_ACCURATE_SHADES, true));
        binding.accurateShades.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_ACCURATE_SHADES, isChecked);
            sharedViewModel.setBooleanState(MONET_ACCURATE_SHADES, isChecked);
        });

        binding.pitchBlackTheme.setSwitchChecked(RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false));
        binding.pitchBlackTheme.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(MONET_PITCH_BLACK_THEME, isChecked));

        binding.customPrimaryColor.setSwitchChecked(RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false));
        binding.customPrimaryColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_SEED_COLOR_ENABLED, isChecked);
            sharedViewModel.setVisibilityState(MONET_SEED_COLOR_ENABLED, isChecked ? View.VISIBLE : View.GONE);
        });

        binding.overrideColorsManually.setSwitchChecked(RPrefs.getBoolean(MANUAL_OVERRIDE_COLORS, false));
        binding.overrideColorsManually.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(MANUAL_OVERRIDE_COLORS, isChecked));

        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }
}
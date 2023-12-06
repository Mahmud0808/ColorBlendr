package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentToolsBinding;

public class ToolsFragment extends Fragment {

    private FragmentToolsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);

        binding.pitchBlackTheme.setSwitchChecked(RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false));
        binding.pitchBlackTheme.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(MONET_PITCH_BLACK_THEME, isChecked));

        return binding.getRoot();
    }
}
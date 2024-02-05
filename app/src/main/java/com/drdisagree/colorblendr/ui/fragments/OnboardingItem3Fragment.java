package com.drdisagree.colorblendr.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem3Binding;

public class OnboardingItem3Fragment extends Fragment {

    private FragmentOnboardingItem3Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false);

        binding.root.setOnClickListener(v -> {
            Const.WORKING_METHOD = Const.WORK_METHOD.ROOT;
            binding.shizuku.setSelected(false);
        });

        binding.shizuku.setOnClickListener(v -> {
            Const.WORKING_METHOD = Const.WORK_METHOD.SHIZUKU;
            binding.root.setSelected(false);
        });

        return binding.getRoot();
    }
}
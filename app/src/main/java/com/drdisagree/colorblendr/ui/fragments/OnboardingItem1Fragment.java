package com.drdisagree.colorblendr.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem1Binding;

public class OnboardingItem1Fragment extends Fragment {

    private FragmentOnboardingItem1Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingItem1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
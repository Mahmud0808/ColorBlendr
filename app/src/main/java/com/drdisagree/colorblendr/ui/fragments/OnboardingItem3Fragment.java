package com.drdisagree.colorblendr.ui.fragments;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem3Binding;

public class OnboardingItem3Fragment extends Fragment {

    private FragmentOnboardingItem3Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false);

        binding.title.setText(getString(R.string.auto_startup_title));
        binding.description.setText(HtmlCompat.fromHtml(getString(R.string.auto_startup_desc, getString(R.string.app_name), getString(R.string.app_name)), HtmlCompat.FROM_HTML_MODE_COMPACT));
        binding.description.setMovementMethod(LinkMovementMethod.getInstance());

        return binding.getRoot();
    }
}
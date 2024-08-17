package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem1Binding

class OnboardingItem1Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingItem1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem1Binding.inflate(inflater, container, false)
        return binding.getRoot()
    }
}
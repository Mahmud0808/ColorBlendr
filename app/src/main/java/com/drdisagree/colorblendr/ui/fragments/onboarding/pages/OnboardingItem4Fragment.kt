package com.drdisagree.colorblendr.ui.fragments.onboarding.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.enums.WorkMethod
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem4Binding

class OnboardingItem4Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingItem4Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem4Binding.inflate(inflater, container, false)

        binding.root.setOnClickListener {
            WORKING_METHOD = WorkMethod.ROOT
            binding.shizuku.isSelected = false
        }

        binding.shizuku.setOnClickListener {
            WORKING_METHOD = WorkMethod.SHIZUKU
            binding.root.isSelected = false
        }

        return binding.getRoot()
    }
}
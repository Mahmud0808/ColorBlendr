package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem3Binding

class OnboardingItem3Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingItem3Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false)

        binding.root.setOnClickListener {
            Const.WORKING_METHOD =
                Const.WorkMethod.ROOT
            binding.shizuku.isSelected = false
        }

        binding.shizuku.setOnClickListener {
            Const.WORKING_METHOD =
                Const.WorkMethod.SHIZUKU
            binding.root.isSelected = false
        }

        return binding.getRoot()
    }
}
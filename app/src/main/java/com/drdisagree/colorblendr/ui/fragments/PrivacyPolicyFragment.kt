package com.drdisagree.colorblendr.ui.fragments

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.databinding.FragmentPrivacyPolicyBinding
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import java.io.IOException

class PrivacyPolicyFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)

        setToolbarTitle(
            requireContext(),
            R.string.privacy_policy,
            true,
            binding.header.toolbar
        )

        binding.privacyPolicyText.apply {
            text = loadPrivacyPolicyFromAssets(context)
            typeface = Typeface.MONOSPACE
        }

        return binding.root
    }

    private fun loadPrivacyPolicyFromAssets(context: Context): String {
        return try {
            context.assets.open("privacy_policy.txt").bufferedReader().use { it.readText().trim() }
        } catch (e: IOException) {
            Log.e("PrivacyPolicyFragment", "Error loading privacy policy: ${e.message}")
            "Unable to load privacy policy."
        }
    }
}
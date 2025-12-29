package com.drdisagree.colorblendr.ui.fragments

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
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
            val rawText = loadPrivacyPolicyFromAssets(context)
            val boldText = markdownToBold(rawText)
            text = boldText
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

    private fun markdownToBold(text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        val regex = "\\*\\*(.*?)\\*\\*".toRegex()

        var offset = 0
        regex.findAll(text).forEach { match ->
            val start = match.range.first - offset
            val end = match.range.last + 1 - offset

            // Remove ending **
            builder.delete(end - 2, end)
            // Remove starting **
            builder.delete(start, start + 2)

            // Apply bold span
            builder.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end - 4,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            offset += 4
        }

        return builder
    }
}
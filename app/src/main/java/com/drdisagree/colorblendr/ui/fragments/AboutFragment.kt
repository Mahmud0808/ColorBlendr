package com.drdisagree.colorblendr.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.databinding.FragmentAboutBinding
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)

        setToolbarTitle(
            requireContext(),
            R.string.about_this_app_title,
            true,
            binding.header.toolbar
        )

        try {
            binding.appIcon.setImageDrawable(
                requireContext().packageManager.getApplicationIcon(
                    requireContext().packageName
                )
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
            // Unlikely to happen
            binding.appIcon.setImageResource(R.mipmap.ic_launcher)
        }
        binding.versionCode.text = getString(
            R.string.version_codes,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        binding.btnNews.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/IconifyOfficial")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnSupport.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/IconifyDiscussion")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnGithub.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Mahmud0808/ColorBlendr")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.developer.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Mahmud0808")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.buymeacoffee.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://buymeacoffee.com/drdisagree")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return binding.root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            parentFragmentManager.popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
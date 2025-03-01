package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.databinding.FragmentAboutBinding
import com.drdisagree.colorblendr.ui.adapters.AboutAppAdapter
import com.drdisagree.colorblendr.data.models.AboutAppModel
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.app.parseContributors
import com.drdisagree.colorblendr.utils.app.parseTranslators

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

        binding.aboutAppContainer.apply {
            setLayoutManager(LinearLayoutManager(requireContext()))
            setAdapter(
                ConcatAdapter(
                    initAboutAppSection(),
                    initContributorsSection(),
                    initTranslatorsSection()
                )
            )
            setHasFixedSize(true)
        }

        return binding.root
    }

    private fun initAboutAppSection(): AboutAppAdapter {
        return AboutAppAdapter(
            requireContext(),
            ArrayList<AboutAppModel>().also {
                it.add(AboutAppModel(R.layout.view_about_app))
            }
        )
    }

    private fun initContributorsSection(): AboutAppAdapter {
        return AboutAppAdapter(
            requireContext(),
            parseContributors().also {
                it.add(0, AboutAppModel(resources.getString(R.string.contributors)))
            }
        )
    }

    private fun initTranslatorsSection(): AboutAppAdapter {
        return AboutAppAdapter(
            requireContext(),
            parseTranslators().also {
                it.add(0, AboutAppModel(resources.getString(R.string.translators)))
            }
        )
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
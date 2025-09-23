package com.drdisagree.colorblendr.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.EXCLUDED_PREFS_FROM_BACKUP
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.config.Prefs.toGsonString
import com.drdisagree.colorblendr.data.models.CustomStyleModel
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.databinding.FragmentStylesBinding
import com.drdisagree.colorblendr.databinding.ViewTextFieldOutlinedBinding
import com.drdisagree.colorblendr.ui.adapters.StylePreviewAdapter
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.app.DividerItemDecoration
import com.drdisagree.colorblendr.utils.app.MiscUtil.getDialogPreferredPadding
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.app.MiscUtil.toPx
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class StylesFragment : BaseFragment() {

    private lateinit var binding: FragmentStylesBinding
    private var styleAdapter: StylePreviewAdapter? = null
    private val customStyleRepository = Utilities.getCustomStyleRepository()
    private val stylesViewModel: StylesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStylesBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.styles, true, binding.header.toolbar)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext().resources.getDimensionPixelSize(R.dimen.container_margin_bottom)
            )
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stylesViewModel.stylePalettes.observe(viewLifecycleOwner) { stylePalettes ->
            styleAdapter = StylePreviewAdapter(
                this@StylesFragment,
                stylesViewModel.styleList.value.orEmpty().toMutableList(),
                stylePalettes
            )
            binding.recyclerView.adapter = styleAdapter
        }

        binding.addStyle.visibility = if (isRootMode()) View.VISIBLE else View.GONE
        binding.addStyle.setOnClickListener {
            showNewStyleDialog(
                callback = { title, desc ->
                    lifecycleScope.launch {
                        addCustomStyle(title = title, description = desc)
                    }
                }
            )
        }
    }

    fun showNewStyleDialog(
        title: String = "",
        desc: String = "",
        callback: (String, String) -> Unit
    ) {
        binding.addStyle.visibility = View.GONE

        val dialogLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(
                    context.getDialogPreferredPadding(),
                    0,
                    context.getDialogPreferredPadding(),
                    0
                )
            }
        }

        View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                context.toPx(16)
            )
            dialogLayout.addView(this)
        }

        val titleBinding = ViewTextFieldOutlinedBinding.inflate(layoutInflater).apply {
            outlinedTextField.hint = getString(R.string.title)
            outlinedTextField.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            outlinedTextField.editText?.gravity = Gravity.TOP or Gravity.START
            if (title.isNotEmpty()) {
                outlinedTextField.editText?.setText(title)
            }
            dialogLayout.addView(root)
        }

        View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                context.toPx(8)
            )
            dialogLayout.addView(this)
        }

        val descBinding = ViewTextFieldOutlinedBinding.inflate(layoutInflater).apply {
            outlinedTextField.hint = getString(R.string.description)
            outlinedTextField.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            outlinedTextField.editText?.gravity = Gravity.TOP or Gravity.START
            if (desc.isNotEmpty()) {
                outlinedTextField.editText?.setText(desc)
            }
            dialogLayout.addView(root)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.save_current_style)
            .setView(dialogLayout)
            .setPositiveButton(R.string.save) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()

                callback.invoke(
                    titleBinding.outlinedTextField.editText?.text.toString(),
                    descBinding.outlinedTextField.editText?.text.toString()
                )
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                binding.addStyle.visibility = if (isRootMode()) View.VISIBLE else View.GONE
            }
            .show()
    }

    private suspend fun addCustomStyle(
        title: String,
        description: String
    ) {
        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.title_and_desc_cant_be_empty,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val allPrefs = Prefs.getAllPrefs()
        for (excludedPref in EXCLUDED_PREFS_FROM_BACKUP) {
            allPrefs.remove(excludedPref)
        }

        val currentMonet = getCurrentMonetStyle()
        val newStyle = CustomStyleModel(
            styleName = title.trim(),
            description = description.trim(),
            prefsGson = allPrefs.mapValues { it.value as Any }.toGsonString(),
            monet = currentMonet,
            palette = ColorUtil.generateModifiedColors(
                currentMonet,
                getAccentSaturation(),
                getBackgroundSaturation(),
                getBackgroundLightness(),
                pitchBlackThemeEnabled(),
                accurateShadesEnabled()
            )
        )

        customStyleRepository.saveCustomStyle(newStyle)
        styleAdapter?.addStyle(
            StyleModel(
                isEnabled = true,
                monetStyle = currentMonet,
                customStyle = newStyle
            )
        )
        stylesViewModel.refreshData()
    }

    suspend fun editCustomStyle(
        title: String,
        description: String,
        styleId: String
    ) {
        if (title.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.title_and_desc_cant_be_empty,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val customStyle = customStyleRepository.getCustomStyleById(styleId)
        if (customStyle != null) {
            val updatedStyle = customStyle.copy(
                styleName = title.trim(),
                description = description.trim()
            )

            customStyleRepository.updateCustomStyle(updatedStyle)
            styleAdapter?.updateStyle(
                StyleModel(
                    isEnabled = true,
                    monetStyle = updatedStyle.monet,
                    customStyle = updatedStyle
                )
            )
            stylesViewModel.refreshData()
        }
    }

    suspend fun deleteCustomStyle(
        styleId: String
    ) {
        val customStyle = customStyleRepository.getCustomStyleById(styleId)
        if (customStyle != null) {
            customStyleRepository.deleteCustomStyle(customStyle)
            styleAdapter?.removeStyle(customStyle = customStyle)
            stylesViewModel.refreshData()
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && isAdded) {
            parentFragmentManager.popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
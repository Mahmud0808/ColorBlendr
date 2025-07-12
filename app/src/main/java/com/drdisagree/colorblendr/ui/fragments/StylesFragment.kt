package com.drdisagree.colorblendr.ui.fragments

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.CustomStyleModel
import com.drdisagree.colorblendr.data.models.StyleModel
import com.drdisagree.colorblendr.databinding.FragmentStylesBinding
import com.drdisagree.colorblendr.databinding.ViewTextFieldOutlinedBinding
import com.drdisagree.colorblendr.ui.adapters.StylePreviewAdapter
import com.drdisagree.colorblendr.utils.app.DividerItemDecoration
import com.drdisagree.colorblendr.utils.app.MiscUtil.getDialogPreferredPadding
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.app.MiscUtil.toPx
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class StylesFragment : Fragment() {

    private lateinit var binding: FragmentStylesBinding
    private var styleAdapter: StylePreviewAdapter? = null
    private val customStyleRepository = Utilities.getCustomStyleRepository()
    private val isAtleastA13 = isRootMode() ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val isAtleastA14 = isRootMode() ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

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

        lifecycleScope.launch {
            val styles = getStyleList()
            styleAdapter = StylePreviewAdapter(this@StylesFragment, styles)
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

        return binding.root
    }

    fun showNewStyleDialog(
        title: String = "",
        desc: String = "",
        callback: (String, String) -> Unit
    ) {
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
        }
    }

    suspend fun deleteCustomStyle(
        styleId: String
    ) {
        val customStyle = customStyleRepository.getCustomStyleById(styleId)
        if (customStyle != null) {
            customStyleRepository.deleteCustomStyle(customStyle)
            styleAdapter?.removeStyle(customStyle = customStyle)
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

    private suspend fun getStyleList(): ArrayList<StyleModel> {
        return arrayListOf(
            StyleModel(
                titleResId = R.string.monet_neutral,
                descriptionResId = R.string.monet_neutral_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.SPRITZ
            ),
            StyleModel(
                titleResId = R.string.monet_monochrome,
                descriptionResId = R.string.monet_monochrome_desc,
                isEnabled = isAtleastA14,
                monetStyle = MONET.MONOCHROMATIC
            ),
            StyleModel(
                titleResId = R.string.monet_tonalspot,
                descriptionResId = R.string.monet_tonalspot_desc,
                isEnabled = true,
                monetStyle = MONET.TONAL_SPOT
            ),
            StyleModel(
                titleResId = R.string.monet_vibrant,
                descriptionResId = R.string.monet_vibrant_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.VIBRANT
            ),
            StyleModel(
                titleResId = R.string.monet_rainbow,
                descriptionResId = R.string.monet_rainbow_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.RAINBOW
            ),
            StyleModel(
                titleResId = R.string.monet_expressive,
                descriptionResId = R.string.monet_expressive_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.EXPRESSIVE
            ),
            StyleModel(
                titleResId = R.string.monet_fidelity,
                descriptionResId = R.string.monet_fidelity_desc,
                isEnabled = isRootMode(),
                monetStyle = MONET.FIDELITY
            ),
            StyleModel(
                titleResId = R.string.monet_content,
                descriptionResId = R.string.monet_content_desc,
                isEnabled = isRootMode(),
                monetStyle = MONET.CONTENT
            ),
            StyleModel(
                titleResId = R.string.monet_fruitsalad,
                descriptionResId = R.string.monet_fruitsalad_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.FRUIT_SALAD
            )
        ).apply {
            if (!isRootMode()) return@apply

            customStyleRepository.getCustomStyles().forEach { customStyle ->
                add(
                    StyleModel(
                        isEnabled = true,
                        monetStyle = customStyle.monet,
                        customStyle = customStyle
                    )
                )
            }
        }
    }
}
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.EXCLUDED_PREFS_FROM_BACKUP
import com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.config.RPrefs.toGsonString
import com.drdisagree.colorblendr.databinding.FragmentStylesBinding
import com.drdisagree.colorblendr.databinding.ViewTextFieldOutlinedBinding
import com.drdisagree.colorblendr.ui.adapters.StylePreviewAdapter
import com.drdisagree.colorblendr.ui.models.CustomStyleModel
import com.drdisagree.colorblendr.ui.models.StyleModel
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.getCurrentMonetStyle
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.getCustomStyles
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.saveCustomStyles
import com.drdisagree.colorblendr.utils.ColorUtil
import com.drdisagree.colorblendr.utils.DividerItemDecoration
import com.drdisagree.colorblendr.utils.MONET
import com.drdisagree.colorblendr.utils.MiscUtil.getDialogPreferredPadding
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.MiscUtil.toPx
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class StylesFragment : Fragment() {

    private lateinit var binding: FragmentStylesBinding
    private val notShizukuMode = workingMethod != Const.WorkMethod.SHIZUKU
    private val isAtleastA13 = notShizukuMode ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val isAtleastA14 = notShizukuMode ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    private val styleAdapter = StylePreviewAdapter(this, getStyleList())

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
                requireContext(),
                requireContext().resources.getDimensionPixelSize(R.dimen.container_margin_bottom)
            )
        )
        binding.recyclerView.adapter = styleAdapter

        binding.addStyle.visibility = if (notShizukuMode) View.VISIBLE else View.GONE
        binding.addStyle.setOnClickListener {
            showNewStyleDialog(
                callback = { title, desc ->
                    addCustomStyle(title = title, description = desc)
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

    private fun addCustomStyle(
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

        val allPrefs = RPrefs.getAllPrefs()
        for (excludedPref in EXCLUDED_PREFS_FROM_BACKUP) {
            allPrefs.remove(excludedPref)
        }

        saveCustomStyles(
            getCustomStyles().apply {
                val currentMonet = getCurrentMonetStyle()
                val newStyle = CustomStyleModel(
                    styleName = title.trim(),
                    description = description.trim(),
                    prefsGson = allPrefs.mapValues { it.value as Any }.toGsonString(),
                    monet = currentMonet,
                    palette = ColorUtil.generateModifiedColors(
                        currentMonet,
                        getInt(MONET_ACCENT_SATURATION, 100),
                        getInt(MONET_BACKGROUND_SATURATION, 100),
                        getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                        getBoolean(MONET_PITCH_BLACK_THEME, false),
                        getBoolean(MONET_ACCURATE_SHADES, true)
                    )
                )
                add(newStyle)
                styleAdapter.addStyle(
                    StyleModel(
                        isEnabled = true,
                        monetStyle = currentMonet,
                        customStyle = newStyle
                    )
                )
            }
        )
    }

    fun editCustomStyle(
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

        saveCustomStyles(
            getCustomStyles().apply {
                val index = indexOfFirst { it.styleId == styleId }
                if (index != -1) {
                    val styleToEdit = this[index].copy(
                        styleName = title.trim(),
                        description = description.trim()
                    )
                    set(index, styleToEdit)
                    styleAdapter.updateStyle(
                        StyleModel(
                            isEnabled = true,
                            monetStyle = styleToEdit.monet,
                            customStyle = styleToEdit
                        )
                    )
                }
            }
        )
    }

    fun deleteCustomStyle(
        styleId: String
    ) {
        saveCustomStyles(
            getCustomStyles().apply {
                val index = indexOfFirst { it.styleId == styleId }
                if (index != -1) {
                    styleAdapter.removeStyle(
                        StyleModel(
                            isEnabled = true,
                            monetStyle = /* unused */ MONET.TONAL_SPOT,
                            customStyle = this[index]
                        )
                    )
                    removeAt(index)
                }
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

    private fun getStyleList(): ArrayList<StyleModel> {
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
                isEnabled = notShizukuMode,
                monetStyle = MONET.FIDELITY
            ),
            StyleModel(
                titleResId = R.string.monet_content,
                descriptionResId = R.string.monet_content_desc,
                isEnabled = notShizukuMode,
                monetStyle = MONET.CONTENT
            ),
            StyleModel(
                titleResId = R.string.monet_fruitsalad,
                descriptionResId = R.string.monet_fruitsalad_desc,
                isEnabled = isAtleastA13,
                monetStyle = MONET.FRUIT_SALAD
            )
        ).apply {
            if (!notShizukuMode) return@apply

            getCustomStyles().forEach { customStyle ->
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
package com.drdisagree.colorblendr.ui.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.manualColorOverrideEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.config.Prefs.clearPref
import com.drdisagree.colorblendr.data.config.Prefs.getInt
import com.drdisagree.colorblendr.data.config.Prefs.putInt
import com.drdisagree.colorblendr.databinding.FragmentColorPaletteBinding
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToHexColor
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView

class ColorPaletteFragment : Fragment() {

    private lateinit var binding: FragmentColorPaletteBinding
    private lateinit var colorTableRows: Array<LinearLayout>
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentColorPaletteBinding.inflate(inflater, container, false)

        setToolbarTitle(
            requireContext(),
            R.string.color_palette_title,
            true,
            binding.header.toolbar
        )

        colorTableRows = arrayOf(
            binding.colorPreview.systemAccent1,
            binding.colorPreview.systemAccent2,
            binding.colorPreview.systemAccent3,
            binding.colorPreview.systemNeutral1,
            binding.colorPreview.systemNeutral2
        )

        // Warning message
        val isOverrideAvailable = isRootMode() && manualColorOverrideEnabled()

        binding.warn.warningText.setText(
            if (isOverrideAvailable) R.string.color_palette_root_warn else R.string.color_palette_rootless_warn
        )

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.getBooleanStates()
            .observe(getViewLifecycleOwner()) { stringBooleanMap: Map<String, Boolean> ->
                this.updateBooleanStates(
                    stringBooleanMap
                )
            }

        // Color table preview
        initColorTablePreview(colorTableRows)
    }

    private fun updateBooleanStates(stringBooleanMap: Map<String, Boolean>) {
        val accurateShades: Boolean? = stringBooleanMap[MONET_ACCURATE_SHADES]
        if (accurateShades != null) {
            try {
                updatePreviewColors(
                    colorTableRows,
                    generateModifiedColors()
                )
            } catch (_: Exception) {
            }
        }
    }

    private fun updatePreviewColors(
        colorTableRows: Array<LinearLayout>,
        palette: ArrayList<ArrayList<Int>>?
    ) {
        if (palette == null) return

        // Update preview colors
        for (i in colorTableRows.indices) {
            for (j in 0 until colorTableRows[i].childCount) {
                colorTableRows[i].getChildAt(j).background.setTint(palette[i][j])
                colorTableRows[i].getChildAt(j).tag = palette[i][j]
                ((colorTableRows[i].getChildAt(j) as ViewGroup)
                    .getChildAt(0) as TextView)
                    .setTextColor(calculateTextColor(palette[i][j]))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initColorTablePreview(colorTableRows: Array<LinearLayout>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val systemColors = generateModifiedColors()

                withContext(Dispatchers.Main) {
                    for (i in colorTableRows.indices) {
                        for (j in 0 until colorTableRows[i].childCount) {
                            val childView = colorTableRows[i].getChildAt(j)
                            childView.background.setTint(systemColors[i][j])
                            childView.tag = systemColors[i][j]

                            if (getInt(systemPaletteNames[i][j], Int.MIN_VALUE) != Int.MIN_VALUE) {
                                childView.background.setTint(getInt(systemPaletteNames[i][j], 0))
                            }

                            val textView = TextView(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                text = colorCodes[j].toString()
                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                setTextColor(calculateTextColor(systemColors[i][j]))
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                alpha = 0.8f
                                setMaxLines(1)
                                setSingleLine(true)
                                setAutoSizeTextTypeUniformWithConfiguration(
                                    1,
                                    20,
                                    1,
                                    TypedValue.COMPLEX_UNIT_SP
                                )
                            }

                            (childView as ViewGroup).addView(textView)
                            (childView as LinearLayout).gravity = Gravity.CENTER
                        }
                    }

                    enablePaletteOnClickListener(colorTableRows)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing color table preview", e)
            }
        }
    }

    private fun enablePaletteOnClickListener(colorTableRows: Array<LinearLayout>) {
        for (i in colorTableRows.indices) {
            for (j in 0 until colorTableRows[i].childCount) {
                val finalI: Int = i
                val finalJ: Int = j

                colorTableRows[i].getChildAt(j).setOnClickListener { v: View ->
                    val manualOverride: Boolean = manualColorOverrideEnabled()
                    val snackbarButton: String = getString(
                        if (manualOverride) {
                            R.string.override
                        } else {
                            R.string.copy
                        }
                    )

                    Snackbar
                        .make(
                            requireView(),
                            getString(R.string.color_code, intToHexColor((v.tag as Int))),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        .setAction(snackbarButton) {
                            if (!manualOverride || isShizukuMode()) {
                                val clipboard: ClipboardManager =
                                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip: ClipData = ClipData.newPlainText(
                                    systemPaletteNames[finalI][finalJ],
                                    intToHexColor(v.tag as Int)
                                )
                                clipboard.setPrimaryClip(clip)
                                return@setAction
                            }

                            if (finalJ == 0 || finalJ == 12) {
                                Snackbar.make(
                                    requireView(),
                                    getString(R.string.cannot_override_color),
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setAction(getString(R.string.dismiss)) { }
                                    .show()
                                return@setAction
                            }

                            ColorPickerDialog()
                                .withCornerRadius(10f)
                                .withColor(v.tag as Int)
                                .withAlphaEnabled(false)
                                .withPicker(ImagePickerView::class.java)
                                .withListener { _: ColorPickerDialog?, color: Int ->
                                    if (v.tag as Int != color) {
                                        v.tag = color
                                        v.background.setTint(color)
                                        ((v as ViewGroup)
                                            .getChildAt(0) as TextView)
                                            .setTextColor(calculateTextColor(color))

                                        resetCustomStyleIfNotNull()
                                        putInt(systemPaletteNames[finalI][finalJ], color)

                                        CoroutineScope(Dispatchers.Main).launch {
                                            updateColorAppliedTimestamp()
                                            delay(200)
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    applyFabricatedColors()
                                                } catch (_: Exception) {
                                                }
                                            }
                                        }
                                    }
                                }
                                .show(
                                    getChildFragmentManager(),
                                    "overrideColorPicker$finalI$finalJ"
                                )
                        }
                        .show()
                }

                colorTableRows[i].getChildAt(j).setOnLongClickListener {
                    if (finalJ == 0 ||
                        finalJ == 12 ||
                        getInt(systemPaletteNames[finalI][finalJ], Int.MIN_VALUE) == Int.MIN_VALUE
                    ) {
                        return@setOnLongClickListener false
                    }

                    resetCustomStyleIfNotNull()
                    clearPref(systemPaletteNames[finalI][finalJ])

                    CoroutineScope(Dispatchers.Main).launch {
                        updateColorAppliedTimestamp()
                        withContext(Dispatchers.IO) {
                            try {
                                applyFabricatedColors()
                            } catch (_: Exception) {
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    private fun generateModifiedColors(): ArrayList<ArrayList<Int>> {
        return ColorUtil.generateModifiedColors(
            getCurrentMonetStyle(),
            getAccentSaturation(),
            getBackgroundSaturation(),
            getBackgroundLightness(),
            pitchBlackThemeEnabled(),
            accurateShadesEnabled()
        )
    }

    companion object {
        private val TAG: String = ColorPaletteFragment::class.java.getSimpleName()
        private val colorCodes: IntArray = intArrayOf(
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
        )
    }
}
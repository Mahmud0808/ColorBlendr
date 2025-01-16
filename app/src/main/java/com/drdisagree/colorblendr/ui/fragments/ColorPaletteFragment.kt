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
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS
import com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs.clearPref
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.databinding.FragmentColorPaletteBinding
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.getCurrentMonetStyle
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.utils.ColorUtil
import com.drdisagree.colorblendr.utils.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.ColorUtil.getSystemColors
import com.drdisagree.colorblendr.utils.ColorUtil.intToHexColor
import com.drdisagree.colorblendr.utils.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
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
    private val notShizukuMode: Boolean = workingMethod != Const.WorkMethod.SHIZUKU

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
        val isOverrideAvailable: Boolean = notShizukuMode &&
                getBoolean(MANUAL_OVERRIDE_COLORS, false)

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
            } catch (ignored: Exception) {
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
                val systemColors = generateModifiedColors() ?: getSystemColors()

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
                                text = colorCodes[j].toString()
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
                    val manualOverride: Boolean = getBoolean(MANUAL_OVERRIDE_COLORS, false)
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
                            if (!manualOverride || !notShizukuMode) {
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
                                            putLong(
                                                MONET_LAST_UPDATED,
                                                System.currentTimeMillis()
                                            )
                                            delay(200)
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    applyFabricatedColors(requireContext())
                                                } catch (ignored: Exception) {
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
                        getInt(systemPaletteNames[finalI][finalJ], Int.MIN_VALUE) != Int.MIN_VALUE
                    ) {
                        return@setOnLongClickListener true
                    }

                    resetCustomStyleIfNotNull()
                    clearPref(systemPaletteNames[finalI][finalJ])

                    CoroutineScope(Dispatchers.Main).launch {
                        putLong(
                            MONET_LAST_UPDATED,
                            System.currentTimeMillis()
                        )
                        withContext(Dispatchers.IO) {
                            try {
                                applyFabricatedColors(requireContext())
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    private fun generateModifiedColors(): ArrayList<ArrayList<Int>>? {
        try {
            return ColorUtil.generateModifiedColors(
                getCurrentMonetStyle(),
                getInt(MONET_ACCENT_SATURATION, 100),
                getInt(MONET_BACKGROUND_SATURATION, 100),
                getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                getBoolean(MONET_PITCH_BLACK_THEME, false),
                getBoolean(MONET_ACCURATE_SHADES, true)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating modified colors", e)
            return null
        }
    }

    companion object {
        private val TAG: String = ColorPaletteFragment::class.java.getSimpleName()
        private val colorCodes: IntArray = intArrayOf(
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
        )
    }
}
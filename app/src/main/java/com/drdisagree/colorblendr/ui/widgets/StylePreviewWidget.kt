package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.ui.views.ColorPreview
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.stringToEnumMonetStyle
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.app.SystemUtil.isDarkMode
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StylePreviewWidget : RelativeLayout {

    private var context: Context? = null
    private var container: MaterialCardView? = null
    private var titleTextView: TextView? = null
    private var descriptionTextView: TextView? = null
    private var colorContainer: ColorPreview? = null
    private var isSelected: Boolean = false
    private var onClickListener: OnClickListener? = null
    private var onLongClickListener: OnLongClickListener? = null
    private var styleName: String? = null
    private var colorPalette: ArrayList<ArrayList<Int>>? = null
    private var isCustomStyle: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.context = context
        inflate(context, R.layout.view_widget_style_preview, this)

        initializeId()

        context.obtainStyledAttributes(
            attrs,
            R.styleable.StylePreviewWidget
        ).apply {
            setTitle(getString(R.styleable.StylePreviewWidget_titleText))
            setDescription(getString(R.styleable.StylePreviewWidget_descriptionText))
            recycle()
        }

        coroutineScope.launch {
            setColorPreview()
        }

        container!!.setOnClickListener { v: View? ->
            if (onClickListener != null && !isSelected()) {
                setSelected(true)
                onClickListener!!.onClick(v)
            }
        }

        container!!.setOnLongClickListener { v: View? ->
            if (onLongClickListener != null) {
                onLongClickListener!!.onLongClick(v)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }
    }

    fun setTitle(titleResId: Int) {
        styleName = titleResId.getOriginalString()
        titleTextView!!.setText(titleResId)
    }

    fun setTitle(title: String?) {
        styleName = title
        titleTextView!!.text = title
    }

    fun setDescription(summaryResId: Int) {
        descriptionTextView!!.setText(summaryResId)
    }

    fun setDescription(summary: String?) {
        descriptionTextView!!.text = summary
    }

    // call after setting title
    fun setCustomColors(palette: ArrayList<ArrayList<Int>>) {
        isCustomStyle = true
        colorPalette = palette

        coroutineScope.launch {
            setColorPreview()
        }
    }

    fun resetCustomColors() {
        isCustomStyle = false
        colorPalette = null
    }

    override fun isSelected(): Boolean {
        return isSelected
    }

    override fun setSelected(isSelected: Boolean) {
        this.isSelected = isSelected
        container!!.setCardBackgroundColor(cardBackgroundColor)
        container!!.setStrokeWidth(if (isSelected) 2 else 0)
        titleTextView!!.setTextColor(getTextColor(isSelected))
        descriptionTextView!!.setTextColor(getTextColor(isSelected))
    }

    fun applyColorScheme() {
        updateColorAppliedTimestamp()

        coroutineScope.launch {
            applyFabricatedColors()
        }
    }

    suspend fun setColorPreview() {
        withContext(Dispatchers.IO) {
            if (!isCustomStyle || colorPalette == null) {
                try {
                    if (context == null || styleName == null) return@withContext

                    colorPalette = generateModifiedColors(
                        stringToEnumMonetStyle(context!!, styleName!!),
                        getAccentSaturation(),
                        getBackgroundSaturation(),
                        getBackgroundLightness(),
                        pitchBlackThemeEnabled(),
                        accurateShadesEnabled()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating color palette", e)
                    return@withContext
                }
            }

            withContext(Dispatchers.Main) {
                if (colorPalette != null) {
                    colorContainer?.apply {
                        setHalfCircleColor(colorPalette!![0][4])
                        setFirstQuarterCircleColor(colorPalette!![2][5])
                        setSecondQuarterCircleColor(colorPalette!![1][6])
                        setSquareColor(colorPalette!![4][if (!isDarkMode) 2 else 9])
                        invalidateColors()
                    }
                }
            }
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        onLongClickListener = listener
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        descriptionTextView = findViewById(R.id.summary)
        colorContainer = findViewById(R.id.color_container)

        container!!.setId(generateViewId())
        titleTextView!!.setId(generateViewId())
        descriptionTextView!!.setId(generateViewId())
        colorContainer!!.setId(generateViewId())

        val textContainer = findViewById<View>(R.id.text_container)
        val layoutParams: LayoutParams = textContainer.layoutParams as LayoutParams
        layoutParams.addRule(END_OF, colorContainer!!.id)
        textContainer.setLayoutParams(layoutParams)
    }

    @get:ColorInt
    private val cardBackgroundColor: Int
        get() {
            return if (isSelected()) MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorPrimaryContainer
            ) else MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorSurfaceContainer
            )
        }

    @ColorInt
    private fun getTextColor(isSelected: Boolean): Int {
        return if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnPrimaryContainer
        ) else MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorOnSurface
        )
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable? = super.onSaveInstanceState()

        val ss = SavedState(superState)
        ss.isSelected = isSelected()

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        setSelected(state.isSelected)

        coroutineScope.launch {
            setColorPreview()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) {
            titleTextView!!.setAlpha(1.0f)
            descriptionTextView!!.setAlpha(0.8f)
        } else {
            titleTextView!!.setAlpha(0.6f)
            descriptionTextView!!.setAlpha(0.4f)
        }

        container!!.setEnabled(enabled)
        titleTextView!!.setEnabled(enabled)
        descriptionTextView!!.setEnabled(enabled)
        colorContainer!!.setEnabled(enabled)
    }

    private class SavedState : BaseSavedState {
        var isSelected: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            isSelected = `in`.readBoolean()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeBoolean(isSelected)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    companion object {
        private val TAG: String = StylePreviewWidget::class.java.simpleName
    }
}

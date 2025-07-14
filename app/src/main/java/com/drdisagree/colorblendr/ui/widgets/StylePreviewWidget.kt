package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.ui.views.ColorPreview
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.app.MiscUtil.setCardCornerRadius
import com.drdisagree.colorblendr.utils.app.SystemUtil.isDarkMode
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    private var colorPalette: List<List<Int>>? = null
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

        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.StylePreviewWidget)
        setTitle(typedArray.getString(R.styleable.StylePreviewWidget_titleText))
        setDescription(typedArray.getString(R.styleable.StylePreviewWidget_descriptionText))
        val position = typedArray.getInt(R.styleable.StylePreviewWidget_position, 0)
        typedArray.recycle()

        container!!.setOnClickListener { v: View? ->
            if (onClickListener != null && !isSelected) {
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

        setCardCornerRadius(context, position, container!!, false)
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

    fun setPreviewColors(colorList: List<List<Int>>) {
        if (colorList.isEmpty()) return

        colorPalette = colorList

        colorContainer?.apply {
            setHalfCircleColor(colorList[0][4])
            setFirstQuarterCircleColor(colorList[2][5])
            setSecondQuarterCircleColor(colorList[1][6])
            setSquareColor(colorList[4][if (!isDarkMode) 2 else 9])
            invalidateColors()
        }
    }

    // call after setting title
    fun setCustomPreviewColors(palette: List<List<Int>>) {
        if (palette.isEmpty()) return

        isCustomStyle = true
        colorPalette = palette

        setPreviewColors(palette)
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
        container!!.strokeWidth = if (isSelected) 2 else 0
        titleTextView!!.setTextColor(getTextColor(isSelected))
        descriptionTextView!!.setTextColor(getTextColor(isSelected))
    }

    fun applyColorScheme() {
        updateColorAppliedTimestamp()

        coroutineScope.launch {
            applyFabricatedColors()
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
            return if (isSelected) MaterialColors.getColor(
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
        ss.isSelected = isSelected

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
            state.colorPalette?.let {
                setPreviewColors(it)
            }
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
        var colorPalette: ArrayList<ArrayList<Int>>? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            isSelected = `in`.readBoolean()

            val hasPalette = `in`.readInt() == 1
            if (hasPalette) {
                val outerSize = `in`.readInt()
                colorPalette = ArrayList(outerSize)
                repeat(outerSize) {
                    val innerSize = `in`.readInt()
                    val innerList = ArrayList<Int>(innerSize)
                    repeat(innerSize) {
                        innerList.add(`in`.readInt())
                    }
                    colorPalette!!.add(innerList)
                }
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)

            dest.writeBoolean(isSelected)

            if (colorPalette != null) {
                dest.writeInt(1)
                dest.writeInt(colorPalette!!.size)
                for (innerList in colorPalette!!) {
                    dest.writeInt(innerList.size)
                    for (value in innerList) {
                        dest.writeInt(value)
                    }
                }
            } else {
                dest.writeInt(0)
            }
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
}

package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.utils.SystemUtil.isDarkMode
import com.google.android.material.card.MaterialCardView

class ColorPickerWidget : RelativeLayout {

    private var container: MaterialCardView? = null
    private var titleTextView: TextView? = null
    private var summaryTextView: TextView? = null
    private var iconImageView: ImageView? = null
    private var colorView: View? = null

    @ColorInt
    private var selectedColor: Int = Color.WHITE

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
        inflate(context, R.layout.view_widget_colorpicker, this)

        initializeId()

        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ColorPickerWidget)
        setTitle(typedArray.getString(R.styleable.ColorPickerWidget_titleText))
        setSummary(typedArray.getString(R.styleable.ColorPickerWidget_summaryText))
        val icon: Int = typedArray.getResourceId(R.styleable.ColorPickerWidget_icon, 0)
        var iconSpaceReserved: Boolean =
            typedArray.getBoolean(R.styleable.SwitchWidget_iconSpaceReserved, false)
        val colorResId: Int =
            typedArray.getResourceId(R.styleable.ColorPickerWidget_previewColor, Int.MIN_VALUE)
        selectedColor = typedArray.getColor(R.styleable.ColorPickerWidget_previewColor, Color.WHITE)
        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView!!.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView!!.setVisibility(GONE)
        }

        if (colorResId != Int.MIN_VALUE) {
            previewColor = ContextCompat.getColor(getContext(), colorResId)
        }
    }

    fun setTitle(titleResId: Int) {
        titleTextView!!.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView!!.text = title
    }

    fun setSummary(summaryResId: Int) {
        summaryTextView!!.setText(summaryResId)
    }

    fun setSummary(summary: String?) {
        summaryTextView!!.text = summary
    }

    fun setIcon(icon: Int) {
        iconImageView!!.setImageResource(icon)
        iconImageView!!.setVisibility(VISIBLE)
    }

    fun setIcon(drawable: Drawable?) {
        iconImageView!!.setImageDrawable(drawable)
        iconImageView!!.setVisibility(VISIBLE)
    }

    fun setIconVisibility(visibility: Int) {
        iconImageView!!.setVisibility(visibility)
    }

    @get:ColorInt
    var previewColor: Int
        get() {
            return selectedColor
        }
        set(color) {
            var colorTemp: Int = color
            this.selectedColor = colorTemp

            if (!isEnabled) {
                colorTemp = if (isDarkMode) {
                    Color.DKGRAY
                } else {
                    Color.LTGRAY
                }
            }

            val drawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(colorTemp, colorTemp)
            )
            drawable.setShape(GradientDrawable.OVAL)
            colorView!!.background = drawable
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (enabled) {
            val typedValue = TypedValue()
            val a: TypedArray = context.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(com.google.android.material.R.attr.colorPrimary)
            )
            val color: Int = a.getColor(0, 0)
            a.recycle()

            iconImageView!!.setImageTintList(ColorStateList.valueOf(color))

            titleTextView!!.setAlpha(1.0f)
            summaryTextView!!.setAlpha(0.8f)
        } else {
            if (isDarkMode) {
                iconImageView!!.setImageTintList(ColorStateList.valueOf(Color.DKGRAY))
            } else {
                iconImageView!!.setImageTintList(ColorStateList.valueOf(Color.LTGRAY))
            }

            titleTextView!!.setAlpha(0.6f)
            summaryTextView!!.setAlpha(0.4f)
        }

        container!!.setEnabled(enabled)
        titleTextView!!.setEnabled(enabled)
        summaryTextView!!.setEnabled(enabled)
        iconImageView!!.setEnabled(enabled)
        previewColor = if (enabled) previewColor else Color.GRAY
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        iconImageView = findViewById(R.id.icon)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        colorView = findViewById(R.id.color_widget)

        container!!.setId(generateViewId())
        iconImageView!!.setId(generateViewId())
        titleTextView!!.setId(generateViewId())
        summaryTextView!!.setId(generateViewId())
        colorView!!.setId(generateViewId())

        val layoutParams: LayoutParams =
            findViewById<View>(R.id.text_container).layoutParams as LayoutParams
        layoutParams.addRule(START_OF, colorView!!.id)
        layoutParams.addRule(END_OF, iconImageView!!.id)
        findViewById<View>(R.id.text_container).setLayoutParams(layoutParams)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        container!!.setOnClickListener(l)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable? = super.onSaveInstanceState()

        val ss = SavedState(superState)
        ss.selectedColor = selectedColor

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        previewColor = state.selectedColor
    }

    private class SavedState : BaseSavedState {
        var selectedColor: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            selectedColor = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(selectedColor)
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

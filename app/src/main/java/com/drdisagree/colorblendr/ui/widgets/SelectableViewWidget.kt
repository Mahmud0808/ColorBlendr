package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.utils.app.SystemUtil.isDarkMode
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class SelectableViewWidget : RelativeLayout {

    private var container: MaterialCardView? = null
    private var titleTextView: TextView? = null
    private var descriptionTextView: TextView? = null
    private var iconImageView: ImageView? = null
    private var onClickListener: OnClickListener? = null

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
        inflate(context, R.layout.view_widget_selectable, this)

        initializeId()

        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SelectableViewWidget)
        setTitle(typedArray.getString(R.styleable.SelectableViewWidget_titleText))
        setDescription(typedArray.getString(R.styleable.SelectableViewWidget_descriptionText))
        isSelected = typedArray.getBoolean(R.styleable.SelectableViewWidget_isSelected, false)
        typedArray.recycle()

        container!!.setOnClickListener { v: View? ->
            if (onClickListener != null && !isSelected) {
                isSelected = true
                onClickListener!!.onClick(v)
            }
        }

        updateViewOnOrientation()
    }

    fun setTitle(titleResId: Int) {
        titleTextView!!.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView!!.text = title
    }

    fun setDescription(descriptionResId: Int) {
        descriptionTextView!!.setText(descriptionResId)
    }

    fun setDescription(description: String?) {
        descriptionTextView!!.text = description
    }

    override fun isSelected(): Boolean {
        return iconImageView!!.alpha == 1.0f
    }

    override fun setSelected(isSelected: Boolean) {
        iconImageView!!.alpha = if (isSelected) 1.0f else 0.2f
        iconImageView!!.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        iconImageView!!.setImageResource(if (isSelected) R.drawable.ic_checked_filled else R.drawable.ic_checked_outline)
        container!!.setCardBackgroundColor(cardBackgroundColor)
        container!!.strokeWidth = if (isSelected) 0 else 2
        titleTextView!!.setTextColor(getTextColor(isSelected))
        descriptionTextView!!.setTextColor(getTextColor(isSelected))
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (enabled) {
            val typedValue: TypedValue = TypedValue()
            val a: TypedArray = context.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(com.google.android.material.R.attr.colorPrimary)
            )
            val color: Int = a.getColor(0, 0)
            a.recycle()

            iconImageView!!.imageTintList = ColorStateList.valueOf(color)

            titleTextView!!.alpha = 1.0f
            descriptionTextView!!.alpha = 0.8f
        } else {
            if (isDarkMode) {
                iconImageView!!.imageTintList =
                    ColorStateList.valueOf(Color.DKGRAY)
            } else {
                iconImageView!!.imageTintList =
                    ColorStateList.valueOf(Color.LTGRAY)
            }

            titleTextView!!.alpha = 0.6f
            descriptionTextView!!.alpha = 0.4f
        }

        container!!.isEnabled = enabled
        iconImageView!!.isEnabled = enabled
        titleTextView!!.isEnabled = enabled
        descriptionTextView!!.isEnabled = enabled
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        iconImageView = findViewById(R.id.icon)
        titleTextView = findViewById(R.id.title)
        descriptionTextView = findViewById(R.id.description)

        container!!.setId(generateViewId())
        iconImageView!!.setId(generateViewId())
        titleTextView!!.setId(generateViewId())
        descriptionTextView!!.setId(generateViewId())
    }

    @get:ColorInt
    private val cardBackgroundColor: Int
        get() = if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimaryContainer
        ) else MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorSurfaceContainer
        )

    @get:ColorInt
    private val iconColor: Int
        get() {
            return if (isSelected) MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorPrimary
            ) else MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurface
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

    private fun updateViewOnOrientation() {
        val config: Configuration = appContext.resources.configuration
        val isLandscape: Boolean = config.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            val screenWidth: Int = resources.displayMetrics.widthPixels
            val screenHeight: Int = resources.displayMetrics.heightPixels

            val isSmallHeightDevice: Boolean = screenWidth >= screenHeight * 1.8

            if (isSmallHeightDevice) {
                container!!.minimumHeight = 0
                descriptionTextView!!.visibility = GONE
            }
        } else {
            val minHeightInDp: Int = 100
            val minHeightInPixels: Int =
                (minHeightInDp * context!!.resources.displayMetrics.density).toInt()
            container!!.minimumHeight = minHeightInPixels
            descriptionTextView!!.visibility = VISIBLE
        }
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

        isSelected = state.isSelected
        updateViewOnOrientation()
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
}

package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.utils.app.MiscUtil.setCardCornerRadius
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch

class SwitchWidget : RelativeLayout {

    private var context: Context? = null
    private var container: MaterialCardView? = null
    private var titleTextView: TextView? = null
    private var summaryTextView: TextView? = null
    private var iconImageView: ImageView? = null
    private var materialSwitch: MaterialSwitch? = null
    private var switchChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var beforeSwitchChangeListener: BeforeSwitchChangeListener? = null
    private var isMasterSwitch: Boolean = false
    private var summaryOnText: String? = null
    private var summaryOffText: String? = null

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
        inflate(context, R.layout.view_widget_switch, this)

        initializeId()

        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchWidget)
        setTitle(typedArray.getString(R.styleable.SwitchWidget_titleText))
        setSummary(typedArray.getString(R.styleable.SwitchWidget_summaryText))
        val icon: Int = typedArray.getResourceId(R.styleable.SwitchWidget_icon, 0)
        var iconSpaceReserved: Boolean =
            typedArray.getBoolean(R.styleable.SwitchWidget_iconSpaceReserved, false)
        isMasterSwitch = typedArray.getBoolean(R.styleable.SwitchWidget_isMasterSwitch, false)
        summaryOnText = typedArray.getString(R.styleable.SwitchWidget_summaryOnText)
        summaryOffText = typedArray.getString(R.styleable.SwitchWidget_summaryOffText)
        isSwitchChecked = typedArray.getBoolean(R.styleable.SwitchWidget_isChecked, false)
        val position = typedArray.getInt(R.styleable.SwitchWidget_position, 0)
        updateSummary()
        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView!!.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView!!.setVisibility(GONE)
        }

        container!!.setOnClickListener {
            if (materialSwitch!!.isEnabled) {
                materialSwitch!!.toggle()
            }
        }

        materialSwitch!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (materialSwitch!!.isEnabled) {
                if (beforeSwitchChangeListener != null) {
                    beforeSwitchChangeListener!!.beforeSwitchChanged()
                }

                updateSummary()
                if (switchChangeListener != null) {
                    switchChangeListener!!.onCheckedChanged(buttonView, isChecked)
                }
            }
        }

        if (isMasterSwitch) {
            container!!.radius = resources.getDimension(R.dimen.container_corner_radius_round)
            (container!!.layoutParams as MarginLayoutParams).bottomMargin =
                context.resources.getDimensionPixelSize(R.dimen.container_margin_bottom) * 2
        } else {
            setCardCornerRadius(context, position, container!!)
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

    var isSwitchChecked: Boolean
        get() {
            return materialSwitch!!.isChecked
        }
        set(isChecked) {
            materialSwitch!!.setChecked(isChecked)
            if (switchChangeListener != null) {
                switchChangeListener!!.onCheckedChanged(materialSwitch, isChecked)
            }
        }

    private fun updateSummary() {
        if (summaryOnText == null || summaryOffText == null) {
            return
        }

        val isChecked: Boolean = isSwitchChecked

        if (isChecked) {
            setSummary(summaryOnText)
        } else {
            setSummary(summaryOffText)
        }

        if (isMasterSwitch) {
            container!!.setCardBackgroundColor(getCardBackgroundColor(isChecked))
            iconImageView!!.setColorFilter(getIconColor(isChecked), PorterDuff.Mode.SRC_IN)
            titleTextView!!.setTextColor(getTextColor(isChecked))
            summaryTextView!!.setTextColor(getTextColor(isChecked))
        }
    }

    @ColorInt
    private fun getCardBackgroundColor(isSelected: Boolean): Int {
        return if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimaryContainer
        ) else ColorUtils.setAlphaComponent(
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer),
            64
        )
    }

    @ColorInt
    private fun getIconColor(isSelected: Boolean): Int {
        return if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimary
        ) else MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorOnSurface
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

    fun setSwitchChangeListener(listener: CompoundButton.OnCheckedChangeListener?) {
        switchChangeListener = listener
    }

    fun setBeforeSwitchChangeListener(listener: BeforeSwitchChangeListener?) {
        beforeSwitchChangeListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        val typedValue = TypedValue()
        val a: TypedArray = context!!.obtainStyledAttributes(
            typedValue.data,
            intArrayOf(com.google.android.material.R.attr.colorPrimary)
        )
        val color: Int = a.getColor(0, 0)
        a.recycle()

        iconImageView!!.setImageTintList(ColorStateList.valueOf(color))

        if (enabled) {
            titleTextView!!.setAlpha(1.0f)
            iconImageView!!.setAlpha(1.0f)
            summaryTextView!!.setAlpha(0.8f)
        } else {
            titleTextView!!.setAlpha(0.6f)
            iconImageView!!.setAlpha(0.4f)
            summaryTextView!!.setAlpha(0.4f)
        }

        container!!.setEnabled(enabled)
        iconImageView!!.setEnabled(enabled)
        titleTextView!!.setEnabled(enabled)
        summaryTextView!!.setEnabled(enabled)
        materialSwitch!!.setEnabled(enabled)
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        iconImageView = findViewById(R.id.icon)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        materialSwitch = findViewById(R.id.switch_widget)

        container!!.setId(generateViewId())
        iconImageView!!.setId(generateViewId())
        titleTextView!!.setId(generateViewId())
        summaryTextView!!.setId(generateViewId())
        materialSwitch!!.setId(generateViewId())

        val textContainer = findViewById<View>(R.id.text_container)
        val layoutParams: LayoutParams = textContainer.layoutParams as LayoutParams
        layoutParams.addRule(START_OF, materialSwitch!!.id)
        layoutParams.addRule(END_OF, iconImageView!!.id)
        textContainer.setLayoutParams(layoutParams)
    }

    interface BeforeSwitchChangeListener {
        fun beforeSwitchChanged()
    }
}

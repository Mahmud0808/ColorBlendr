package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.utils.app.MiscUtil.setCardCornerRadius
import com.drdisagree.colorblendr.utils.colors.ColorUtil.getErrorContainer
import com.drdisagree.colorblendr.utils.colors.ColorUtil.getOnErrorContainer
import com.drdisagree.colorblendr.utils.ui.RoundedBackgroundSpan
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class MenuWidget : RelativeLayout {

    private var container: MaterialCardView? = null
    private var titleTextView: TextView? = null
    private var summaryTextView: TextView? = null
    private var iconImageView: ImageView? = null
    private var endArrowImageView: ImageView? = null
    private var baseTitle: String? = null
    private var disabledReason: String? = null

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
        inflate(context, R.layout.view_widget_menu, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuWidget)
        setTitle(typedArray.getString(R.styleable.MenuWidget_titleText))
        setSummary(typedArray.getString(R.styleable.MenuWidget_summaryText))
        val icon = typedArray.getResourceId(R.styleable.MenuWidget_icon, 0)
        var iconSpaceReserved =
            typedArray.getBoolean(R.styleable.MenuWidget_iconSpaceReserved, false)
        val showEndArrow = typedArray.getBoolean(R.styleable.MenuWidget_showEndArrow, false)
        val position = typedArray.getInt(R.styleable.MenuWidget_position, 0)
        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView!!.setImageResource(icon)
            iconImageView!!.setImageTintList(ColorStateList.valueOf(getIconColor()))
        }

        if (!iconSpaceReserved) {
            iconImageView!!.visibility = GONE
        }

        if (showEndArrow) {
            endArrowImageView!!.visibility = VISIBLE
        }

        setCardCornerRadius(context, position, container!!)
    }

    fun setTitle(titleResId: Int) {
        setTitle(context?.getString(titleResId))
    }

    fun setTitle(title: String?) {
        this.baseTitle = title
        updateTitle()
    }

    private fun updateTitle() {
        if (baseTitle == null) return

        if (disabledReason.isNullOrEmpty()) {
            titleTextView!!.text = baseTitle
        } else {
            val badgeText = disabledReason!!
            val spannable = SpannableString("$baseTitle $badgeText")
            val start = baseTitle!!.length + 1
            val end = start + badgeText.length

            spannable.setSpan(
                RoundedBackgroundSpan(
                    backgroundColor = context.getErrorContainer(),
                    textColor = context.getOnErrorContainer()
                ),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            titleTextView!!.text = spannable
        }
    }

    fun setSummary(summaryResId: Int) {
        summaryTextView!!.setText(summaryResId)
    }

    fun setSummary(summary: String?) {
        summaryTextView!!.text = summary
    }

    fun setIcon(icon: Int) {
        iconImageView!!.setImageResource(icon)
        iconImageView!!.visibility = VISIBLE
    }

    fun setIcon(drawable: Drawable?) {
        iconImageView!!.setImageDrawable(drawable)
        iconImageView!!.visibility = VISIBLE
    }

    fun setIconVisibility(visibility: Int) {
        iconImageView!!.visibility = visibility
    }

    fun setEndArrowVisibility(visibility: Int) {
        endArrowImageView!!.visibility = visibility
    }

    fun setDisabledReason(reason: String?) {
        this.disabledReason = reason
        updateTitle()
    }

    fun setDisabledReason(reasonResId: Int) {
        setDisabledReason(if (reasonResId != 0) context?.getString(reasonResId) else null)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        container!!.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        container!!.setOnLongClickListener(l)
    }

    @ColorInt
    private fun getIconColor(): Int {
        val typedValue = TypedValue()
        val a: TypedArray = context!!.obtainStyledAttributes(
            typedValue.data,
            intArrayOf(com.google.android.material.R.attr.colorPrimaryVariant)
        )
        val color: Int = a.getColor(0, 0)
        a.recycle()
        return color
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        iconImageView!!.imageTintList = ColorStateList.valueOf(getIconColor())
        endArrowImageView!!.imageTintList = ColorStateList.valueOf(
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurface
            )
        )

        if (enabled) {
            titleTextView!!.alpha = 1.0f
            iconImageView!!.alpha = 1.0f
            endArrowImageView!!.alpha = 1.0f
            summaryTextView!!.alpha = 0.8f
        } else {
            titleTextView!!.alpha = 0.6f
            iconImageView!!.alpha = 0.4f
            endArrowImageView!!.alpha = 0.4f
            summaryTextView!!.alpha = 0.4f
        }

        container!!.isEnabled = enabled
        iconImageView!!.isEnabled = enabled
        titleTextView!!.setEnabled(enabled)
        summaryTextView!!.setEnabled(enabled)

        if (enabled) {
            disabledReason = null
            updateTitle()
        }
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        iconImageView = findViewById(R.id.icon)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        endArrowImageView = findViewById(R.id.end_arrow)

        container!!.setId(generateViewId())
        iconImageView!!.setId(generateViewId())
        titleTextView!!.setId(generateViewId())
        summaryTextView!!.setId(generateViewId())
        endArrowImageView!!.setId(generateViewId())

        val textContainer = findViewById<View>(R.id.text_container)
        val layoutParams = textContainer.layoutParams as LayoutParams
        layoutParams.addRule(START_OF, endArrowImageView!!.id)
        layoutParams.addRule(END_OF, iconImageView!!.id)
        textContainer.layoutParams = layoutParams
    }
}

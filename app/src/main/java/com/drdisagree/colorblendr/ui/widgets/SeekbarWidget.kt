package com.drdisagree.colorblendr.ui.widgets

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.utils.app.MiscUtil.setCardCornerRadius
import com.google.android.material.card.MaterialCardView
import java.text.DecimalFormat
import java.util.Objects

class SeekbarWidget : RelativeLayout {

    private var container: MaterialCardView? = null
    private var titleTextView: TextView? = null
    private var summaryTextView: TextView? = null
    private var seekBar: SeekBar? = null
    private var resetIcon: ImageView? = null
    private var valueFormat: String? = null
    private var defaultValue = 0
    private var outputScale = 1f
    private var isDecimalFormat = false
    private var decimalFormat: String? = "#.#"
    private var resetClickListener: OnLongClickListener? = null

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
        inflate(context, R.layout.view_widget_seekbar, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekbarWidget)
        valueFormat = typedArray.getString(R.styleable.SeekbarWidget_progressFormat)
        defaultValue =
            typedArray.getInt(R.styleable.SeekbarWidget_seekbarDefaultProgress, Int.MAX_VALUE)
        setTitle(typedArray.getString(R.styleable.SeekbarWidget_titleText))
        setSeekbarMinProgress(typedArray.getInt(R.styleable.SeekbarWidget_seekbarMinProgress, 0))
        setSeekbarMaxProgress(typedArray.getInt(R.styleable.SeekbarWidget_seekbarMaxProgress, 100))
        seekbarProgress = typedArray.getInt(
            R.styleable.SeekbarWidget_seekbarProgress,
            typedArray.getInt(R.styleable.SeekbarWidget_seekbarDefaultProgress, 50)
        )
        isDecimalFormat = typedArray.getBoolean(R.styleable.SeekbarWidget_isDecimalFormat, false)
        decimalFormat = typedArray.getString(R.styleable.SeekbarWidget_decimalFormat)
        outputScale = typedArray.getFloat(R.styleable.SeekbarWidget_outputScale, 1f)
        val position = typedArray.getInt(R.styleable.SeekbarWidget_position, 0)
        typedArray.recycle()

        if (valueFormat == null) {
            valueFormat = ""
        }

        if (decimalFormat == null) {
            decimalFormat = "#.#"
        }

        setSelectedProgress()
        handleResetVisibility()
        setOnSeekbarChangeListener(null)
        setResetClickListener(null)
        setCardCornerRadius(context, position, container!!)
    }

    fun setTitle(titleResId: Int) {
        titleTextView!!.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView!!.text = title
    }

    fun setSelectedProgress() {
        summaryTextView!!.text = if (valueFormat!!.isBlank() || valueFormat!!.isEmpty()) {
            context.getString(
                R.string.opt_selected1,
                (if (!isDecimalFormat) (seekBar!!.progress / outputScale).toInt() else DecimalFormat(
                    decimalFormat
                )
                    .format((seekBar!!.progress / outputScale).toDouble())).toString()
            )
        } else {
            context.getString(
                R.string.opt_selected1,
                context.getString(
                    R.string.opt_selected2,
                    if (!isDecimalFormat) seekBar!!.progress.toString()
                    else DecimalFormat(decimalFormat).format((seekBar!!.progress / outputScale).toDouble()),
                    valueFormat
                )
            )
        }
        handleResetVisibility()
    }

    var seekbarProgress: Int
        get() = seekBar!!.progress
        set(value) {
            seekBar!!.progress = value
            setSelectedProgress()
            handleResetVisibility()
        }

    fun setSeekbarMinProgress(value: Int) {
        seekBar!!.min = value
    }

    fun setSeekbarMaxProgress(value: Int) {
        seekBar!!.max = value
    }

    fun setIsDecimalFormat(isDecimalFormat: Boolean) {
        this.isDecimalFormat = isDecimalFormat
        setSelectedProgress()
    }

    fun setDecimalFormat(decimalFormat: String) {
        this.decimalFormat = Objects.requireNonNullElse(decimalFormat, "#.#")
        setSelectedProgress()
    }

    fun setOutputScale(scale: Float) {
        this.outputScale = scale
        setSelectedProgress()
    }

    fun setOnSeekbarChangeListener(listener: OnSeekBarChangeListener?) {
        seekBar!!.setOnSeekBarChangeListener(listener)
    }

    fun setResetClickListener(listener: OnLongClickListener?) {
        resetClickListener = listener

        resetIcon!!.setOnClickListener {
            if (defaultValue == Int.MAX_VALUE) {
                return@setOnClickListener
            }
            Toast.makeText(context, R.string.long_press_to_reset, Toast.LENGTH_SHORT).show()
        }

        resetIcon!!.setOnLongClickListener { v: View ->
            if (defaultValue == Int.MAX_VALUE) {
                return@setOnLongClickListener false
            }
            seekbarProgress = defaultValue
            handleResetVisibility()
            notifyOnResetClicked(v)
            true
        }
    }

    fun resetSeekbar() {
        resetIcon!!.performLongClick()
    }

    private fun notifyOnResetClicked(v: View) {
        if (resetClickListener != null) {
            resetClickListener!!.onLongClick(v)
        }
    }

    private fun handleResetVisibility() {
        if (defaultValue != Int.MAX_VALUE && seekBar!!.progress != defaultValue) {
            if (resetIcon?.visibility != VISIBLE) {
                resetIcon?.visibility = VISIBLE
            }
        } else {
            if (resetIcon?.visibility != GONE) {
                resetIcon?.visibility = GONE
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        container!!.isEnabled = enabled
        titleTextView!!.isEnabled = enabled
        summaryTextView!!.isEnabled = enabled
        resetIcon!!.isEnabled = enabled
        seekBar!!.isEnabled = enabled

        if (enabled) {
            titleTextView!!.alpha = 1.0f
            summaryTextView!!.alpha = 0.8f
        } else {
            titleTextView!!.alpha = 0.6f
            summaryTextView!!.alpha = 0.4f
        }
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        seekBar = findViewById(R.id.seekbar_widget)
        resetIcon = findViewById(R.id.reset)

        container!!.setId(generateViewId())
        titleTextView!!.setId(generateViewId())
        summaryTextView!!.setId(generateViewId())
        seekBar!!.setId(generateViewId())
        resetIcon!!.setId(generateViewId())
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState)
        ss.seekbarProgress = seekBar!!.progress

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        seekBar!!.progress = state.seekbarProgress
        setSelectedProgress()
        handleResetVisibility()
    }

    private class SavedState : BaseSavedState {
        var seekbarProgress: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            seekbarProgress = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeFloat(seekbarProgress.toFloat())
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

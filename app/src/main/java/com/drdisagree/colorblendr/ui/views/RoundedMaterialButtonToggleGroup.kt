package com.drdisagree.colorblendr.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.shape.ShapeAppearanceModel

class RoundedMaterialButtonToggleGroup : MaterialButtonToggleGroup {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        for (i in 0 until childCount) {
            val childView: View = getChildAt(i)
            if (childView is MaterialButton) {
                if (childView.getVisibility() == GONE) {
                    continue
                }

                val builder: ShapeAppearanceModel.Builder =
                    childView.shapeAppearanceModel.toBuilder()
                val radius: Float = 120 * resources.displayMetrics.density
                childView.shapeAppearanceModel = builder
                    .setTopLeftCornerSize(radius)
                    .setBottomLeftCornerSize(radius)
                    .setTopRightCornerSize(radius)
                    .setBottomRightCornerSize(radius).build()
            }
        }
    }
}

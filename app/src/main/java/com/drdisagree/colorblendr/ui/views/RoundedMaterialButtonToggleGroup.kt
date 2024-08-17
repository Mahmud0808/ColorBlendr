package com.drdisagree.colorblendr.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.shape.ShapeAppearanceModel;

public class RoundedMaterialButtonToggleGroup extends MaterialButtonToggleGroup {
    public RoundedMaterialButtonToggleGroup(@NonNull Context context) {
        super(context);
    }

    public RoundedMaterialButtonToggleGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedMaterialButtonToggleGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof MaterialButton button) {
                if (button.getVisibility() == View.GONE) {
                    continue;
                }

                ShapeAppearanceModel.Builder builder = button.getShapeAppearanceModel().toBuilder();
                float radius = 120 * getResources().getDisplayMetrics().density;
                button.setShapeAppearanceModel(
                        builder
                                .setTopLeftCornerSize(radius)
                                .setBottomLeftCornerSize(radius)
                                .setTopRightCornerSize(radius)
                                .setBottomRightCornerSize(radius).build()
                );
            }
        }
    }
}

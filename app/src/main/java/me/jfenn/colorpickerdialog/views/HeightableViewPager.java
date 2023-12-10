package me.jfenn.colorpickerdialog.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class HeightableViewPager extends ViewPager {

    public HeightableViewPager(@NonNull Context context) {
        super(context);
    }

    public HeightableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;

        PagerAdapter adapter = getAdapter();
        if (adapter != null && adapter instanceof Heightable)
            height = ((Heightable) adapter).getHeightAt(getCurrentItem(), widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (height <= 0) {
            View v = getChildAt(getCurrentItem());
            if (v != null) {
                v.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height = v.getMeasuredHeight();
            }
        }

        if (height <= 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View v = getChildAt(i);
                v.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height = Math.max(height, v.getMeasuredHeight());
            }
        }

        if (height > 0)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface Heightable {
        int getHeightAt(int position, int widthMeasureSpec, int heightMeasureSpec);
    }
}

package me.jfenn.colorpickerdialog.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
import me.jfenn.androidutils.DimenUtilsKt;

public class RoundedSquareImageView extends AppCompatImageView {

    private int radius;
    private Path path;
    private RectF rect;

    public RoundedSquareImageView(Context context) {
        super(context);
        init();
    }

    public RoundedSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundedSquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        radius = DimenUtilsKt.dpToPx(4);
        path = new Path();
        rect = new RectF(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredWidth();
        setMeasuredDimension(size, size);
        rect.set(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
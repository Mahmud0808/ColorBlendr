package me.jfenn.colorpickerdialog.views.picker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.jfenn.androidutils.DimenUtilsKt;
import me.jfenn.androidutils.anim.AnimatedInteger;
import me.jfenn.colorpickerdialog.R;
import me.jfenn.colorpickerdialog.utils.ColorUtils;

public class ImageColorPickerView extends PickerView<ImageColorPickerView.ImageState> {

    private Bitmap bitmap;
    private AnimatedInteger x, y;
    private int circleWidth;
    private int color;

    private ImageState restoreState;

    private Paint paint, fillPaint, strokePaint;
    private Matrix bitmapMatrix;

    public ImageColorPickerView(Context context) {
        super(context);
    }

    public ImageColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        setFocusable(true);
        setClickable(true);
        setWillNotDraw(false);

        x = new AnimatedInteger(-1);
        y = new AnimatedInteger(-1);

        circleWidth = DimenUtilsKt.dpToPx(18);

        paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(DimenUtilsKt.dpToPx(2));
        strokePaint.setAntiAlias(true);

        bitmapMatrix = new Matrix();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (bitmap != null)
                    calculateBitmapMatrix();

                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected ImageState newState(@Nullable Parcelable parcelable) {
        return new ImageState(parcelable);
    }

    @Override
    public int getColor() {
        return color;
    }

    @NonNull
    @Override
    public String getName() {
        return getContext().getString(R.string.colorPickerDialog_image);
    }

    /**
     * Specify a bitmap for the image picker to pick its color from.
     *
     * @param bitmap            The bitmap to pick a color from.
     * @return                  "This" view instance, for method chaining.
     */
    public ImageColorPickerView withBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;

        if (getWidth() > 0)
            calculateBitmapMatrix();

        requestLayout();

        return this;
    }

    private void calculateBitmapMatrix() {
        if (bitmap == null || getWidth() <= 0)
            return;

        float scale = (float) getWidth() / bitmap.getWidth();
        bitmapMatrix.reset();
        bitmapMatrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
        bitmapMatrix.postScale(scale, scale);
        bitmapMatrix.postTranslate(getWidth() / 2, (bitmap.getHeight() * scale) / 2);

        if (restoreState != null) {
            x.setCurrent((int) (restoreState.x * getWidth()));
            y.setCurrent((int) (restoreState.y * scale * bitmap.getHeight())); // work around view height's iffy existence
            onColorPicked(this, color);
        } else {
            x.setCurrent(-getWidth());
            y.setCurrent(-getWidth()); // this is stupid, but the view's height isn't reliable
        }

        postInvalidate();
    }

    private int getBitmapX(float x) {
        return (int) (x * bitmap.getWidth() / getWidth());
    }

    private int getBitmapY(float y) {
        return (int) (y * bitmap.getHeight() / getHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (x.val() >= 0 && y.val() > 0) {
            x.to((int) event.getX());
            y.to((int) event.getY());
        } else {
            x.setCurrent((int) event.getX());
            y.setCurrent((int) event.getY());
        }

        postInvalidate();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = getBitmapX(event.getX()), y = getBitmapY(event.getY());
            if (x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight()) {
                color = bitmap.getPixel(x, y);
                color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
                onColorPicked(this, color);
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (bitmap != null) {
            x.next(true, 50);
            y.next(true, 50);

            canvas.drawBitmap(bitmap, bitmapMatrix, paint);

            int x = getBitmapX(this.x.val()), y = getBitmapY(this.y.val());
            if (x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight()) {
                int color = bitmap.getPixel(x, y);
                color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));

                fillPaint.setColor(color);
                strokePaint.setColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);

                canvas.drawCircle(this.x.val(), this.y.val(), circleWidth, fillPaint);
                canvas.drawCircle(this.x.val(), this.y.val(), circleWidth, strokePaint);
            }

            if (!this.x.isTarget() || !this.y.isTarget())
                postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (bitmap != null) {
            int width = getMeasuredWidth();
            setMeasuredDimension(width, (int) (bitmap.getHeight() * ((float) width / bitmap.getWidth())));
        }
    }

    public static class ImageState extends SavedState<ImageColorPickerView> {

        private float x, y;
        private int color;

        protected ImageState(Parcelable superState) {
            super(superState);
        }

        protected ImageState(@Nullable Parcel in) {
            super(in);
            if (in != null) {
                x = in.readFloat();
                y = in.readFloat();
                color = in.readInt();
            }
        }

        @Override
        public SavedState<ImageColorPickerView> fromInstance(ImageColorPickerView view) {
            x = (float) view.x.getTarget() / view.getWidth();
            y = (float) view.y.getTarget() / view.getHeight();
            color = view.color;
            return super.fromInstance(view);
        }

        @Override
        public SavedState<ImageColorPickerView> toInstance(ImageColorPickerView view) {
            view.color = color;
            view.restoreState = this;
            return super.toInstance(view);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(x);
            dest.writeFloat(y);
            dest.writeInt(color);
        }
    }
}

package me.jfenn.colorpickerdialog.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.drdisagree.colorblendr.R;

import me.jfenn.androidutils.DimenUtilsKt;
import me.jfenn.colorpickerdialog.interfaces.ActivityRequestHandler;
import me.jfenn.colorpickerdialog.interfaces.ActivityResultHandler;
import me.jfenn.colorpickerdialog.interfaces.OnColorPickedListener;
import me.jfenn.colorpickerdialog.interfaces.PickerTheme;
import me.jfenn.colorpickerdialog.utils.ColorUtils;
import me.jfenn.colorpickerdialog.views.picker.PickerView;

abstract class PickerDialog<T extends PickerDialog> extends AppCompatDialogFragment implements OnColorPickedListener<PickerView>, ActivityRequestHandler, PickerTheme {

    private static final String INST_KEY_COLOR = "me.jfenn.colorpickerdialog.INST_KEY_COLOR";
    private static final String INST_KEY_TITLE = "me.jfenn.colorpickerdialog.INST_KEY_TITLE";
    private static final String INST_KEY_CORNER_RADIUS = "me.jfenn.colorpickerdialog.INST_KEY_CORNER_RADIUS";
    private static final String INST_KEY_RETAIN_INST = "me.jfenn.colorpickerdialog.INST_KEY_RETAIN_INST";
    private final SparseArray<ActivityResultHandler> resultHandlers;
    @ColorInt
    private int color = Color.BLACK;
    private String title;
    private int cornerRadius;
    private OnColorPickedListener<T> listener;

    public PickerDialog() {
        resultHandlers = new SparseArray<>();
        withTheme(R.style.ColorPickerDialog);
        withCornerRadius(2);
        setRetainInstance(true);
        init();
    }

    protected abstract void init();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getTitle());
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = window.getWindowManager();
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        window.setLayout(
                Math.min(DimenUtilsKt.dpToPx(displayMetrics.widthPixels > displayMetrics.heightPixels ? 800 : 500),
                        (int) (displayMetrics.widthPixels * 0.9f)),
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ColorUtils.fromAttr(new ContextThemeWrapper(getContext(), getTheme()),
                android.R.attr.colorBackground, Color.WHITE));
        drawable.setCornerRadius(cornerRadius);

        window.setBackgroundDrawable(new InsetDrawable(drawable, DimenUtilsKt.dpToPx(12)));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(INST_KEY_COLOR, color);
            title = savedInstanceState.getString(INST_KEY_TITLE, title);
            cornerRadius = savedInstanceState.getInt(INST_KEY_CORNER_RADIUS, cornerRadius);
            setRetainInstance(savedInstanceState.getBoolean(INST_KEY_RETAIN_INST, getRetainInstance()));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INST_KEY_COLOR, color);
        outState.putString(INST_KEY_TITLE, title);
        outState.putInt(INST_KEY_CORNER_RADIUS, cornerRadius);
        outState.putBoolean(INST_KEY_RETAIN_INST, getRetainInstance());
    }

    /**
     * Set whether the dialog should retain its current instance.
     * <p>
     * Defaults to true. If this is true, the dialog will be dismissed on
     * an orientation / config change - if not, it will be automatically
     * recreated by the system. The current color, pickers, and theme
     * options will be retained, but you will need to reconnect to this
     * fragment to re-apply an `OnColorPickedListener`.
     *
     * @param shouldRetainInstance Whether the dialog should retain its
     *                             current instance.
     * @return "This" dialog instance, for method
     * chaining.
     */
    public T withRetainInstance(boolean shouldRetainInstance) {
        setRetainInstance(shouldRetainInstance);
        return (T) this;
    }

    /**
     * Specify a listener to receive updates when a new color is selected.
     *
     * @param listener The listener to receive updates.
     * @return "This" dialog instance, for method chaining.
     */
    public T withListener(OnColorPickedListener<T> listener) {
        this.listener = listener;
        return (T) this;
    }

    /**
     * Specify an initial color for the picker to use.
     *
     * @param color The initial color int.
     * @return "This" dialog instance, for method chaining.
     */
    public T withColor(@ColorInt int color) {
        this.color = color;
        return (T) this;
    }

    @Override
    public void onColorPicked(@Nullable PickerView pickerView, @ColorInt int color) {
        this.color = color;
    }

    /**
     * Get the current color int selected by the picker.
     *
     * @return The current color of the picker.
     */
    @ColorInt
    public int getColor() {
        return color;
    }

    /**
     * Specify a title for the dialog. Passing "null" will set the dialog to
     * its default title.
     *
     * @param title The (string) title of the dialog.
     * @return "This" dialog instance, for method chaining.
     */
    public T withTitle(@Nullable String title) {
        this.title = title;
        return (T) this;
    }

    /**
     * Get the current title of the dialog; "null" if there is not one set.
     *
     * @return The title of the dialog.
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Specify a theme/style of the dialog. Defaults to @style/ColorPickerDialog
     *
     * @param style The style for the dialog to use.
     * @return "This" dialog instance, for method chaining.
     */
    public T withTheme(@StyleRes int style) {
        setStyle(DialogFragment.STYLE_NORMAL, style);
        return (T) this;
    }

    /**
     * Specify the corner radius for the dialog to use, in dp.
     *
     * @param cornerRadius The corner radius of the dialog, in dp.
     * @return "This" dialog instance, for method chaining.
     */
    public T withCornerRadius(float cornerRadius) {
        this.cornerRadius = DimenUtilsKt.dpToPx(cornerRadius);
        return (T) this;
    }

    /**
     * Specify the corner radius for the dialog to use, in px.
     *
     * @param cornerRadiusPx The corner radius of the dialog, in px.
     * @return "This" dialog instance, for method chaining.
     */
    public T withCornerRadiusPx(int cornerRadiusPx) {
        this.cornerRadius = cornerRadiusPx;
        return (T) this;
    }

    /**
     * Get the currently applied corner radius, in dp.
     *
     * @return The corner radius, in dp.
     */
    public float getCornerRadius() {
        return DimenUtilsKt.pxToDp(cornerRadius);
    }

    /**
     * Get the currently applied corner radius, in px.
     *
     * @return The corner radius, in px.
     */
    public int getCornerRadiusPx() {
        return cornerRadius;
    }

    protected void confirm() {
        if (listener != null)
            listener.onColorPicked(null, color);

        dismiss();
    }

    @Override
    public void handlePermissionsRequest(ActivityResultHandler resultHandler, String... permissions) {
        int code = resultHandlers.size();
        resultHandlers.put(code, resultHandler);
        requestPermissions(permissions, code);
    }

    @Override
    public void handleActivityRequest(ActivityResultHandler resultHandler, Intent intent) {
        int code = resultHandlers.size();
        resultHandlers.put(code, resultHandler);
        startActivityForResult(intent, code);
    }

    @Nullable
    @Override
    public FragmentManager requestFragmentManager() {
        return getChildFragmentManager();
    }

    /**
     * Initialize theme-related variables from a provided PickerTheme
     * interface. Coincidentally, this class also implements that
     * interface.
     *
     * @param theme The theme to initialize values from.
     * @return "This" dialog instance, for method chaining.
     */
    public T withPickerTheme(@Nullable PickerTheme theme) {
        if (theme == null)
            return (T) this;

        return (T) this.withTheme(theme.requestTheme())
                .withCornerRadiusPx(theme.requestCornerRadiusPx())
                .withRetainInstance(theme.requestRetainInstance());
    }

    @Nullable
    @Override
    public PickerTheme getPickerTheme() {
        return this;
    }

    @Override
    public int requestTheme() {
        return getTheme();
    }

    @Override
    public int requestCornerRadiusPx() {
        return cornerRadius;
    }

    @Override
    public boolean requestRetainInstance() {
        return getRetainInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ActivityResultHandler handler;
        if ((handler = resultHandlers.get(requestCode)) != null) {
            handler.onPermissionsResult(permissions, grantResults);
            resultHandlers.remove(requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ActivityResultHandler handler;
        if ((handler = resultHandlers.get(requestCode)) != null) {
            handler.onActivityResult(resultCode, data);
            resultHandlers.remove(requestCode);
        }
    }
}

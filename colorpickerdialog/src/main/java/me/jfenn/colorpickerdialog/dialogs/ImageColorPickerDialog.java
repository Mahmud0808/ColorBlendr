package me.jfenn.colorpickerdialog.dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import me.jfenn.colorpickerdialog.R;

import me.jfenn.colorpickerdialog.views.picker.ImageColorPickerView;

public class ImageColorPickerDialog extends PickerDialog<ImageColorPickerDialog> {

    private static final String INST_KEY_IMAGE_URI = "me.jfenn.colorpickerdialog.INST_KEY_IMAGE_URI";

    private String imageUriString;
    private Bitmap bitmap;

    private ImageColorPickerView pickerView;

    @SuppressWarnings("deprecation")
    private final SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            withBitmap(bitmap);
        }
    };

    @Override
    protected void init() {
        // nothing to initialize!
    }

    @Override
    public String getTitle() {
        String title = super.getTitle();
        return title != null ? title : getString(R.string.colorPickerDialog_imageColorPicker);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            imageUriString = savedInstanceState.getString(INST_KEY_IMAGE_URI, imageUriString);
            if (imageUriString != null)
                withImageUri(getContext(), Uri.parse(imageUriString));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INST_KEY_IMAGE_URI, imageUriString);
    }

    /**
     * Specify an image path to load the picker dialog's image from.
     *
     * @param path          The string path of the image to load.
     * @return              "This" dialog instance, for method chaining.
     */
    public ImageColorPickerDialog withImagePath(Context context, String path) {
        imageUriString = path;

        Glide.with(context)
                .asBitmap()
                .load(path)
                .into(target);

        return this;
    }

    /**
     * Specify an image uri to load the picker dialog's image from.
     *
     * @param imageUri      The string uri of the image to load.
     * @return              "This" dialog instance, for method chaining.
     */
    public ImageColorPickerDialog withImageUri(Context context, Uri imageUri) {
        if (imageUri.toString().startsWith("/"))
            return withImagePath(context, imageUri.toString());

        imageUriString = imageUri.toString();

        Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .into(target);

        return this;
    }

    /**
     * Specify an image bitmap to use as the picker dialog's image.
     *
     * @param bitmap        The bitmap image to use.
     * @return              "This" dialog instance, for method chaining.
     */
    public ImageColorPickerDialog withBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (pickerView != null)
            pickerView.withBitmap(bitmap);

        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.colorpicker_dialog_image_color_picker, container, false);

        pickerView = v.findViewById(R.id.image);
        pickerView.setListener(this);
        if (bitmap != null)
            pickerView.withBitmap(bitmap);

        v.findViewById(R.id.cancel).setOnClickListener(view -> dismiss());

        v.findViewById(R.id.confirm).setOnClickListener(view -> confirm());

        return v;
    }
}

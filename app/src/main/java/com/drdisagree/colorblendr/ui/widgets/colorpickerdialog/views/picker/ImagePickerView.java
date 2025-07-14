package com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.views.picker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.colorblendr.R;

import java.lang.ref.WeakReference;

import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.adapters.ImagePickerAdapter;
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.dialogs.ImageColorPickerDialog;
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.interfaces.ActivityResultHandler;
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.interfaces.OnColorPickedListener;

public class ImagePickerView extends PickerView implements ActivityResultHandler, ImagePickerAdapter.Listener {

    private static final String TAG_CHILD_IMAGE_PICKER = "colorPickerDialog_imagePicker";

    private int color;
    private View permissions, permissionsButton;
    private RecyclerView recycler;

    private ImageColorListener listener;

    public ImagePickerView(Context context) {
        super(context);
    }

    public ImagePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImagePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ImagePickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                FragmentManager manager = requestFragmentManager();
                if (manager != null) {
                    Fragment fragment = manager.findFragmentByTag(TAG_CHILD_IMAGE_PICKER);
                    if (fragment instanceof ImageColorPickerDialog)
                        ((ImageColorPickerDialog) fragment).withListener(listener);
                }
            }
        });
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.colorpicker_layout_image_picker, this);
        permissions = findViewById(R.id.permissions);
        permissionsButton = findViewById(R.id.permissionsButton);
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recycler.setHasFixedSize(true);

        permissionsButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                handlePermissionsRequest(
                        ImagePickerView.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
            } else {
                handlePermissionsRequest(
                        ImagePickerView.this,
                        Manifest.permission.READ_MEDIA_IMAGES
                );
            }
        });

        new Handler(Looper.getMainLooper()).post(() -> onPermissionsResult(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ?
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        } :
                        new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES
                        },
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ?
                        new int[]{
                                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE),
                                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } :
                        new int[]{
                                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                        }
        ));

        listener = new ImageColorListener(this);
    }

    @Override
    protected SavedState newState(@Nullable Parcelable parcelable) {
        return new SavedState(parcelable);
    }

    @Override
    public void onPermissionsResult(String[] permissions, int[] grantResults) {
        boolean isGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }

        if (isGranted) {
            this.permissions.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);

            recycler.setAdapter(new ImagePickerAdapter(getContext(), this, hasActivityRequestHandler()));
        } else {
            this.permissions.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        }
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

    @Override
    public void onRequestImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        handleActivityRequest(this, intent);
    }

    @Override
    public void setColor(int color, boolean animate) {
        super.setColor(color, animate);
        this.color = color;
    }

    @Override
    public void onImagePicked(Uri uri) {
        FragmentManager manager = requestFragmentManager();
        if (manager != null) {
            new ImageColorPickerDialog()
                    .withPickerTheme(getPickerTheme())
                    .withImageUri(getContext(), uri)
                    .withColor(color)
                    .withListener(listener)
                    .show(manager, "colorPickerDialog_imagePicker");
        }
    }

    @Override
    public void onColorPicked(@Nullable PickerView pickerView, int color) {
        this.color = color;
        super.onColorPicked(pickerView, color);
    }

    @Override
    public void onActivityResult(int resultCode, Intent data) {
        if (data != null && data.getData() != null)
            onImagePicked(data.getData());
        else if (data != null)
            Toast.makeText(getContext(), R.string.colorPickerDialog_msg_image_invalid, Toast.LENGTH_SHORT).show();
    }

    private static class ImageColorListener implements OnColorPickedListener<ImageColorPickerDialog> {

        private WeakReference<PickerView> reference;

        public ImageColorListener(PickerView pickerView) {
            reference = new WeakReference<>(pickerView);
        }

        @Override
        public void onColorPicked(@Nullable ImageColorPickerDialog pickerView, int color) {
            PickerView view = reference.get();
            if (view != null)
                view.onColorPicked(view, color);
        }
    }
}

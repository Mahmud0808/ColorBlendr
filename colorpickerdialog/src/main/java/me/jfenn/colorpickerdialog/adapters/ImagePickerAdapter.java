package me.jfenn.colorpickerdialog.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import me.jfenn.colorpickerdialog.R;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerAdapter extends RecyclerView.Adapter {

    private Listener listener;

    private List<String> images;
    private boolean hasRequestHandler;

    public ImagePickerAdapter(Context context, Listener listener, boolean hasRequestHandler) {
        this.listener = listener;
        this.hasRequestHandler = hasRequestHandler;
        images = getImagePaths(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.colorpicker_item_image_select, parent, false));
        else
            return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.colorpicker_item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder && hasRequestHandler) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onRequestImage();
            });
        } else if (holder instanceof ImageViewHolder imageHolder) {
            Glide.with(imageHolder.imageView.getContext().getApplicationContext())
                    .load(getItem(position))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageHolder.imageView);

            imageHolder.itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onImagePicked(Uri.parse(getItem(holder.getAdapterPosition())));
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && hasRequestHandler ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return images.size() + (hasRequestHandler ? 1 : 0);
    }

    private String getItem(int position) {
        return images.get(position - (hasRequestHandler ? 1 : 0));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }

    private static ArrayList<String> getImagePaths(Context context) {
        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME},
                null, null, null);

        if (cursor == null)
            return list;

        cursor.moveToFirst();

        int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            list.add(cursor.getString(index));
        }

        cursor.close();

        return list;
    }

    public interface Listener {
        void onRequestImage();

        void onImagePicked(Uri data);
    }

}

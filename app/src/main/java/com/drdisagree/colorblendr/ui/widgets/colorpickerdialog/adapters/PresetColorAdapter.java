package com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.colorblendr.R;

import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.interfaces.OnColorPickedListener;
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.views.SelectableCircleColorView;

public class PresetColorAdapter extends RecyclerView.Adapter<PresetColorAdapter.ViewHolder> {

    private int color;
    private int[] colors;
    private OnColorPickedListener<?> listener;

    public PresetColorAdapter(int... colors) {
        this.colors = colors;
    }

    public PresetColorAdapter withListener(OnColorPickedListener listener) {
        this.listener = listener;
        return this;
    }

    public void setPresets(int... colors) {
        this.colors = colors;
        notifyDataSetChanged();
    }

    @ColorInt
    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        for (int i = 0; i < colors.length; i++) {
            if (this.color == colors[i])
                notifyItemChanged(i);
        }

        this.color = color;

        for (int i = 0; i < colors.length; i++) {
            if (this.color == colors[i])
                notifyItemChanged(i);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.colorpicker_item_color, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.colorView.setColor(colors[position]);
        holder.colorView.setSelected(color == colors[position]);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColor(colors[holder.getAdapterPosition()]);
                if (listener != null) {
                    listener.onColorPicked(null, color);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors != null ? colors.length : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final SelectableCircleColorView colorView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color);
        }
    }
}

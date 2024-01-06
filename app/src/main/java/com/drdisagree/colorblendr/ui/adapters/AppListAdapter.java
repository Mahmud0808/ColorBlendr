package com.drdisagree.colorblendr.ui.adapters;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.ui.models.AppInfoModel;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private Context context;
    private final List<AppInfoModel> appList;
    private final HashMap<String, Boolean> selectedApps = new HashMap<>();

    public AppListAdapter(List<AppInfoModel> appList) {
        this.appList = appList;

        for (AppInfoModel appInfo : appList) {
            if (appInfo.isSelected()) {
                selectedApps.put(appInfo.packageName, appInfo.isSelected());
            }
        }

        Const.saveSelectedFabricatedApps(selectedApps);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_app_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfoModel appInfo = appList.get(position);
        holder.appIcon.setImageDrawable(appInfo.appIcon);
        holder.appName.setText(appInfo.appName);
        holder.packageName.setText(appInfo.packageName);

        holder.itemView.setOnClickListener(v -> {
            final boolean isSelected = appInfo.isSelected();
            setSelected(holder, !isSelected);
            appInfo.setSelected(!isSelected);

            selectedApps.put(appInfo.packageName, !isSelected);
            Const.saveSelectedFabricatedApps(selectedApps);

            if (isSelected) {
                OverlayManager.unregisterFabricatedOverlay(String.format(FABRICATED_OVERLAY_NAME_APPS, appInfo.packageName));
            } else {
                OverlayManager.applyFabricatedColorsPerApp(context, appInfo.packageName, null);
            }
        });

        setSelected(holder, appInfo.isSelected());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        setSelected(holder, appList.get(holder.getBindingAdapterPosition()).isSelected());
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public MaterialCardView container;
        public ImageView appIcon;
        public TextView appName;
        public TextView packageName;
        public ImageView iconView;

        public ViewHolder(View view) {
            super(view);

            container = view.findViewById(R.id.container);
            appIcon = view.findViewById(R.id.app_icon);
            appName = view.findViewById(R.id.title);
            packageName = view.findViewById(R.id.summary);
            iconView = view.findViewById(R.id.icon);
        }
    }

    private void setSelected(ViewHolder holder, boolean isSelected) {
        holder.iconView.setAlpha(isSelected ? 1.0f : 0.2f);
        holder.iconView.setColorFilter(getIconColor(isSelected), PorterDuff.Mode.SRC_IN);
        holder.iconView.setImageResource(isSelected ? R.drawable.ic_checked_filled : R.drawable.ic_checked_outline);
        holder.container.setCardBackgroundColor(getCardBackgroundColor(isSelected));
        holder.container.setStrokeWidth(isSelected ? 0 : 2);
        holder.appName.setTextColor(getTextColor(isSelected));
        holder.packageName.setTextColor(getTextColor(isSelected));

        if (holder.getBindingAdapterPosition() == 0) {
            ((ViewGroup.MarginLayoutParams) holder.container.getLayoutParams()).topMargin = (int) (72 * context.getResources().getDisplayMetrics().density);
        } else {
            ((ViewGroup.MarginLayoutParams) holder.container.getLayoutParams()).topMargin = 0;
        }
    }

    private @ColorInt int getCardBackgroundColor(boolean isSelected) {
        return isSelected ?
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorPrimaryContainer) :
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorSurfaceContainer);
    }

    private @ColorInt int getIconColor(boolean isSelected) {
        return isSelected ?
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorPrimary) :
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorOnSurface);
    }

    private @ColorInt int getTextColor(boolean isSelected) {
        return isSelected ?
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorOnPrimaryContainer) :
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorOnSurface);
    }
}

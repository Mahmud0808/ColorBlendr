package com.drdisagree.colorblendr.ui.models;

import android.graphics.drawable.Drawable;

public class AppInfoModel {

    public String appName;
    public String packageName;
    public Drawable appIcon;
    private boolean isSelected = false;

    public AppInfoModel(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

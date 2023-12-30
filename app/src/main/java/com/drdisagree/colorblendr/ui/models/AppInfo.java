package com.drdisagree.colorblendr.ui.models;

import android.graphics.drawable.Drawable;

public class AppInfo {

    public String appName;
    public String packageName;
    public Drawable appIcon;
    private boolean isSelected = false;

    public AppInfo(String appName, String packageName, Drawable appIcon) {
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

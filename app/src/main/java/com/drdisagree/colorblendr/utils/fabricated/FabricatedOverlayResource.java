package com.drdisagree.colorblendr.utils.fabricated;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_SOURCE_PACKAGE;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.TypedValue;

import java.util.HashMap;
import java.util.Map;

public class FabricatedOverlayResource implements Parcelable {

    public final String overlayName;
    public final String targetPackage;
    public final String sourcePackage;

    public Map<String, FabricatedOverlayEntry> entries = new HashMap<>();

    public FabricatedOverlayResource(String overlayName, String targetPackage) {
        this(overlayName, targetPackage, FABRICATED_OVERLAY_SOURCE_PACKAGE);
    }

    public FabricatedOverlayResource(String overlayName, String targetPackage, String sourcePackage) {
        this.overlayName = overlayName;
        this.targetPackage = targetPackage;
        this.sourcePackage = sourcePackage;
    }

    public void setInteger(String name, int value) {
        this.setInteger(name, value, null);
    }

    public void setInteger(String name, int value, String configuration) {
        String formattedName = formatName(name, "integer");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_INT_DEC, value, configuration));
    }

    public void setBoolean(String name, boolean value) {
        this.setBoolean(name, value, null);
    }

    public void setBoolean(String name, boolean value, String configuration) {
        String formattedName = formatName(name, "bool");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_INT_BOOLEAN, value ? 1 : 0, configuration));
    }

    public void setDimension(String name, int value) {
        this.setDimension(name, value, null);
    }

    public void setDimension(String name, int value, String configuration) {
        String formattedName = formatName(name, "dimen");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_DIMENSION, value, configuration));
    }

    public void setAttribute(String name, int value) {
        this.setAttribute(name, value, null);
    }

    public void setAttribute(String name, int value, String configuration) {
        String formattedName = formatName(name, "attr");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_ATTRIBUTE, value, configuration));
    }

    public void setColor(String name, int value) {
        this.setColor(name, value, null);
    }

    public void setColor(String name, int value, String configuration) {
        String formattedName = formatName(name, "color");
        entries.put(formattedName, new FabricatedOverlayEntry(formattedName, TypedValue.TYPE_INT_COLOR_ARGB8, value, configuration));
    }

    public Map<String, FabricatedOverlayEntry> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, FabricatedOverlayEntry> entries) {
        this.entries = entries;
    }

    private String formatName(String name, String type) {
        if (name.contains(":") && name.contains("/")) {
            return name;
        } else {
            return targetPackage + ":" + type + "/" + name;
        }
    }

    protected FabricatedOverlayResource(Parcel in) {
        overlayName = in.readString();
        targetPackage = in.readString();
        sourcePackage = in.readString();
        in.readMap(entries, FabricatedOverlayEntry.class.getClassLoader());
    }

    public static final Creator<FabricatedOverlayResource> CREATOR = new Creator<>() {
        @Override
        public FabricatedOverlayResource createFromParcel(Parcel in) {
            return new FabricatedOverlayResource(in);
        }

        @Override
        public FabricatedOverlayResource[] newArray(int size) {
            return new FabricatedOverlayResource[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(overlayName);
        dest.writeString(targetPackage);
        dest.writeString(sourcePackage);
        dest.writeMap(entries);
    }
}

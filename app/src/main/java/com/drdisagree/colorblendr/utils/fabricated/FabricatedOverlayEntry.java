package com.drdisagree.colorblendr.utils.fabricated;

import android.os.Parcel;
import android.os.Parcelable;

public class FabricatedOverlayEntry implements Parcelable {

    private String resourceName;
    private int resourceType;
    private int resourceValue;

    public FabricatedOverlayEntry(String resourceName, int resourceType, int resourceValue) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceValue = resourceValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public int getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(int resourceValue) {
        this.resourceValue = resourceValue;
    }

    protected FabricatedOverlayEntry(Parcel in) {
        resourceName = in.readString();
        resourceType = in.readInt();
        resourceValue = in.readInt();
    }

    public static final Creator<FabricatedOverlayEntry> CREATOR = new Creator<FabricatedOverlayEntry>() {
        @Override
        public FabricatedOverlayEntry createFromParcel(Parcel in) {
            return new FabricatedOverlayEntry(in);
        }

        @Override
        public FabricatedOverlayEntry[] newArray(int size) {
            return new FabricatedOverlayEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resourceName);
        dest.writeInt(resourceType);
        dest.writeInt(resourceValue);
    }
}

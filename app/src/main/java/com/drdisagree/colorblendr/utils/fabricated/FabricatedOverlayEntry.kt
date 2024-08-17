package com.drdisagree.colorblendr.utils.fabricated;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

public class FabricatedOverlayEntry implements Parcelable {

    private String resourceName;
    private int resourceType;
    private int resourceValue;
    private String configuration;

    public FabricatedOverlayEntry(String resourceName, int resourceType, int resourceValue) {
        this(resourceName, resourceType, resourceValue, null);
    }

    public FabricatedOverlayEntry(String resourceName, int resourceType, int resourceValue, String configuration) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceValue = resourceValue;
        this.configuration = configuration;
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

    public String getConfiguration() {
        return
                Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ?
                        null :
                        configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    protected FabricatedOverlayEntry(Parcel in) {
        resourceName = in.readString();
        resourceType = in.readInt();
        resourceValue = in.readInt();
        configuration = in.readString();
    }

    public static final Creator<FabricatedOverlayEntry> CREATOR = new Creator<>() {
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
        dest.writeString(configuration);
    }
}

package com.drdisagree.colorblendr.extension;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Interface for running a method with root.
 */
public abstract class MethodInterface implements Parcelable {

    public abstract void run();

    public static final Parcelable.Creator<MethodInterface> CREATOR = new Parcelable.Creator<>() {
        @Override
        public MethodInterface createFromParcel(Parcel source) {
            return new MethodInterface() {
                @Override
                public void run() {
                    // Do nothing
                }
            };
        }

        @Override
        public MethodInterface[] newArray(int size) {
            return new MethodInterface[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}

package android.app;

import android.os.Parcel;
import android.os.Parcelable;

public class ProfilerInfo implements Parcelable {

    public static final Creator<ProfilerInfo> CREATOR = new Creator<ProfilerInfo>() {
        @Override
        public ProfilerInfo createFromParcel(Parcel in) {
            return new ProfilerInfo(in);
        }

        @Override
        public ProfilerInfo[] newArray(int size) {
            return new ProfilerInfo[size];
        }
    };

    protected ProfilerInfo(Parcel in) {
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        throw new RuntimeException("Stub!");
    }
}
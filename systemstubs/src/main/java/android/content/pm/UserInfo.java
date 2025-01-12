package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import java.util.List;

public class UserInfo implements Parcelable {

    private UserInfo(@NonNull Parcel in) {
        throw new RuntimeException("Stub!");
    }

    public boolean isProfile() {
        throw new RuntimeException("Stub!");
    }

    public UserHandle getUserHandle() {
        throw new RuntimeException("Stub!");
    }

    public List<UserInfo> getProfiles(int userId, boolean enabledOnly) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static final @NonNull Parcelable.Creator<UserInfo> CREATOR =
            new Parcelable.Creator<UserInfo>() {
                @Override
                public UserInfo createFromParcel(Parcel source) {
                    return new UserInfo(source);
                }

                @Override
                public UserInfo[] newArray(int size) {
                    return new UserInfo[size];
                }
            };
}
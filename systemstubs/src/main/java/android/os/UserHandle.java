package android.os;

import androidx.annotation.NonNull;

public class UserHandle implements Parcelable {

    private UserHandle(@NonNull Parcel in) {
        throw new RuntimeException("Stub!");
    }

    public static UserHandle of(int userId) {
        throw new RuntimeException("Stub!");
    }

    public int getIdentifier() {
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

    public static final @NonNull Creator<UserHandle> CREATOR =
            new Creator<UserHandle>() {
                @Override
                public UserHandle createFromParcel(Parcel source) {
                    return UserHandle.of(source.readInt());
                }

                @Override
                public UserHandle[] newArray(int size) {
                    return new UserHandle[size];
                }
            };
}
package io.github.fbmediahack.quiethome.model;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class User {
    @NonNull
    public String userId;
    @NonNull
    public String displayName;

    public User() {
    }

    public User(@NonNull String userId, @NonNull String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public static User fromFirebaseUser(@NonNull final FirebaseUser user) {
        return new User(
                user.getUid(),
                user.getDisplayName() == null ? "<unknown>" : user.getDisplayName());
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}

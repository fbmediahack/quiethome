package io.github.fbmediahack.quiethome.model;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class User {
    public String userId;
    public String displayName;
    public boolean isSleeping = false;

    public User() {
    }

    public User(@NonNull String userId, @NonNull String displayName, boolean isSleeping) {
        this.userId = userId;
        this.displayName = displayName;
        this.isSleeping = isSleeping;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isSleeping=" + isSleeping +
                '}';
    }

    public static User fromFirebaseUser(@NonNull final FirebaseUser user) {
        return new User(
                user.getUid(),
                user.getDisplayName() == null ? "<unknown>" : user.getDisplayName(),
                false);
    }

    public static User fromFirebaseUser(FirebaseUser user, boolean b) {
        User user1 = fromFirebaseUser(user);
        user1.isSleeping = b;
        return user1;
    }
}

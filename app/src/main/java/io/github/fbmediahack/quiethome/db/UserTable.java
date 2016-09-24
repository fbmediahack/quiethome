package io.github.fbmediahack.quiethome.db;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import io.github.fbmediahack.quiethome.model.User;

public class UserTable {
    private final DatabaseReference ref;
    private static final String PATH = "users";

    public UserTable(@NonNull final DatabaseReference reference) {
        this.ref = reference;
    }

    @NonNull
    public Task<Void> insert(@NonNull final User user) {
        return ref.child(PATH).child(user.userId).setValue(user);
    }

    public Query getAllUsersQuery() {
        return ref.child(PATH);
    }
}

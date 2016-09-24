package io.github.fbmediahack.quiethome.db;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import io.github.fbmediahack.quiethome.model.User;
import rx.Observable;
import rx.Subscriber;

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

    public Observable<User> getAllUsers() {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(final Subscriber<? super User> subscriber) {
                ref.child(PATH).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        subscriber.onNext(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}

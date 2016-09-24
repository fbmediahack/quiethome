package io.github.fbmediahack.quiethome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.github.fbmediahack.quiethome.db.UserTable;
import io.github.fbmediahack.quiethome.model.User;

public class StartupActivity extends Activity {

    private static final int RC_SIGN_IN = 1;
    private DatabaseReference db = null;
    private UserTable userTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.userTable = new UserTable(FirebaseDatabase.getInstance().getReference());
        ensureUserIsLoggedIn();
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void ensureUserIsLoggedIn() {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startLoginFlow();
        } else {
            updateDatabaseUserRecord(currentUser);
            startMain();
        }
    }

    private void updateDatabaseUserRecord(@NonNull final FirebaseUser currentUser) {
        userTable.insert(User.fromFirebaseUser(currentUser));
    }

    private void startLoginFlow() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(AuthUI.GOOGLE_PROVIDER)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                ensureUserIsLoggedIn();
            }
        }
    }
}

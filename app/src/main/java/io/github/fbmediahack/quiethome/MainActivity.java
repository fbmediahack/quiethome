package io.github.fbmediahack.quiethome;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.pwittchen.reactivebeacons.library.Beacon;
import com.github.pwittchen.reactivebeacons.library.Filter;
import com.github.pwittchen.reactivebeacons.library.Proximity;
import com.github.pwittchen.reactivebeacons.library.ReactiveBeacons;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private AudioDetector ad = null;
    private FirebaseUser user = null;

    private String [] permissions = {
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        Snackbar.make(fab, "Welcome to Quiet Home, " + user.getDisplayName() + "!", Snackbar.LENGTH_SHORT)
                .show();

        final Context context = this;

        prepareReactiveBeacons()
                .observe()
                .filter(Filter.hasMacAddress("66:29:22:2A:70:4A"))
                .filter(Filter.proximityIsEqualTo(Proximity.NEAR))
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Beacon>() {
                    @Override
                    public void call(Beacon beacon) {
                        Toast.makeText(
                                context,
                                "Connected to " + beacon.macAddress,
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private ReactiveBeacons prepareReactiveBeacons() {
        ReactiveBeacons reactiveBeacons = new ReactiveBeacons(this);
        if (!reactiveBeacons.isBleSupported()) {
            Toast.makeText(this, "BLE is not supported on this device", Toast.LENGTH_SHORT).show();
        }

        if (!reactiveBeacons.isBluetoothEnabled()) {
            reactiveBeacons.requestBluetoothAccess(this);
        } else if (!reactiveBeacons.isLocationEnabled(this)) {
            reactiveBeacons.requestLocationAccess(this);
        }
        return reactiveBeacons;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.startDetectingAudio();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopDetectingAudio();
    }

    private void startDetectingAudio() {
        if (ad == null) {
            ad = new AudioDetector(getApplicationContext());
        }

        String permission = "android.permission.RECORD_AUDIO";
        int res = checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED) {
            ad.start();
        } else {
            int requestCode = 200;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, requestCode);
            }
        }
    }

    private void stopDetectingAudio() {
        if (ad != null) {
            ad.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    ad.start();
                }
                break;
        }
    }

}

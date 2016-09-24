package io.github.fbmediahack.quiethome;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AudioDetector.NoiseListener {

    private AudioDetector ad = null;
    private FirebaseUser user = null;

    private String [] permissions = {
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private BeaconManager beaconManager;
    private AsyncTask mLightBulbAlarmAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), android.R.color.background_dark, getTheme()));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLightBulbAlarmAsyncTask == null) {
                    mLightBulbAlarmAsyncTask = new LightBulbAlarmAsyncTask().execute();
                    view.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), android.R.color.holo_red_dark, getTheme()));
                } else {
                    mLightBulbAlarmAsyncTask.cancel(false);
                    mLightBulbAlarmAsyncTask = null;
                    view.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), android.R.color.background_dark, getTheme()));
                }
            }
        });

        Snackbar.make(fab, "Welcome to Quiet Home, " + user.getDisplayName() + "!", Snackbar.LENGTH_SHORT)
                .show();

        beaconManager = new BeaconManager(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 32817, 20843));
            }
        });
        beaconManager.setBackgroundScanPeriod(1000, 0);
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> beacons) {
                toolbar.setTitle("YOU'RE AT HOME!");
                showAtHomeView();
            }
            @Override
            public void onExitedRegion(Region region) {
                toolbar.setTitle("YOU'RE OUT OF HOME");
                showOutOfHomeView();
            }
        });
    }

    private void showAtHomeView() {
        // TODO: Do stuff
    }

    private void showOutOfHomeView() {
        // TODO: Do stuff
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

        if (mLightBulbAlarmAsyncTask != null) {
            mLightBulbAlarmAsyncTask.cancel(false);
        }
    }

    private void startDetectingAudio() {
        if (ad == null) {
            ad = new AudioDetector(getApplicationContext(), this);
        }

        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
        else {
            ad.start();
        }
    }

    private void stopDetectingAudio() {
        if (ad != null) {
            ad.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
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

    @Override
    public void onNoiceDetected() {
        // TODO: Notify Light
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                mLightBulbAlarmAsyncTask = new LightBulbAlarmAsyncTask().execute();
                fab.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), android.R.color.holo_red_dark, getTheme()));
            }
        });

        // TODO: Notify user , play sound
    }
}

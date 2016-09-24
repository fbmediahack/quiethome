package io.github.fbmediahack.quiethome;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.matteo.firebase_recycleview.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.fbmediahack.quiethome.adapter.UserRecyclerAdapter;
import io.github.fbmediahack.quiethome.db.UserTable;
import io.github.fbmediahack.quiethome.model.User;

public class MainActivity extends AppCompatActivity implements AudioDetector.NoiseListener {
    private static final int SLEEPING_MODE = 0x01;
    private static final int AWAKE_MODE = 0x02;

    private AudioDetector ad = null;
    private FirebaseUser user = null;

    private String [] permissions = {
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private BeaconManager beaconManager;
    private AsyncTask mLightBulbAlarmAsyncTask;
    private Toolbar mToolbar;
    private boolean mAtHome = false;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter mAdapter;

    private int mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecycler = (RecyclerView) findViewById(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);
        final Query query = new UserTable(FirebaseDatabase.getInstance().getReference())
                .getAllUsersQuery();
        mAdapter = new UserRecyclerAdapter(query, User.class, new ArrayList<User>(), new ArrayList<String>());
        mRecycler.setAdapter(mAdapter);

        mMode = AWAKE_MODE;

        setSupportActionBar(mToolbar);

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
                showAtHomeView(false);
            }
            @Override
            public void onExitedRegion(Region region) {
                showOutOfHomeView(false);
            }
        });
    }

    private void showAtHomeView(boolean manual) {
        mAtHome = true;
        if (manual) {
            mToolbar.setTitle("[YOU'RE AT HOME!]");
        } else {
            mToolbar.setTitle("YOU'RE AT HOME!");
        }
        // TODO: Do stuff
    }

    private void showOutOfHomeView(boolean manual) {
        mAtHome = false;
        if (manual) {
            mToolbar.setTitle("[YOU'RE OUT OF HOME]");
        } else {
            mToolbar.setTitle("YOU'RE OUT OF HOME");
        }
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

        if (id == R.id.action_toggle) {
            if (mAtHome) {
                showOutOfHomeView(true);
            } else {
                showAtHomeView(true);
            }
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
            new UserTable(FirebaseDatabase.getInstance().getReference()).insert(User.fromFirebaseUser(user, true));
            ad.start();
            mMode = SLEEPING_MODE;
        }
    }

    private void stopDetectingAudio() {
        if (ad != null) {
            ad.stop();
            new UserTable(FirebaseDatabase.getInstance().getReference()).insert(User.fromFirebaseUser(user, false));
            mMode = AWAKE_MODE;
        }
    }

    public boolean isAwake() {
        return mMode == AWAKE_MODE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        if(ad != null) {
            ad.start();
            new UserTable(FirebaseDatabase.getInstance().getReference()).insert(User.fromFirebaseUser(user, true));
            mMode = SLEEPING_MODE;
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

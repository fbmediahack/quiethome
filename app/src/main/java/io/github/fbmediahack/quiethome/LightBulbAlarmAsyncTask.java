package io.github.fbmediahack.quiethome;

import android.os.AsyncTask;
import android.util.Log;

import java.net.UnknownHostException;

import de.toman.milight.WiFiBox;

/**
 * Created by monchote on 24/09/2016.
 */

public class LightBulbAlarmAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final int BRIGHTNESS_INCREMENT = 6;
    private static final int BRIGHTNESS_SLEEP = WiFiBox.DEFAULT_SLEEP_BETWEEN_MESSAGES * 4;
    private WiFiBox mWifiBox;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            mWifiBox = new WiFiBox("192.168.0.125");
        } catch (UnknownHostException e) {
            Log.e(this.getClass().getName(), e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            mWifiBox.on();
            Thread.sleep(WiFiBox.DEFAULT_SLEEP_BETWEEN_MESSAGES);
            mWifiBox.color(0xA8);
            while (!isCancelled()) {
                for (int i = 0; i <= WiFiBox.MAX_BRIGHTNESS; i += BRIGHTNESS_INCREMENT) {
                    if (isCancelled()) { break; }
                    Thread.sleep(BRIGHTNESS_SLEEP);
                    Log.d("Brightness", i + "");
                    mWifiBox.brightness(i);
                }
                for (int i = WiFiBox.MAX_BRIGHTNESS; i >= 0; i -= BRIGHTNESS_INCREMENT) {
                    if (isCancelled()) { break; }
                    Thread.sleep(BRIGHTNESS_SLEEP);
                    Log.d("Brightness", i + "");
                    mWifiBox.brightness(i);
                }
            }
            Thread.sleep(WiFiBox.DEFAULT_SLEEP_BETWEEN_MESSAGES);
            mWifiBox.off();
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getLocalizedMessage(), e);
        }
        return null;
    }
}

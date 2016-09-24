package io.github.fbmediahack.quiethome;

/**
 * Created by erida on 24/09/2016.
 */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioDetector {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final String EMPTY_DUMMY_FILE = "/dev/null";
    private static final double MIN_NOICE_AMPLITUDE = 10.0;


    private AudioManager audioManager;
    private Context mContext;
    private MediaRecorder mRecorder = null;

    private Thread mThread;
    private Handler handler = new Handler();


    AudioDetector(Context appContext) {
        super();
        this.mContext = appContext;
        this.audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(EMPTY_DUMMY_FILE);

            try {
                mRecorder.prepare();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e(LOG_TAG, "prepare() failed. Couldn't start the audio recorder");
            }

            mRecorder.start();

            this.startThread();
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;

            stopThread();
        }
    }

    public double getAmplitude() {
        if (mRecorder != null) {
            return mRecorder.getMaxAmplitude();
        }
        else {
            return 0;
        }
    }

    public boolean isThereNoice() {
        return getAmplitude() > MIN_NOICE_AMPLITUDE;
    }

    public void sendAlert() throws RuntimeException {
        Log.i(LOG_TAG, "BE QUIET I AM Sleeping");
        mThread.interrupt();
    }

    // Inner class definition
    protected Runnable amplitudeCheckRunnable = new Runnable() {

        @Override
        public void run() {

            while (!Thread.interrupted()) {
                try {
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run() {
                            if(isThereNoice()) {
                                sendAlert();
                            }
                        }
                    }, 100);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "The thread is interrupted");
                }

            }
        }
    };

    public void startThread() {
        if(mThread == null) {
            mThread = new Thread(amplitudeCheckRunnable);
        }
        mThread.start();
    }

    public void stopThread() {
        if(mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

}

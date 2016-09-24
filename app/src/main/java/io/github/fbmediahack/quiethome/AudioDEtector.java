package io.github.fbmediahack.quiethome;

/**
 * Created by erida on 24/09/2016.
 */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioDetector {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final String EMPTY_DUMMY_FILE = "/dev/null";

    private AudioManager audioManager;
    private Context mContext;
    private MediaRecorder mRecorder = null;

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
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
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


}

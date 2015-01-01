package eu.zkkn.android.ring;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;

public class PositionTrackService extends Service implements SensorEventListener {

    private AudioManager mAudioManager;
    private SensorManager mSensorManager;
    int mInitialMode = AudioManager.RINGER_MODE_NORMAL;
    private boolean faceUp = false;
    private boolean faceDown = false;
    private boolean muted = false;


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onCreate() {
        MyLog.setContext(getApplicationContext());
        MyLog.l(getClass().getName() + ".onCreate()");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mInitialMode = mAudioManager.getRingerMode();

        // check if device isn't already in Silent Mode
        if (mInitialMode != AudioManager.RINGER_MODE_SILENT) {
            MyLog.l("Accelerometer activated");
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.l(getClass().getName() + ".onStart()");
        // If killed, don't restart. We don't know when would it be
        // and if phone was still ringing
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        MyLog.l(getClass().getName() + ".onDestroy()");

        mSensorManager.unregisterListener(this);
        restoreRingerMode();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);
        int z = Math.round(event.values[2]);

        // device must by face up at first
        if (!faceUp) {
            faceUp = isFaceUp(x, y, z);
        } else {
            if (!faceDown) {
                faceDown = isFaceDown(x, y, z);
            }
        }

        if (!muted && faceDown && faceUp) {
            muted = true;
            mSensorManager.unregisterListener(this);
            setSilentMode();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean isFaceDown(int x, int y, int z) {
        return z < -9 && z > -11 && y < 2 && y > -2 && x < 2 &&  x > -2;
    }

    private boolean isFaceUp(int x, int y, int z) {
        return z > 1;
    }

    private void setSilentMode() {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        MyLog.notification("Ring Notification", "Device was upside down!");
        MyLog.l("SILENT MODE");
    }

    private void restoreRingerMode() {
        if (mInitialMode != mAudioManager.getRingerMode()) {
            MyLog.l("RINGER MODE RESTORED");
            mAudioManager.setRingerMode(mInitialMode);
        }
    }
}

package eu.zkkn.android.ring;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class PositionTrackService extends Service implements SensorEventListener {

    private AudioManager mAudioManager;
    private SensorManager mSensorManager;
    int ringerMode = AudioManager.RINGER_MODE_NORMAL;
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
        //TODO: watch for multiple calling onCreate
        MyLog.setContext(getApplicationContext());
        MyLog.l(getClass().getName() + ".onCreate()");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringerMode = mAudioManager.getRingerMode();

        //TODO: test if isn't already muted
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //TODO: onStartCommand

    @Override
    public void onDestroy() {
        MyLog.l(getClass().getName() + ".onDestroy()");

        mSensorManager.unregisterListener(this);
        mAudioManager.setRingerMode(ringerMode);
        MyLog.l("RINGER MODE RESTORED");
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
            showNotification();
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

    private void showNotification() {

        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        MyLog.l("SILENT MODE");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Ring Notification")
                        .setContentText("Device was upside down!");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, mBuilder.build());
    }
}

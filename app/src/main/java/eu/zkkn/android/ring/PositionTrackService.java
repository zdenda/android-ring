package eu.zkkn.android.ring;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class PositionTrackService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private boolean faceUp = false;
    //private boolean faceDown = false;

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(getApplicationContext(), getClass().getName() + ".onCreate()", Toast.LENGTH_SHORT).show();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), getClass().getName() + ".onDestroy()", Toast.LENGTH_SHORT).show();

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);
        int z = Math.round(event.values[2]);


        if (!faceUp) {
            faceUp = isFaceUp(x, y, z);
        } else if (isFaceDown(x, y, z)) {
            showNotification();
            stopSelf();
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
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Ring Notification")
                        .setContentText("Device was upside down!");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, mBuilder.build());
    }
}

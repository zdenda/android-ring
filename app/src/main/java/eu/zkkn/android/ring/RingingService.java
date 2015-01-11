package eu.zkkn.android.ring;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;

public class RingingService extends Service implements SensorEventListener {

    /**
     * Intent action indicating that ringtone should be muted when
     * the front of device is turned so that is facing downwards
     */
    public static final String ACTION_MUTE = "eu.zkkn.android.ring.action.MUTE";

    /**
     * Intent action indicating that the volume of ringtone should
     * gradually increase up to the maximum
     */
    public static final String ACTION_INCREASE = "eu.zkkn.android.ring.action.INCREASE";

    /**
     * Intent action indicating that both of the modes (Mute and Increase) should be activated
     *
     * @see #ACTION_MUTE
     * @see #ACTION_INCREASE
     */
    public static final String ACTION_ALL = "eu.zkkn.android.ring.action.ALL";


    /** Indicates whether the mute functionality is active */
    private boolean mMuteStarted;

    /** Indicates whether the increasing of ringer's volume is active */
    private boolean mIncreaseStarted;

    /** Access to volume and ringer mode */
    private AudioManager mAudioManager;

    /** Access to the device's sensors, used for Accelerometer */
    private SensorManager mSensorManager;

    /** Ringer mode at the start */
    private int mInitialMode = AudioManager.RINGER_MODE_NORMAL;

    /** Ringer volume at the start*/
    private int mInitialVolume;

    /** Maximum volume for ringer */
    private int mMaxVolume;

    /** Interval (in milliseconds) between each increase of ringtone volume */
    private static final int DELAY_MS = 7 * 1000;

    /** The volume index to set */
    private int mVolume = 1;

    /**
     * Schedule runnable (@see #mRunnable) to be executed
     *  in periodical intervals (@see #DELAY_MS)
     */
    private Handler mHandler;
    /** Code that will be periodically executed */
    private Runnable mRunnable;

    /** Indicates whether the Ringer Mode was changed by this service */
    private boolean mModeChanged;

    /** Indicates whether the display of device was facing upwards */
    private boolean mDisplayUp;

    /** indicates whether the display of device was facing downwards*/
    private boolean mDisplayDown;



    @Override
    public void onCreate() {
        MyLog.setContext(getApplicationContext());
        MyLog.l(getClass().getName() + ".onCreate()");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        mInitialVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mInitialMode = mAudioManager.getRingerMode();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.l(getClass().getName() + ".onStart()");

        // there's no point in running this service if the Silent Mode is activated
        if (mInitialMode == AudioManager.RINGER_MODE_SILENT) stopSelf();
        MyLog.l(intent.getAction());

        switch (intent.getAction()) {
            case ACTION_ALL:
                mIncreaseStarted = startIncrease();
                mMuteStarted = startMute();
                break;
            case ACTION_INCREASE:
                mIncreaseStarted = startIncrease();
                mMuteStarted = false;
                break;
            case ACTION_MUTE:
                mIncreaseStarted = false;
                mMuteStarted = startMute();
                break;
            default:
                mIncreaseStarted = false;
                mMuteStarted = false;
                break;
        }
        // stop service if none function was successfully started
        if (!mMuteStarted && !mIncreaseStarted) stopSelf();

        // If killed, don't restart. We don't know when would that be
        // and if the phone was still ringing
        return START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {
        MyLog.l(getClass().getName() + ".onDestroy()");
        if (mMuteStarted) destroyMute();
        if (mIncreaseStarted) destroyIncrease();
    }

    /**
     * Initialize variables and start increasing of the ringtone volume
     *
     * @return Returns true if the increasing of volume was successfully started,
     * else false
     */
    private boolean startIncrease() {
        MyLog.l("Start Increase");

        // only if device is ringing - is in normal mode (not in silent or vibrate)
        // and volume has positive value
        if (mInitialVolume > 0 && mInitialMode == AudioManager.RINGER_MODE_NORMAL) {
            //TODO: there might be slight delay during which the device will ring with default volume
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mVolume, 0);
            MyLog.l("Volume: " + mVolume);

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    // only if ringer hasn't been already muted by the Mute part
                    if (!mModeChanged) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, ++mVolume, 0);
                        MyLog.l("Volume: " + mVolume);
                        // schedule next run if current volume is lower than maximum
                        if (mVolume < mMaxVolume) mHandler.postDelayed(mRunnable, DELAY_MS);
                    }
                }
            };
            mHandler = new Handler();
            mHandler.postDelayed(mRunnable, DELAY_MS);
            return true;
        }
        return false;
    }

    /**
     * Clean up resources used for increasing ringtone volume and restore initial volume
     *
     * @return true
     */
    private boolean destroyIncrease() {
        MyLog.l("Destroy Increase");

        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        if (mInitialVolume != mAudioManager.getStreamVolume(AudioManager.STREAM_RING)) {
            MyLog.l("Restore Initial Volume: " + mInitialVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mInitialVolume, 0);
        }
        return true;
    }

    /**
     * Initialize monitoring of device's position to determine whether Silent Mode
     * should be activated during ringing
     *
     * @return true
     */
    private boolean startMute() {
        MyLog.l("Start Mute");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        MyLog.l("Accelerometer activated");
        return true;
    }

    /**
     * Clean up resources used for muting of ringer and restore initial mode
     *
     * @return true
     */
    private boolean destroyMute() {
        MyLog.l("Destroy Mute");

        if (mSensorManager != null) mSensorManager.unregisterListener(this);
        if (mInitialMode != mAudioManager.getRingerMode()) {
            MyLog.l("Restore Ringer Mode");
            mAudioManager.setRingerMode(mInitialMode);
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);
        int z = Math.round(event.values[2]);

        // device must be face up at first,
        // so the user can see who's calling
        if (!mDisplayUp) {
            mDisplayUp = isDisplayUp(x, y, z);
        } else {
            if (!mDisplayDown) {
                mDisplayDown = isDisplayDown(x, y, z);
            }
        }

        if (!mModeChanged && mDisplayDown && mDisplayUp) {
            mModeChanged = true;
            mSensorManager.unregisterListener(this);
            setSilentMode();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing, accuracy doesn't matter
    }

    /**
     * Determine whether the top of device is facing upwards
     *
     * @param x Acceleration force along the x axis
     * @param y Acceleration force along the y axis
     * @param z Acceleration force along the z axis
     *
     * @return Returns true if the device's top is facing upwards
     */
    private boolean isDisplayUp(int x, int y, int z) {
        return z > 1;
    }

    /**
     * Determine whether the top of device is facing downwards
     *
     * @param x Acceleration force along the x axis
     * @param y Acceleration force along the y axis
     * @param z Acceleration force along the z axis
     *
     * @return Returns true if the device's top is facing downwards
     */
    private boolean isDisplayDown(int x, int y, int z) {
        return z < -9 && z > -11 && y < 2 && y > -2 && x < 2 &&  x > -2;
    }

    /**
     * Set Silent Ringer Mode
     */
    private void setSilentMode() {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        MyLog.notification("Ring Notification", "Device was upside down!");
        MyLog.l("SILENT MODE");
    }

}

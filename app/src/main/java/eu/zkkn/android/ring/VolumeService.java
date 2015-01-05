package eu.zkkn.android.ring;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;

public class VolumeService extends Service {

    private AudioManager mAudioManager;
    private Handler mHandler;
    private Runnable mRunnable;
    private int mInitialVolume;
    private int mMaxVolume;
    private int mDelayMs = 7 * 1000;
    private int mVolume = 1;


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onCreate() {
        MyLog.setContext(getApplicationContext());
        MyLog.l(getClass().getName() + ".onCreate()");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mInitialVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        MyLog.l("Initial Volume: " + mInitialVolume + "\n" +
                "Max Volume: " + mMaxVolume);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mAudioManager.setStreamVolume(AudioManager.STREAM_RING, ++mVolume, 0);
                MyLog.l("Volume: " + mVolume);
                if (mVolume < mMaxVolume) mHandler.postDelayed(mRunnable, mDelayMs);
            }
        };

        if (mInitialVolume > 0 && mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            //TODO: there might be slight delay during which the device will ring with default volume
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mVolume, 0);
            mHandler.postDelayed(mRunnable, mDelayMs);
            MyLog.l("Volume: " + mVolume);
        } else {
            // volume is 0, or device is in Silent or Vibrate mode,
            // so there's no point in running this service
            stopSelf();
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
        mHandler.removeCallbacksAndMessages(null);
        if (mAudioManager.getStreamVolume(AudioManager.STREAM_RING) != mInitialVolume) {
            MyLog.l("Restore Initial Volume: " + mInitialVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mInitialVolume, 0);
        }
    }
}

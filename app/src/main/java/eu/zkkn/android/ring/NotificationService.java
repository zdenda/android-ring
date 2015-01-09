package eu.zkkn.android.ring;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.CallLog;


public class NotificationService extends Service {

    MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        mPlayer = MediaPlayer.create(this, R.raw.beep);
        // TODO: mPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                stopSelf();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.l(getClass().getName() + ".onStart()");
        MyLog.l(intent);
        String[] projection = {CallLog.Calls._ID};
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                CallLog.Calls.NEW + " = 1 AND " + CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE,
                null, null);

        if (cursor.getCount() > 0) {
            // TODO: play in a new thread instead of main thread
            MyLog.l("Beeeep!!!");
            mPlayer.start();
        } else {
            MyLog.l("Cancel Alarm");
            NotificationAlarmReceiver.cancelAlarm(this);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        MyLog.l("Beep service destroyed");
    }

}

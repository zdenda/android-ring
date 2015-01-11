package eu.zkkn.android.ring;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

import java.io.IOException;


public class NotificationService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    MediaPlayer mPlayer = null;

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.l(getClass().getName() + ".onStart()");
        MyLog.l(intent);
        if (hasNewMissedCalls()) {
            MyLog.l("Beeeep!!!");
            playNotificationSound();
        } else {
            MyLog.l("Cancel Alarm");
            NotificationAlarmReceiver.cancelAlarm(this);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        MyLog.l("Beep service destroyed");
    }

    private boolean hasNewMissedCalls() {
        String[] projection = {CallLog.Calls._ID};
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                CallLog.Calls.NEW + " = 1 AND " + CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE,
                null, null);
        MyLog.l("Missed calls: " + cursor.getCount());
        return cursor.getCount() > 0;
    }

    private void playNotificationSound() {
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.beep);
            if (afd == null) return;
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            Log.d("NotificationService", "MediaPlayer failed", e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
        stopSelf();
    }
}

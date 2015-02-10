package eu.zkkn.android.ring;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;

import java.io.IOException;


public class NotificationService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mPlayer = null;

    /** Intent with Wake Lock ID received from
     * {@link android.support.v4.content.WakefulBroadcastReceiver#startWakefulService(android.content.Context, android.content.Intent)
     * WakefulBroadcastReceiver.startWakefulService()}, it should be passed back
     * in order to release the wake lock.
     */
    private Intent mWakefullIntent;

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.l(getClass().getName() + ".onStart()");
        MyLog.l(intent);
        mWakefullIntent = intent;

        if (hasNewMissedCalls() || hasNewSmsMms()) {
            MyLog.setContext(this);
            MyLog.l("Beeeep!!!");
            MyLog.notification("Beeeep!!!", "Missed call");
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
        NotificationAlarmReceiver.completeWakefulIntent(mWakefullIntent);
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        MyLog.l("Beep service destroyed");
    }

    private boolean hasNewMissedCalls() {
        // There's a bug in Lollipop, which count dismissed calls as missed, but doesn't show any
        // notification for them, so we'll play beep sound and user won't see any notification
        // https://code.google.com/p/android/issues/detail?id=81262
        String[] projection = {CallLog.Calls._ID};
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                CallLog.Calls.NEW + " = 1 AND " + CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE,
                null, null);
        int count = cursor.getCount();
        cursor.close();
        MyLog.l("Missed calls: " + count);
        return count > 0;
    }

    // Telephony.Sms Telephony.Mms are part of SDK since KITKAT,
    // but unofficially should work on older versions
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean hasNewSmsMms() {
        int count = 0;
        Cursor cursor;

        //SMS
        String[] projection = {Telephony.Sms.Inbox._ID};
        cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
                projection, Telephony.Sms.Inbox.READ + " = 0", null, null);
        count += cursor.getCount();
        //MMS
        cursor = getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI,
                projection, Telephony.Mms.Inbox.READ + " = 0", null, null);
        count += cursor.getCount();

        cursor.close();
        MyLog.l("New SMS and MSS: " + count);
        return count > 0;
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
            stopSelf();
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

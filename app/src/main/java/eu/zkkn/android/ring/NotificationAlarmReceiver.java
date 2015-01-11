package eu.zkkn.android.ring;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NotificationAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.l("Alarm onReceive()");
        Intent service = new Intent(context, NotificationService.class);
        startWakefulService(context, service);
    }

    public static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                getAlarmIntent(context));
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getAlarmIntent(context));
    }

    private static PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}

package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class UserPresentReceiver extends BroadcastReceiver {

    public static void register(Context context) {
        context.getApplicationContext().registerReceiver(new UserPresentReceiver(),
                new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // if the user is present, cancel alarm for beep notification
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            MyLog.setContext(context);
            MyLog.l("Cancel Sound Notification because user is present");
            NotificationAlarmReceiver.cancelAlarm(context);
        }
    }
}

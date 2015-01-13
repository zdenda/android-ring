package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class MessagingBroadcastReceiver extends BroadcastReceiver {

    protected Intent mIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.setContext(context);
        MyLog.l(intent);

        mIntent = intent;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = preferences.getBoolean(MainActivity.PREF_KEY_SOUND_NOTIFICATION_ENABLED, true);

        if (enabled && checkIntent()) {
            MyLog.l("Set alarm");
            NotificationAlarmReceiver.setAlarm(context);
        }

    }

    protected Intent getIntent() {
        if (mIntent != null) return mIntent;
        return new Intent();
    }

    protected abstract boolean checkIntent();

}

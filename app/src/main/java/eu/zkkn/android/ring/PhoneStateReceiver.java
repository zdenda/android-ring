package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.setContext(context);
        MyLog.l(intent);

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Device call state: Ringing. A new call arrived and is ringing or waiting. In the latter case, another call is already active.
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                MyLog.l("RINGING; number: " + number);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean mute = preferences.getBoolean(MainActivity.PREF_KEY_MUTE_ON_FLIP_ENABLED, true);
                boolean increase = preferences.getBoolean(MainActivity.PREF_KEY_GRADUALLY_INCREASE_ENABLED, true);

                // determine action for intent
                String action = null;
                if (mute && increase) {
                    action = RingingService.ACTION_ALL;
                } else {
                    if (mute) action = RingingService.ACTION_MUTE;
                    if (increase) action = RingingService.ACTION_INCREASE;
                }

                // start service
                ComponentName componentName = null;
                if (action != null) {
                    Intent i = new Intent(context, RingingService.class);
                    i.setAction(action);
                    componentName = context.startService(i);
                }
                if (componentName == null) {
                    MyLog.l("Service couldn't be started");
                }
            } else {
                if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    // Device call state: Off-hook. At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.
                    MyLog.l("OFFHOOK");
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    // Device call state: No activity.
                    MyLog.l("IDLE");
                } else {
                    MyLog.l("UNKNOWN");
                }
                boolean stopped;
                stopped = context.stopService(new Intent(context, RingingService.class));
                if (!stopped) {
                    MyLog.l("RingingService couldn't be stopped");
                }
            }

        }

    }
}

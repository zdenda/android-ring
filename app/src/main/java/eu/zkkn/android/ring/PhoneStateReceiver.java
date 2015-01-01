package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
                ComponentName componentName = context.startService(new Intent(context, PositionTrackService.class));
                if (componentName == null) {
                    MyLog.l("PositionTrackService couldn't be started");
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
                boolean stopped = context.stopService(new Intent(context, PositionTrackService.class));
                if (!stopped) {
                    MyLog.l("PositionTrackService couldn't be stopped");
                }
            }

        }

    }
}

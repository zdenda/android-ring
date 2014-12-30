package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.setContext(context);
        MyLog.d(intent);

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Device call state: Ringing. A new call arrived and is ringing or waiting. In the latter case, another call is already active.
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                MyLog.l("RINGING; number: " + number);
                context.startService(new Intent(context, PositionTrackService.class));
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Device call state: Off-hook. At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.
                MyLog.l("OFFHOOK");
                //TODO: check if service is running
                context.stopService(new Intent(context, PositionTrackService.class));
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // Device call state: No activity.
                MyLog.l("IDLE");
                //TODO: see OFFHOOK todo
                context.stopService(new Intent(context, PositionTrackService.class));
            } else {
                MyLog.l("UNKNOWN");
            }

        }

    }
}

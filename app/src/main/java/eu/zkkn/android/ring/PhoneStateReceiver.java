package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneStateReceiver extends BroadcastReceiver {


    private static final String LOG_TAG = "ring_log";

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.d(LOG_TAG, intent);

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

            String toastText = "Status: ";

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Device call state: Ringing. A new call arrived and is ringing or waiting. In the latter case, another call is already active.
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                toastText += "Příchozí hovor\n" +
                        "Číslo: " + number;
                Log.d(LOG_TAG + "_z", "RINGING");
                context.startService(new Intent(context, PositionTrackService.class));
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Device call state: Off-hook. At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.
                toastText += "Odchozí hovor";
                Log.d(LOG_TAG + "_z", "OFFHOOK");
                context.stopService(new Intent(context, PositionTrackService.class));
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // Device call state: No activity.
                toastText += "Konec";
                Log.d(LOG_TAG + "_z", "IDLE");
                context.stopService(new Intent(context, PositionTrackService.class));
            } else {
                toastText += "unknown";
                Log.d(LOG_TAG + "_z", "UNKNOWN");
            }

            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
        }

    }
}

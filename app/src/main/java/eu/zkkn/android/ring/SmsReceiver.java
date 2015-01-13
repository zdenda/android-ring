package eu.zkkn.android.ring;

import android.annotation.TargetApi;
import android.os.Build;
import android.provider.Telephony;

public class SmsReceiver extends MessagingBroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean checkIntent() {
        // check whether mIntent is valid SMS received Intent
        return Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(getIntent().getAction());
    }
}

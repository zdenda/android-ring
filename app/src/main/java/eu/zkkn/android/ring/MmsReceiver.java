package eu.zkkn.android.ring;

import android.annotation.TargetApi;
import android.os.Build;
import android.provider.Telephony;

public class MmsReceiver extends MessagingBroadcastReceiver {

    private static final String MMS_MIME_TYPE = "application/vnd.wap.mms-message";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean checkIntent() {
        // check whether mIntent is valid MMS received Intent
        return (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION.equals(getIntent().getAction())
                && MMS_MIME_TYPE.equals(getIntent().getType()));
    }

}

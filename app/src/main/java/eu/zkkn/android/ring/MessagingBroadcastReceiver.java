package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MessagingBroadcastReceiver extends BroadcastReceiver {

    protected Intent mIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.setContext(context);
        MyLog.l(intent);

        mIntent = intent;

        boolean enabled = ShPrefUtils.isSoundNotificationEnabled(context);
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

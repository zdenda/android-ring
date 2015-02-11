package eu.zkkn.android.ring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class UserPresentReceiver extends BroadcastReceiver {

    public static void register(Context context) {
        MyLog.l("Register ACTION_USER_PRESENT Receiver");
        // This dynamic registering doesn't work on Moto G, it stops receiving intents after
        // some time (25 minutes). Defining and enabling static receiver in manifest seems to work.
        /*
        context.getApplicationContext().registerReceiver(new UserPresentReceiver(),
                new IntentFilter(Intent.ACTION_USER_PRESENT));
        */
        toggleComponentState(context, UserPresentReceiver.class, true);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // if the user is present, cancel alarm for beep notification
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            MyLog.setContext(context);
            MyLog.l("Cancel Sound Notification because user is present");
            NotificationAlarmReceiver.cancelAlarm(context);
            toggleComponentState(context, UserPresentReceiver.class, false);
        }
    }

    // TODO: similar method is in MainActivity.java, so if this will work on Moto G, rewrite it
    private static void toggleComponentState(Context context, Class<?> componentClass, boolean enable) {
        context = context.getApplicationContext();
        int state = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName component = new ComponentName(context, componentClass);
        PackageManager packageManager = context.getPackageManager();
        if (state != packageManager.getComponentEnabledSetting(component)) {
            packageManager.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP);
            MyLog.l(component + " state changed to [" + enable + "]");
        }
    }
}

package eu.zkkn.android.ring;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/**
 * @deprecated Only for debugging
 */
public class DebugUserPresentReceiver extends UserPresentReceiver {

    /** @deprecated This doesn't work on Moto G */
    public static void register(Context context) {
        context.getApplicationContext().registerReceiver(new DebugUserPresentReceiver(),
                new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            MyLog.setContext(context);
            MyLog.l("DebugUserPresentReceiver: user is present");
            Toast.makeText(context, context.getString(R.string.user_present_welcome_back_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }
}

package eu.zkkn.android.ring;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

public final class MyLog {

    public static final String LOG_TAG = "my_log";
    public static final boolean ENABLED = true;
    public static boolean toast = false;

    private static Context context = null;

    public static void setContext(Context context) {
        MyLog.context = context;
    }


    public static void l(String msg) {
        if (ENABLED) {
            l(LOG_TAG, msg);
        }
    }

    public static void l(String tag, String msg) {
        if (ENABLED) {
            if (toast && context != null) toast(msg);
            else debug(tag, msg);
        }
    }

    public static void l(Intent intent) {
        if (ENABLED) {
            l(LOG_TAG, intent);
        }
    }

    private static void l(String tag, Intent intent) {
        if (ENABLED) {
            if (toast && context != null) toast(intent);
            else debug(tag, intent);
        }
    }

    public static void notification(String title, String text) {
        if (ENABLED) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(text);
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            mBuilder.setContentIntent(pendingIntent);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(1, mBuilder.build());
        }
    }



    private static void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private static void toast(Intent intent) {
        // Log Intent as toast is not implemented, use debug
        debug(LOG_TAG, intent);
    }

    private static void debug(String tag, String msg) {
        if (ENABLED) {
            Log.d(tag, msg);
        }
    }

    private static void debug(String tag, Intent intent) {
        if (ENABLED) {
            Log.d(tag, intent.toString());
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Set<String> keys = bundle.keySet();
                Iterator<String> it = keys.iterator();
                Log.d(tag, "Intent Extras {");
                while (it.hasNext()) {
                    String key = it.next();
                    Log.d(tag, " " + key + "=" + bundle.get(key) + " ");
                }
                Log.d(tag, "}");
            }
        }
    }

}

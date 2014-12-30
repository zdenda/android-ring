package eu.zkkn.android.ring;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

public final class MyLog {

    public static boolean enabled = true;
    public static boolean toast = true;
    public static final String LOG_TAG = "my_log";

    private static Context context = null;

    public static void setContext(Context context) {
        MyLog.context = context;
    }


    public static int t(String msg) {
        if (context != null) return t(context, msg);
        return 0;
    }

    public static int t(Context context, String msg) {
        if (!enabled) return 0;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        return msg.length();
    }

    public static int d(String msg) {
        return log("d", LOG_TAG, msg);
    }

    public static int d(Intent intent) {
        return log("d", LOG_TAG, intent);
    }

    public static int l(String msg) {
        if (toast && context != null) return t(msg);
        return d(msg);
    }

    private static int log(String logMethodName, String tag, String msg) {
        if (!enabled) return 0;
        Method logMethod = getLogMethod(logMethodName);
        return invoke(logMethod, tag, msg);
    }

    private static int log(String logMethodName, String tag, Intent intent) {
        if (!enabled) return 0;
        int ret = 0;
        Method logMethod = getLogMethod(logMethodName);
        ret += invoke(logMethod, tag, intent.toString());
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            invoke(logMethod, tag, "Intent Extras {");
            while (it.hasNext()) {
                String key = it.next();
                invoke(logMethod, tag, " " + key + "=" + bundle.get(key) + " ");
            }
            invoke(logMethod, tag, "}");
        }
        return ret;
    }

    private static Method getLogMethod(String methodName) {
        Method method = null;
        try {
            method = Log.class.getMethod(methodName, String.class, String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    private static int invoke(Method logType, String tag, String msg) {
        int ret = 0;
        try {
            return (int) logType.invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

}

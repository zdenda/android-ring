package eu.zkkn.android.ring;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

public final class MyLog {

    public static int v(String tag, Intent intent) {
        return log("v", tag, intent);
    }

    public static int d(String tag, Intent intent) {
        return log("d", tag, intent);
    }

    public static int i(String tag, Intent intent) {
        return log("i", tag, intent);
    }

    public static int w(String tag, Intent intent) {
        return log("w", tag, intent);
    }

    public static int e(String tag, Intent intent) {
        return log("e", tag, intent);
    }

    private static int log(String logMethodName, String tag, Intent intent) {
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

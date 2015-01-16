package eu.zkkn.android.ring;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utilities and constants  associated with app shared preferences.
 */
public class ShPrefUtils {

    /**
     * Boolean preference that indicates whether the Muting functionality is enabled.
     * If enabled, it will mute the ringer when the device is flipped that the front is facing downwards.
     */
    public static final String PREF_KEY_MUTE_ON_FLIP_ENABLED = "pref_mute_enabled";

    /**
     * Boolean preference that indicates whether the Increasing of ringer's volume is enabled.
     */
    public static final String PREF_KEY_GRADUALLY_INCREASE_ENABLED = "pref_increase_enabled";

    /**
     * Boolean preference that indicates whether the Notification Sound is enabled.
     * If enabled, the beep sound will be played in regular intervals until there's unreceived call, unread SMS, etc.
     */
    public static final String PREF_KEY_SOUND_NOTIFICATION_ENABLED = "pref_notification_enabled";

    /**
     * Boolean preference that indicates whether some debug information should be presented to the user
     */
    public static final String PREF_KEY_DEBUG_ENABLED = "pref_debug_enabled";


    private static SharedPreferences sPreferences;

    private static SharedPreferences getPref(Context context) {
        if (sPreferences == null) {
            sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sPreferences;
    }


    public static void setMuteEnabled(Context context, boolean enabled) {
        getPref(context).edit().putBoolean(PREF_KEY_MUTE_ON_FLIP_ENABLED, enabled).commit();
    }

    public static boolean isMuteEnabled(Context context) {
        return getPref(context).getBoolean(PREF_KEY_MUTE_ON_FLIP_ENABLED, true);
    }


    public static void setIncreaseEnabled(Context context, boolean enabled) {
        getPref(context).edit().putBoolean(PREF_KEY_GRADUALLY_INCREASE_ENABLED, enabled).commit();
    }

    public static boolean isIncreaseEnabled(Context context) {
        return getPref(context).getBoolean(PREF_KEY_GRADUALLY_INCREASE_ENABLED, true);
    }


    public static void setSoundNotificationEnabled(Context context, boolean enabled) {
        getPref(context).edit().putBoolean(PREF_KEY_SOUND_NOTIFICATION_ENABLED, enabled).commit();
    }

    public static boolean isSoundNotificationEnabled(Context context) {
        return getPref(context).getBoolean(PREF_KEY_SOUND_NOTIFICATION_ENABLED, true);
    }


    public static void setDebugEnabled(Context context, boolean enabled) {
        getPref(context).edit().putBoolean(PREF_KEY_DEBUG_ENABLED, enabled).commit();
    }

    public static boolean isDebugEnabled(Context context) {
        return getPref(context).getBoolean(PREF_KEY_DEBUG_ENABLED, false);
    }

}

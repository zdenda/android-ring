package eu.zkkn.android.ring;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public static final String PREF_KEY_MUTE_ON_FLIP_ENABLED = "pref_mute_enabled";
    public static final String PREF_KEY_GRADUALLY_INCREASE_ENABLED = "pref_increase_enabled";
    public static final String PREF_KEY_SOUND_NOTIFICATION_ENABLED = "pref_notification_enabled";
    public static final String PREF_KEY_DEBUG_ENABLED = "pref_debug_enabled";

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mutingEnabled = mPreferences.getBoolean(PREF_KEY_MUTE_ON_FLIP_ENABLED, true);
        boolean increasingEnabled = mPreferences.getBoolean(PREF_KEY_GRADUALLY_INCREASE_ENABLED, true);
        boolean notificationEnabled = mPreferences.getBoolean(PREF_KEY_SOUND_NOTIFICATION_ENABLED, true);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        boolean hasAccelerometer = (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);

        CheckBox checkBoxMute = (CheckBox) findViewById(R.id.chkMuteEnabled);
        checkBoxMute.setEnabled(hasAccelerometer);
        checkBoxMute.setChecked(mutingEnabled && hasAccelerometer);
        CheckBox checkBoxIncrease = (CheckBox) findViewById(R.id.chkIncreaseEnabled);
        checkBoxIncrease.setChecked(increasingEnabled);
        CheckBox checkBoxNotification = (CheckBox) findViewById(R.id.chkSoundNotification);
        checkBoxNotification.setChecked(notificationEnabled);


        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean mute = ((CheckBox) findViewById(R.id.chkMuteEnabled)).isChecked();
                boolean increase = ((CheckBox) findViewById(R.id.chkIncreaseEnabled)).isChecked();
                boolean notification = ((CheckBox) findViewById(R.id.chkSoundNotification)).isChecked();
                // update shared preferences
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(PREF_KEY_MUTE_ON_FLIP_ENABLED, mute);
                editor.putBoolean(PREF_KEY_GRADUALLY_INCREASE_ENABLED, increase);
                editor.putBoolean(PREF_KEY_SOUND_NOTIFICATION_ENABLED, notification);
                editor.commit();
                // enable/disable  Phone State Broadcast Receiver
                //TODO: what will happen after restart? wil it be enabled again?
                int state = mute || increase || notification
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                ComponentName phoneStateReceiver = new ComponentName(getApplicationContext(), PhoneStateReceiver.class);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                packageManager.setComponentEnabledSetting(phoneStateReceiver, state, PackageManager.DONT_KILL_APP);
                if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    MyLog.setContext(getApplicationContext());
                    MyLog.l("Phone State Receiver Disabled");
                }
            }
        };
        checkBoxMute.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxIncrease.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxNotification.setOnCheckedChangeListener(onCheckedChangeListener);

        boolean debugEnabled = mPreferences.getBoolean(PREF_KEY_DEBUG_ENABLED, false);
        if (debugEnabled) {
            findViewById(R.id.layoutDebug).setVisibility(View.VISIBLE);
            refreshSmsCounts();

            findViewById(R.id.btSmsRefresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshSmsCounts();
                }
            });

            findViewById(R.id.btMissedCalls).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] projection = {CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE};
                    String[] columns = {CallLog.Calls.NUMBER, CallLog.Calls.DATE};
                    int[] listItems = {android.R.id.text1, android.R.id.text2};
                    Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                            CallLog.Calls.NEW + " = 1 AND " + CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE, null, null);
                    MyLog.l(cursor.getCount() + " missed call(s)");
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(),
                            android.R.layout.simple_list_item_2, cursor, columns, listItems, 0);
                    ((ListView) findViewById(R.id.listView)).setAdapter(adapter);
                }
            });

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem debug = menu.findItem(R.id.action_enable_debug);
        boolean debugEnabled = mPreferences.getBoolean(PREF_KEY_DEBUG_ENABLED, false);
        debug.setChecked(debugEnabled);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_created:
                Uri webpage = Uri.parse("https://github.com/zdenda/android-ring");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;

            case R.id.action_enable_debug:
                boolean debugging;
                if (item.isChecked()) {
                    debugging = false;
                    item.setChecked(false);
                } else {
                    debugging = true;
                    item.setChecked(true);
                }
                // save to preferences
                mPreferences.edit().putBoolean(PREF_KEY_DEBUG_ENABLED, debugging).commit();
                Toast.makeText(this, "Changes will take effect after app restart.", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Telephony.Sms is in SDK since KITKAT, but unofficially should work on older versions
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void refreshSmsCounts() {
        String[] projection = {Telephony.Sms.Inbox._ID};
        Cursor cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
                projection, Telephony.Sms.Inbox.SEEN + " = 0", null, null);
        ((TextView) findViewById(R.id.tvUnseenCount)).setText(" " + cursor.getCount() + " ");

        cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
                projection, Telephony.Sms.Inbox.READ + " = 0", null, null);
        ((TextView) findViewById(R.id.tvUnreadCount)).setText(" " + cursor.getCount() + " ");
    }

}

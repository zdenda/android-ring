package eu.zkkn.android.ring;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    /**
     * CheckBox for enable/disable flipping over to mute
     */
    private CheckBox mChbMute;

    /**
     * CheckBox for enable/disable increasing volume of ringtone
     */
    private CheckBox mChbIncrease;

    /**
     * CheckBox for enable/disable beep sound notification for missed calls and unread SMS/MMS
     */
    private CheckBox mChbNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set statuses of checkBoxes
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        boolean hasAccelerometer = (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
        boolean mutingEnabled = ShPrefUtils.isMuteEnabled(this);
        mChbMute = (CheckBox) findViewById(R.id.chkMuteEnabled);
        mChbMute.setEnabled(hasAccelerometer); // disable checkbox if there's no Accelerometer
        mChbMute.setChecked(mutingEnabled && hasAccelerometer);

        mChbIncrease = (CheckBox) findViewById(R.id.chkIncreaseEnabled);
        mChbIncrease.setChecked(ShPrefUtils.isIncreaseEnabled(this));

        mChbNotification = (CheckBox) findViewById(R.id.chkSoundNotification);
        mChbNotification.setChecked(ShPrefUtils.isSoundNotificationEnabled(this));


        // common changeListener for all checkboxes
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // call on*Changed for appropriate checkBox
                switch (buttonView.getId()) {
                    case R.id.chkMuteEnabled:
                        onMuteChbChanged(isChecked);
                        break;
                    case  R.id.chkIncreaseEnabled:
                        onIncreaseChbChanged(isChecked);
                        break;
                    case R.id.chkSoundNotification:
                        onNotificationChbChanged(isChecked);
                        break;
                    default:
                        MyLog.l("Unknown CheckBox changed");
                        return;
                }

                // enable/disable  Phone State Broadcast Receiver
                //TODO: what will happen after restart? wil it be enabled again?
                boolean enable = mChbMute.isChecked() || mChbIncrease.isChecked() || mChbNotification.isChecked();
                toggleComponentState(PhoneStateReceiver.class, enable);
                if (!enable) {
                    MyLog.setContext(getApplicationContext());
                    MyLog.l("Phone State Receiver Disabled");
                }
            }
        };
        mChbMute.setOnCheckedChangeListener(onCheckedChangeListener);
        mChbIncrease.setOnCheckedChangeListener(onCheckedChangeListener);
        mChbNotification.setOnCheckedChangeListener(onCheckedChangeListener);


        // initialize Debug controls
        CheckBox cancelNotification = (CheckBox) findViewById(R.id.chkCancelSoundNotification);
        cancelNotification.setEnabled(mChbNotification.isChecked());
        cancelNotification.setChecked(ShPrefUtils.isAutoCancelSoundNotification(this));
        cancelNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ShPrefUtils.setAutoCancelSoundNotification(MainActivity.this, isChecked);
            }
        });

        if (ShPrefUtils.isDebugEnabled(this)) {
            findViewById(R.id.layoutDebug).setVisibility(View.VISIBLE);
            refreshSmsCounts();
            refreshMmsCounts();

            findViewById(R.id.btSmsRefresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshSmsCounts();
                }
            });

            findViewById(R.id.btMmsRefresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshMmsCounts();
                }
            });

            findViewById(R.id.btMissedCalls).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] projection = {CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE};
                    Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                            CallLog.Calls.NEW + " = 1 AND " + CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE, null, null);
                    MyLog.l(cursor.getCount() + " missed call(s)");
                    LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCalls);
                    layout.removeAllViews();
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                        Date date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
                        TextView tv = new TextView(MainActivity.this);
                        tv.setLines(1);
                        tv.setText(number + " (" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + ")");
                        layout.addView(tv);
                    }
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem debug = menu.findItem(R.id.action_enable_debug);
        boolean debugEnabled = ShPrefUtils.isDebugEnabled(this);
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
                ShPrefUtils.setDebugEnabled(this, debugging);
                // TODO: http://developer.android.com/reference/android/app/Activity.html#recreate%28%29
                // unfortunately it works only API level 11+
                Toast.makeText(this, "Changes will take effect after app restart.", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMuteChbChanged(boolean isChecked) {
        ShPrefUtils.setMuteEnabled(this, isChecked);
    }

    private void onIncreaseChbChanged(boolean isChecked) {
        ShPrefUtils.setIncreaseEnabled(this, isChecked);
    }

    private void onNotificationChbChanged(boolean isChecked) {
        ShPrefUtils.setSoundNotificationEnabled(this, isChecked);
        // enable/disable SMS and MMS receivers
        toggleComponentState(SmsReceiver.class, isChecked);
        toggleComponentState(MmsReceiver.class, isChecked);
        CheckBox cancel = (CheckBox) findViewById(R.id.chkCancelSoundNotification);
        cancel.setEnabled(isChecked);
        if (!isChecked) {
            MyLog.setContext(getApplicationContext());
            MyLog.l("SMS and MMS Receivers Disabled");
            cancel.setChecked(false);
        }
    }

    /**
     * Toggle the enabled settings for a package component (e.g. Receiver)
     * This will override state which have been set previously or in manifest file.
     *
     * @param componentClass The Class object of the desired component
     * @param enable True for enabling component, false for disabling
     */
    private void toggleComponentState(Class<?> componentClass, boolean enable) {
        int state = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName component = new ComponentName(getApplicationContext(), componentClass);
        PackageManager packageManager = getApplicationContext().getPackageManager();
        if (state != packageManager.getComponentEnabledSetting(component)) {
            packageManager.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP);
            MyLog.l(component + " state changed to [" + enable + "]");
        }
    }

    // Telephony.Sms is in SDK since KITKAT, but unofficially should work on older versions
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void refreshSmsCounts() {
        String[] projection = {Telephony.Sms.Inbox._ID};
        Cursor cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
                projection, Telephony.Sms.Inbox.SEEN + " = 0", null, null);
        ((TextView) findViewById(R.id.tvUnseenSmsCount)).setText(" " + cursor.getCount() + " ");

        cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
                projection, Telephony.Sms.Inbox.READ + " = 0", null, null);
        ((TextView) findViewById(R.id.tvUnreadSmsCount)).setText(" " + cursor.getCount() + " ");

        cursor.close();
    }

    // Telephony.Mms is in SDK since KITKAT, but unofficially should work on older versions
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void refreshMmsCounts() {
        String[] projection = {Telephony.Mms.Inbox._ID};
        Cursor cursor = getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI,
                projection, Telephony.Mms.Inbox.SEEN + " = 0", null, null);
        ((TextView) findViewById(R.id.tvUnseenMmsCount)).setText(" " + cursor.getCount() + " ");

        cursor = getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI,
                projection, Telephony.Mms.Inbox.READ + " = 0", null, null);
        ((TextView) findViewById(R.id.tvUnreadMmsCount)).setText(" " + cursor.getCount() + " ");

        cursor.close();
    }

}

package eu.zkkn.android.ring;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;


public class MainActivity extends ActionBarActivity {

    public static final String PREF_KEY_MUTE_ON_FLIP_ENABLED = "pref_mute_enabled";
    public static final String PREF_KEY_GRADUALLY_INCREASE_ENABLED = "pref_increase_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mutingEnabled = preferences.getBoolean(PREF_KEY_MUTE_ON_FLIP_ENABLED, true);
        boolean increasingEnabled = preferences.getBoolean(PREF_KEY_GRADUALLY_INCREASE_ENABLED, true);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        boolean hasAccelerometer = (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);

        CheckBox checkBoxMute = (CheckBox) findViewById(R.id.chkMuteEnabled);
        checkBoxMute.setEnabled(hasAccelerometer);
        checkBoxMute.setChecked(mutingEnabled && hasAccelerometer);
        CheckBox checkBoxIncrease = (CheckBox) findViewById(R.id.chkIncreaseEnabled);
        checkBoxIncrease.setChecked(increasingEnabled);


        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean mute = ((CheckBox) findViewById(R.id.chkMuteEnabled)).isChecked();
                boolean increase = ((CheckBox) findViewById(R.id.chkIncreaseEnabled)).isChecked();
                // update shared preferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_KEY_MUTE_ON_FLIP_ENABLED, mute);
                editor.putBoolean(PREF_KEY_GRADUALLY_INCREASE_ENABLED, increase);
                editor.commit();
                // enable/disable  Phone State Broadcast Receiver
                //TODO: what will happen after restart? wil it be enabled again?
                int state = mute || increase ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_created) {
            Uri webpage = Uri.parse("https://github.com/zdenda/android-ring");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

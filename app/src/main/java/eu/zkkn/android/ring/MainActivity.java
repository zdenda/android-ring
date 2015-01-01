package eu.zkkn.android.ring;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;


public class MainActivity extends ActionBarActivity {

    public static final String PREF_KEY_ENABLED = "pref_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enable = preferences.getBoolean(PREF_KEY_ENABLED, true);

        CheckBox checkBox = (CheckBox) findViewById(R.id.chkEnabled);
        checkBox.setChecked(enable);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update shared preferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_KEY_ENABLED, isChecked);
                editor.commit();
                // enable/disable  Phone State Broadcast Receiver
                int state = isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                ComponentName phoneStateReceiver = new ComponentName(getApplicationContext(), PhoneStateReceiver.class);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                packageManager.setComponentEnabledSetting(phoneStateReceiver, state, PackageManager.DONT_KILL_APP);
            }
        });
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

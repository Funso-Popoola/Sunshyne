package com.hoh.android.sunshyne;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    private final static String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.location_settings_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.units_settings_key)));


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        // get the new string value the preference is changed to
        String newPrefValue = newValue.toString();

        if (preference instanceof ListPreference){
            //in case of a ListPreference, first get the index

            ListPreference listPreference = (ListPreference)preference;
            int index = listPreference.findIndexOfValue(getString(R.string.units_settings_key));
            if ( index >= 0) {
                Log.i(LOG_TAG, "The listPreference entry: " + listPreference.getEntries()[index]);
                preference.setSummary(listPreference.getEntries()[index]);
            }
        }
        else{
            preference.setSummary(newPrefValue);
        }

        return true;
    }

    private void bindPreferenceSummaryToValue(Preference preference){
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference, PreferenceManager
                                        .getDefaultSharedPreferences(getApplicationContext())
                                        .getString(preference.getKey(), ""));
    }
}

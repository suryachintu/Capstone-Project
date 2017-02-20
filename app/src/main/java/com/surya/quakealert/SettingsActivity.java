package com.surya.quakealert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
/**
 * Created by Surya on 07-11-2016.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static class EarthquakeFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);
            Preference magnitudeRange = findPreference(getString(R.string.quake_order_by_key));
            //related to notifications
            Preference minMagnitude = findPreference(getString(R.string.quake_min_magnitude_key));
            Preference location = findPreference(getString(R.string.quake_location_key));
            //general preferences
            Preference distance = findPreference(getString(R.string.quake_distance_key));
            Preference mapType = findPreference(getString(R.string.quake_map_type_key));
            bindPreferenceSummaryToValue(magnitudeRange);
            bindPreferenceSummaryToValue(minMagnitude);
            bindPreferenceSummaryToValue(location);
            bindPreferenceSummaryToValue(distance);
            bindPreferenceSummaryToValue(mapType);

        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }

            if (preference.getKey().equals(getString(R.string.quake_order_by_key))) {
                Utility.updateWidgets(getActivity());
            }
            if (preference.getKey().equals(getString(R.string.quake_location_key))){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(getString(R.string.notification_around_my_location_key),true);
                editor.commit();
            }
            return true;
        }
    }
}

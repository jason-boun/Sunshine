package com.example.kk.sunshine;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.example.kk.sunshine.data.WeatherContract;
import com.example.kk.sunshine.data.WeatherPreferences;
import com.example.kk.sunshine.sync.WeatherSyncUtils;

/**
 * Created by kk on 2017/6/30.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_general);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        int count = preferenceScreen.getPreferenceCount();
        for(int i = 0; i < count; i++){
            Preference pre = preferenceScreen.getPreference(i);
            if(!(pre instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(pre.getKey(), "");
                setPreferenceSummary(pre, value);
            }
        }
    }

    private void setPreferenceSummary(Preference pre, String value) {
        String stringValue = value.toString();
        String key = pre.getKey();

        if (pre instanceof ListPreference) {
            /* For list preferences, look up the correct display value in */
            /* the preference's 'entries' list (since they have separate labels/values). */
            ListPreference listPreference = (ListPreference) pre;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                pre.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            pre.setSummary(stringValue);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Activity activity = getActivity();
        if (s.equals(getString(R.string.pref_location_key))) {
            // we've changed the location
            // Wipe out any potential PlacePicker latlng values so that we can use this text entry.
            WeatherPreferences.resetLocationCoordinates(activity);
            //  COMPLETED (14) Sync the weather if the location changes
            WeatherSyncUtils.startImmediateSync(activity);
        } else if (s.equals(getString(R.string.pref_units_key))) {
            // units have changed. update lists of weather entries accordingly
            activity.getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }

        Preference preference = findPreference(s);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(s, ""));
            }
        }
    }
}

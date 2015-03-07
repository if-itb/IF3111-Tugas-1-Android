package com.kevinyudiutama.android.pbd;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Preference nimPreference = findPreference("nim");
        nimPreference.setSummary(sharedPreferences.getString("nim",""));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("nim")) {
            Preference nimPreference = findPreference(key);
            nimPreference.setSummary(sharedPreferences.getString(key,""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

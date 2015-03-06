package com.azaky.android.pbd.tomandjerry;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String KEY_PREF_API_ENDPOINT;
    public static String KEY_PREF_NIM;
    public static String KEY_PREF_CONNECTION_TIMEOUT;
    public static String KEY_PREF_AUTOZOOM;
    public static String KEY_PREF_USE_MY_LOCATION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        // sets the key names
        Resources resources = getResources();
        KEY_PREF_API_ENDPOINT = resources.getString(R.string.pref_key_api_endpoint);
        KEY_PREF_NIM = resources.getString(R.string.pref_key_nim);
        KEY_PREF_CONNECTION_TIMEOUT = resources.getString(R.string.pref_key_connection_timeout);
        KEY_PREF_AUTOZOOM = resources.getString(R.string.pref_key_autozoom);
        KEY_PREF_USE_MY_LOCATION = resources.getString(R.string.pref_key_use_my_location);

        addPreferencesFromResource(R.xml.preferences);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(KEY_PREF_API_ENDPOINT));
        bindPreferenceSummaryToValue(findPreference(KEY_PREF_NIM));
        bindPreferenceSummaryToValue(findPreference(KEY_PREF_CONNECTION_TIMEOUT));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_API_ENDPOINT)) {
            String value = sharedPreferences.getString(key,
                    getResources().getString(R.string.pref_default_api_endpoint));
            // Set the API_ENDPOINT in MainActivity
            MainActivity.API_ENDPOINT = value;
        }
        else if (key.equals(KEY_PREF_NIM)) {
            String value = sharedPreferences.getString(key,
                    getResources().getString(R.string.pref_default_nim));
            // Set the NIM in MainActivity
            MainActivity.NIM = value;
        } else if (key.equals(KEY_PREF_CONNECTION_TIMEOUT)) {
            int value = Integer.parseInt(sharedPreferences.getString(key,
                    getResources().getString(R.string.pref_default_connection_timeout)));
            // Set the Connection Timeout in MainActivity
            MainActivity.CONNECTION_TIMEOUT = value;
        } else if (key.equals(KEY_PREF_AUTOZOOM)) {
            boolean value = sharedPreferences.getBoolean(key,
                    getResources().getBoolean(R.bool.pref_default_autozoom));
            MainActivity.AUTOZOOM = value;
        } else if (key.equals(KEY_PREF_USE_MY_LOCATION)) {
            boolean value = sharedPreferences.getBoolean(key,
                    getResources().getBoolean(R.bool.pref_default_use_my_location));
            MainActivity.USE_MY_LOCATION = value;
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            // Set the summary for connection timeout
            if (preference.getKey().equals(KEY_PREF_CONNECTION_TIMEOUT)) {
                if (stringValue.equals("0")) {
                    stringValue = "infinity";
                }
                preference.setSummary("Current = " + stringValue + " ms. Use 0 for infinity");
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}

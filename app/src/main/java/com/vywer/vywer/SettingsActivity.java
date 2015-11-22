package com.vywer.vywer;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity implements Const {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        private ListPreference mSettingsTransport;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            mSettingsTransport = (ListPreference)findPreference("settingsTransport");
            mSettingsTransport.setSummary(mSettingsTransport.getEntry());
            mSettingsTransport.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String value = newValue.toString();
                    String[] values = getResources().getStringArray(R.array.settings_transport_values);
                    String[] entries = getResources().getStringArray(R.array.settings_transport_entries);

                    for (int i = 0; i < values.length; i++) {
                        if (value.equalsIgnoreCase(values[i])) {
                            mSettingsTransport.setSummary(entries[i]);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }
}
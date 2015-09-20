package com.intel.bugkoops;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        final ListPreference cameraIdListPreference = (ListPreference) findPreference(
                getString(R.string.pref_cameraid_key));
        cameraIdListPreference.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                cameraIdListPreference.setValue(newValue.toString());
                preference.setSummary(cameraIdListPreference.getEntry());
                return true;
            }
        });

        final ListPreference flashStateListPreference = (ListPreference) findPreference(
                getString(R.string.pref_flashstate_key));
        flashStateListPreference.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                flashStateListPreference.setValue(newValue.toString());
                preference.setSummary(flashStateListPreference.getEntry());
                return true;
            }
        });

        final ListPreference focusingModeListPreference = (ListPreference) findPreference(
                getString(R.string.pref_focusingmode_key));
        focusingModeListPreference.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                focusingModeListPreference.setValue(newValue.toString());
                preference.setSummary(focusingModeListPreference.getEntry());
                return true;
            }
        });

        Preference resetDialogPreference = getPreferenceScreen().findPreference(
                getString(R.string.pref_resettodefault_key));

        final Intent startIntent = getIntent();

        resetDialogPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                overridePendingTransition(0, 0);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(startIntent);
                return false;
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}

package com.android.guille.tp6.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.guille.tp6.R;

public class SettingsFragment extends PreferenceFragmentCompat{

    public interface OnExtendedPreferenceChaneListener extends Preference.OnPreferenceChangeListener {
        public void setPreffix(String preffix);
        public void setSuffix(String suffix);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(findPreference("pref_delay"), getString(R.string.seconds));
        bindPreferenceSummaryToValue(findPreference("pref_URL_API"));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    private static void bindPreferenceSummaryToValue(String preffix,
                                                     Preference preference,
                                                     String suffix) {

        // Set the listener to watch for value changes.
        OnExtendedPreferenceChaneListener sBindPreferenceSummaryToValueListener =
                new OnExtendedPreferenceChaneListener() {
                    String mPreffix = "";
                    String mSuffix = "";

                    public void setPreffix(String preffix) {
                        mPreffix = preffix + " ";
                    }
                    public void setSuffix(String suffix) {
                        mSuffix = " " + suffix;
                    }

                    public boolean onPreferenceChange(Preference preference, Object value) {
                        String stringValue = value.toString();

                        if (preference instanceof ListPreference) {
                            // For list preferences, look up the correct display value in
                            // the preference's 'entries' list.
                            ListPreference listPreference = (ListPreference) preference;
                            int index = listPreference.findIndexOfValue(stringValue);

                            // Set the summary to reflect the new value.
                            preference.setSummary(index >= 0 ? mPreffix+listPreference.getEntries()[index]+mSuffix : null);

                        }else{
                            // For all other preferences, set the summary to the value's
                            // simple string representation.
                            preference.setSummary(mPreffix+stringValue+mSuffix);
                        }
                        return true;
                    }
                };

        sBindPreferenceSummaryToValueListener.setPreffix(preffix);
        sBindPreferenceSummaryToValueListener.setSuffix(suffix);

        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        bindPreferenceSummaryToValue("", preference, "");
    }

    private static void bindPreferenceSummaryToValue(Preference preference, String suffix) {
        bindPreferenceSummaryToValue("", preference, suffix);
    }

    private static void bindPreferenceSummaryToValue(String preffix, Preference preference) {
        bindPreferenceSummaryToValue(preffix, preference, "");
    }
}

package com.android.guille.tp6.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.guille.tp6.R;
import com.android.guille.tp6.fragment.SettingsFragment;

import java.util.List;

public class PreferenceActivity extends android.preference.PreferenceActivity{

    /*@Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return SettingsFragment.class.getName().equals(fragmentName);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

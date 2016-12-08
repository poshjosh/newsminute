package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.looseboxes.idisc.common.fragments.SettingsFragment;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.ui.CategoryPreferences;

public class SettingsActivity extends PreferenceActivity {

    public static final String EXTRA_BOOLEAN_DISPLAY_CATEGORIES = SettingsFragment.EXTRA_BOOLEAN_DISPLAY_CATEGORIES;

    private SettingsFragment settingsFragment;

    protected void onCreate(Bundle savedInstanceState) {
        try {

            Logx.getInstance().log(Log.DEBUG, getClass(), "onCreate");

            super.onCreate(savedInstanceState);

            settingsFragment = new SettingsFragment();

            Bundle arguments;
            if(settingsFragment.getArguments() == null) {
                arguments = new Bundle();
                settingsFragment.setArguments(arguments);
            }else{
                arguments = settingsFragment.getArguments();
            }
            final boolean displayCategories = this.getIntent().getBooleanExtra(SettingsActivity.EXTRA_BOOLEAN_DISPLAY_CATEGORIES, false);
            arguments.putBoolean(SettingsFragment.EXTRA_BOOLEAN_DISPLAY_CATEGORIES, displayCategories);

            getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();

            handleIntent(getIntent());

        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }

    protected void onNewIntent(Intent intent) {
        Logx.getInstance().log(Log.DEBUG, getClass(), "#onNewIntent");
        setIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) { }

    public SettingsFragment getSettingsFragment() {
        return settingsFragment;
    }
}

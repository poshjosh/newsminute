package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.looseboxes.idisc.common.fragments.SettingsFragment;
import com.looseboxes.idisc.common.util.Logx;

public class SettingsActivity extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Logx.log(Log.DEBUG, getClass(), "onCreate");
            super.onCreate(savedInstanceState);
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
            handleIntent(getIntent());
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
    }

    protected void onNewIntent(Intent intent) {
        Logx.log(Log.DEBUG, getClass(), "#onNewIntent");
        setIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
    }
}

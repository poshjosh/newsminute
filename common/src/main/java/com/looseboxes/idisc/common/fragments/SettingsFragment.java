package com.looseboxes.idisc.common.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.service.ServiceSchedulerImpl;
import com.looseboxes.idisc.common.service.StartFeedLoadServiceReceiver;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.ui.CategoryPreferences;
import com.looseboxes.idisc.common.ui.SourcePreferences;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    public static final String EXTRA_BOOLEAN_DISPLAY_CATEGORIES = SettingsFragment.class.getName()+".displayCategories.extraBoolean";

    private CategoryPreferences categoryPreferences;

    private SourcePreferences sourcePreferences;

    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.preferences);

        updateAutoSearchPreference();

        PreferenceScreen root = getPreferenceScreen();

        this.categoryPreferences = new CategoryPreferences(this.getContext());

        addPreference(root, this.categoryPreferences);

        this.sourcePreferences = new SourcePreferences(this.getContext());

        addPreference(root, this.sourcePreferences);

        root.findPreference(getString(R.string.pref_syncinterval_id)).setOnPreferenceChangeListener(this);
        root.findPreference(getString(R.string.pref_categories_id)).setOnPreferenceChangeListener(this);
//        root.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        final boolean displayCategories = this.isDisplayCategories(false);

        if(displayCategories) {
            try {

                this.categoryPreferences.show();

            }catch(Exception e) {
                Logx.getInstance().log(this.getClass(), e);
            }
        }
    }

    public Context getContext() {
        if (App.isAcceptableVersion(getActivity(), 23)) {
            return super.getContext();
        }
        return getActivity();
    }

    private void updateAutoSearchPreference() {
        try {
            Preference pref = findPreference(getString(R.string.pref_autosearch_id));
            String defaultValue = App.getPropertiesManager(getContext()).getString(PropertyName.defaultAutoSearchText);
            if (defaultValue != null) {
                pref.setDefaultValue(defaultValue);
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    private boolean addPreference(PreferenceScreen screen, Preference preference) {
        if (preference != null) {
            return screen.addPreference(preference);
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            return _onPreferenceChange(preference, newValue);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
            return true;
        }
    }

    private boolean _onPreferenceChange(Preference preference, Object newValue) {

        Context context = preference.getContext();

        final String preferenceKey = preference.getKey();

        if (context.getString(R.string.pref_syncinterval_id).equals(preferenceKey)) {

            final int delay;
            final long updatedFeedDownloadInterval = Long.parseLong(newValue.toString());
            long lastDownloadTime = FeedDownloadManager.getLastDownloadTime(context, -1);
            if (lastDownloadTime == -1) {
                delay = 500;
            } else {
                long periodSinceLastDownload = System.currentTimeMillis() - lastDownloadTime;
                if (periodSinceLastDownload < updatedFeedDownloadInterval) {
                    delay = (int)(updatedFeedDownloadInterval - periodSinceLastDownload);
                } else {
                    delay = 500;
                }
            }

            new ServiceSchedulerImpl().scheduleDefault(StartFeedLoadServiceReceiver.class, context, delay, updatedFeedDownloadInterval);

        }else if(context.getString(R.string.pref_categories_id).equals(preferenceKey)) {

            if(this.isDisplayCategories(false)) {

                this.getActivity().finish();
            }
        }

        return true;
    }

    private boolean isDisplayCategories(boolean defaultValue) {
        Bundle bundle = this.getArguments();
        return bundle == null ? defaultValue : bundle.getBoolean(EXTRA_BOOLEAN_DISPLAY_CATEGORIES, false);
    }

    public CategoryPreferences getCategoryPreferences() {
        return categoryPreferences;
    }

    public SourcePreferences getSourcePreferences() {
        return sourcePreferences;
    }
}

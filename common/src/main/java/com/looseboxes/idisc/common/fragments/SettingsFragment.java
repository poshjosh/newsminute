package com.looseboxes.idisc.common.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.service.ServiceScheduler;
import com.looseboxes.idisc.common.service.StartFeedLoadServiceReceiver;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        updateAutoSearchPreference();
        PreferenceScreen root = getPreferenceScreen();
        addCategoryPreference(root, R.string.pref_categories_id, R.string.pref_categories_title, R.string.pref_categories_summary, R.string.pref_categories_dialogtitle);
        addCategoryPreference(root, R.string.pref_sources_id, R.string.pref_sources_title, R.string.pref_sources_summary, R.string.pref_sources_dialogtitle);
        root.findPreference(getString(R.string.pref_syncinterval_id)).setOnPreferenceChangeListener(this);
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
            Logx.log(getClass(), e);
        }
    }

    private boolean addCategoryPreference(PreferenceScreen screen, int idRes, int titleRes, int summaryRes, int dialogTitleRes) {
        MultiSelectListPreference listPreference = createCategoryPreference(idRes, titleRes, summaryRes, dialogTitleRes);
        if (listPreference != null) {
            return screen.addPreference(listPreference);
        }
        return false;
    }

    private MultiSelectListPreference createCategoryPreference(int idRes, int titleRes, int summaryRes, int dialogTitleRes) {
        try {
            String key = getString(idRes);
            MultiSelectListPreference listPref = new MultiSelectListPreference(getActivity());
            listPref.setKey(key);
            listPref.setTitle(titleRes);
            listPref.setSummary(summaryRes);
            listPref.setDialogTitle(dialogTitleRes);
            CharSequence[] values = getValues(idRes);
            listPref.setEntries(values);
            listPref.setEntryValues(values);
            return listPref;
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return null;
        }
    }

    private CharSequence[] getValues(int prefKeyRes) {
        if (prefKeyRes == R.string.pref_categories_id) {
            Set<String> categories = App.getPropertiesManager(getContext()).getSet(PropertyName.categories);
            return (CharSequence[]) categories.toArray(new CharSequence[categories.size()]);
        } else if (prefKeyRes == R.string.pref_sources_id) {
            Map sources = App.getPropertiesManager(getContext()).getMap(PropertyName.sources);
            return (CharSequence[]) sources.values().toArray(new CharSequence[sources.size()]);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            return _onPreferenceChange(preference, newValue);
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return true;
        }
    }

    private boolean _onPreferenceChange(Preference preference, Object newValue) {
        Context context = preference.getContext();
        if (preference.getKey() == context.getString(R.string.pref_syncinterval_id)) {
            long delay;
            long lastDownloadTime = FeedDownloadManager.getLastDownloadTime(context, -1);
            if (lastDownloadTime == -1) {
                delay = 500;
            } else {
                long periodSinceLastDownload = System.currentTimeMillis() - lastDownloadTime;
                long updatedFeedDownloadInterval = Long.parseLong(newValue.toString());
                if (periodSinceLastDownload < updatedFeedDownloadInterval) {
                    delay = updatedFeedDownloadInterval - periodSinceLastDownload;
                } else {
                    delay = 500;
                }
            }
            ServiceScheduler.scheduleDefault(StartFeedLoadServiceReceiver.class, context, (int) delay);
        }
        return true;
    }

    private void updateCategoryPreferences(int prefKeyRes) {
        try {
            MultiSelectListPreference listPref = (MultiSelectListPreference) findPreference(getString(prefKeyRes));
            CharSequence[] values = getValues(prefKeyRes);
            listPref.setEntries(values);
            listPref.setEntryValues(values);
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }
}

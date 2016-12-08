package com.looseboxes.idisc.common.ui;

import android.content.Context;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.util.Map;

/**
 * Created by Josh on 9/12/2016.
 */
public class SourcePreferences extends MultiSelectListPreferenceImpl {

    public SourcePreferences(Context context) {

        super(context,
                R.string.pref_sources_id,
                R.string.pref_sources_title,
                R.string.pref_sources_summary,
                R.string.pref_sources_dialogtitle,
                getValues(context));
    }

    private static CharSequence [] getValues(Context context) {
        Map sources = App.getPropertiesManager(context).getMap(PropertiesManager.PropertyName.sources);
        return (CharSequence[]) sources.values().toArray(new CharSequence[sources.size()]);
    }
}


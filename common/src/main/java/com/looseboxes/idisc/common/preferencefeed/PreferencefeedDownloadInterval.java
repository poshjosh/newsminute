package com.looseboxes.idisc.common.preferencefeed;

import android.content.Context;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.activities.PreferenceFeedsActivity;
import com.looseboxes.idisc.common.util.DownloadInterval;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by Josh on 8/4/2016.
 */
public class PreferencefeedDownloadInterval extends DownloadInterval {

    public PreferencefeedDownloadInterval(PreferenceFeedsActivity activity) {
        this(activity, activity.getPreferenceType(), TimeUnit.HOURS.toMillis(App.getPropertiesManager(activity).getLong(PropertiesManager.PropertyName.syncIntervalHours)));
    }

    public PreferencefeedDownloadInterval(PreferenceFeedsActivity activity, long updateIntervalMillis) {
        this(activity, activity.getPreferenceType(), updateIntervalMillis);
    }

    public PreferencefeedDownloadInterval(Context context, Preferencefeeds.PreferenceType preferenceType) {
        this(context, preferenceType, TimeUnit.HOURS.toMillis(App.getPropertiesManager(context).getLong(PropertiesManager.PropertyName.syncIntervalHours)));
    }

    public PreferencefeedDownloadInterval(Context context, Preferencefeeds.PreferenceType preferenceType, long updateIntervalMillis) {
        super(context, Preferencefeeds.class.getName() + '.' + preferenceType.name() + ".lastDownloadTime.long", updateIntervalMillis);
    }
}

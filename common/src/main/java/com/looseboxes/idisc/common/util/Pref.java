package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Pref extends com.bc.android.core.util.Preferences {

    public static int getFeedDownloadLimit(Context context) {
        int limit = App.getPropertiesManager(context).getInt(PropertyName.feedDownloadLimit);
        int max = FeedDownloadManager.getMaxCacheSize(context);
        if(limit > max) {
            limit = max;
        }
        return limit;
    }

    public static int getLastSyncIntervalOption(Context context, int defaultValue) {
        try{
            String [] syncIntervalOptions = Pref.getSyncIntervalOptions(context);
            return syncIntervalOptions == null || syncIntervalOptions.length == 0 ?
                    defaultValue : Integer.parseInt(syncIntervalOptions[syncIntervalOptions.length - 1]);
        }catch(Exception e) {
            Logx.getInstance().log(Pref.class, e);
            return defaultValue;
        }
    }

    public static String [] getSyncIntervalOptions(Context context) {
        final String [] syncIntervalOptions = context.getResources().getStringArray(R.array.pref_syncinterval_values);
        return syncIntervalOptions == null ? new String[0] : syncIntervalOptions;
    }

    public static boolean isDisableVibration(Context ctx) {
        return getBoolean(ctx, R.string.pref_disablevibration_id, false);
    }

    public static void setWifiOnly(Context ctx, boolean val) {
        setBoolean(ctx, R.string.pref_wifionly_id, val);
    }

    public static boolean isWifiOnly(Context ctx, boolean defaultValue) {
        return getBoolean(ctx, R.string.pref_wifionly_id, defaultValue);
    }

    public static Set<String> getPreferredCategories(Context ctx) {
        return getStringSet(ctx, R.string.pref_categories_id, Collections.EMPTY_SET);
    }

    public static void setPreferredCategories(Context ctx, Set<String> set) {
        setStringSet(ctx, R.string.pref_categories_id, set);
    }

    public static Set<String> getPreferredSources(Context ctx) {
        return getStringSet(ctx, R.string.pref_sources_id, Collections.EMPTY_SET);
    }

    public static void setPreferredSources(Context ctx, Set<String> set) {
        setStringSet(ctx, R.string.pref_sources_id, set);
    }

    public static void setAvailableSources(Context context, Set<String> sources) {
        setStringSet(context, Pref.class.getName() + ".availableSources", (Set) sources);
    }

    public static Set getAvailableSources(Context context, Set defaultValues) {
        return getStringSet(context, Pref.class.getName() + ".availableSources", defaultValues);
    }

    public static void setAvailableCategories(Context context, Set<String> categories) {
        setStringSet(context, Pref.class.getName() + ".availableCategories", (Set) categories);
    }

    public static Set getAvailableCategories(Context context, Set defaultValues) {
        return getStringSet(context, Pref.class.getName() + ".availableCategories", defaultValues);
    }

    public static long getFeedDownloadIntervalMillis(Context context) {
        return TimeUnit.MINUTES.toMillis(Long.parseLong(getString(context, context.getString(R.string.pref_syncinterval_id), context.getString(R.string.pref_syncinterval_defaultvalue))));
    }

    public static Editor setAutoSearchText(Context context, String sval) {
        return setString(context, getAutoSearchTextPreferenceKey(context), sval, true);
    }

    public static String getAutoSearchText(Context context) {
        return getString(context, getAutoSearchTextPreferenceKey(context), App.getPropertiesManager(context).getString(PropertyName.defaultAutoSearchText));
    }

    private static String getAutoSearchTextPreferenceKey(Context context) {
        return context.getString(R.string.pref_autosearch_id);
    }

    public static boolean isSubscriptionActive(Context ctx) {
        return getBoolean(ctx.getApplicationContext(), Pref.class.getName() + ".subscription.active", false);
    }

    public static void setSubscritpionActive(Context ctx, boolean b) {
        if (b) {
            setInitiallyGrantedSubscription(ctx, null);
        }
        setBoolean(ctx.getApplicationContext(), Pref.class.getName() + ".subscription.active", b);
    }

    public static String getSubscriptionSku(Context ctx) {
        return getString(ctx.getApplicationContext(), Pref.class.getName() + ".subscription.sku", null);
    }

    public static void setSubscriptionSku(Context ctx, String sval) {
        if (sval != null) {
            setInitiallyGrantedSubscription(ctx, null);
        }
        setString(ctx.getApplicationContext(), Pref.class.getName() + ".subscription.sku", sval);
    }

    public static String getInitiallyGrantedSubscription(Context ctx) {
        return getString(ctx.getApplicationContext(), Pref.class.getName() + ".subscription.skuToActivate", null);
    }

    public static void setInitiallyGrantedSubscription(Context ctx, String sval) {
        setString(ctx.getApplicationContext(), Pref.class.getName() + ".subscription.skuToActivate", sval);
    }

    public static void clearSubscription(Context ctx) {
        setSubscriptionSku(ctx, null);
        setSubscritpionActive(ctx, false);
    }

    public static void setSubscriptionSupported(Context ctx, boolean b) {
        setBoolean(ctx, Pref.class.getName() + ".subscription.supported", b);
    }

    public static boolean isSubscriptionSupported(Context ctx) {
        return getBoolean(ctx, Pref.class.getName() + ".subscription.supported", true);
    }
}

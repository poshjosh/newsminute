package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Pref {
    public static String getGender(Context ctx, String defaultValue) {
        return getString(ctx, Pref.class.getName() + ".user.gender.preferenceKey", defaultValue);
    }

    public static void setGender(Context ctx, String val) {
        setString(ctx, Pref.class.getName() + ".user.gender.preferenceKey", val);
    }

    public static boolean isDisableVibration(Context ctx) {
        return getBoolean(ctx, R.string.pref_disablevibration_id, false);
    }

    public static Set<String> getPreferredCategories(Context ctx) {
        return getStringSet(ctx, R.string.pref_categories_id, Collections.EMPTY_SET);
    }

    public static Set<String> getPreferredSources(Context ctx) {
        return getStringSet(ctx, R.string.pref_sources_id, Collections.EMPTY_SET);
    }

    public static boolean getBoolean(Context ctx, int res_id, boolean defaultValue) {
        return getBoolean(ctx, ctx.getString(res_id), defaultValue);
    }

    public static boolean getBoolean(Context ctx, String key, boolean defaultValue) {
        try {
            defaultValue = getSharedPreferences(ctx).getBoolean(key, defaultValue);
        } catch (Exception e) {
            Logx.log(Pref.class, e);
        }
        return defaultValue;
    }

    public static void setBoolean(Context ctx, int res_id, boolean val) {
        setBoolean(ctx, res_id, val, true);
    }

    public static void setBoolean(Context ctx, String key, boolean val) {
        setBoolean(ctx, key, val, true);
    }

    public static Editor setBoolean(Context ctx, int res_id, boolean val, boolean commit) {
        return setBoolean(ctx, ctx.getString(res_id), val, commit);
    }

    public static Editor setBoolean(Context ctx, String key, boolean val, boolean commit) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(key, val);
        if (commit) {
            editor.apply();
        }
        return editor;
    }

    public static long getLong(Context ctx, int res_id, long defaultValue) {
        return getLong(ctx, ctx.getString(res_id), defaultValue);
    }

    public static long getLong(Context ctx, String key, long defaultValue) {
        try {
            defaultValue = getSharedPreferences(ctx).getLong(key, defaultValue);
        } catch (Exception e) {
            Logx.log(Pref.class, e);
        }
        return defaultValue;
    }

    public static void setLong(Context ctx, int res_id, long val) {
        setLong(ctx, res_id, val, true);
    }

    public static void setLong(Context ctx, String key, long val) {
        setLong(ctx, key, val, true);
    }

    public static Editor setLong(Context ctx, int res_id, long val, boolean commit) {
        return setLong(ctx, ctx.getString(res_id), val, commit);
    }

    public static Editor setLong(Context ctx, String key, long val, boolean commit) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putLong(key, val);
        if (commit) {
            editor.apply();
        }
        return editor;
    }

    public static String getString(Context ctx, int res_id, String defaultValue) {
        return getString(ctx, ctx.getString(res_id), defaultValue);
    }

    public static String getString(Context context, String prefKey, String defaultValue) {
        try {
            defaultValue = getSharedPreferences(context).getString(prefKey, defaultValue);
        } catch (Exception e) {
            Logx.log(Pref.class, e);
        }
        return defaultValue;
    }

    public static void setString(Context ctx, int res_id, String val) {
        setString(ctx, res_id, val, true);
    }

    public static void setString(Context ctx, String key, String val) {
        setString(ctx, key, val, true);
    }

    public static Editor setString(Context ctx, int res_id, String val, boolean commit) {
        return setString(ctx, ctx.getString(res_id), val, commit);
    }

    public static Editor setString(Context context, String key, String val, boolean commit) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, val);
        if (commit) {
            editor.apply();
        }
        return editor;
    }

    public static Set<String> getStringSet(Context ctx, int res_id, Set<String> defaultValue) {
        return getStringSet(ctx, ctx.getString(res_id), (Set) defaultValue);
    }

    public static Set<String> getStringSet(Context context, String prefKey, Set<String> defaultValue) {
        try {
            defaultValue = getSharedPreferences(context).getStringSet(prefKey, defaultValue);
        } catch (Exception e) {
            Logx.log(Pref.class, e);
        }
        return defaultValue;
    }

    public static void setStringSet(Context ctx, int res_id, Set<String> val) {
        setStringSet(ctx, res_id, (Set) val, true);
    }

    public static void setStringSet(Context ctx, String key, Set<String> val) {
        setStringSet(ctx, key, (Set) val, true);
    }

    public static Editor setStringSet(Context ctx, int res_id, Set<String> val, boolean commit) {
        return setStringSet(ctx, ctx.getString(res_id), (Set) val, commit);
    }

    public static Editor setStringSet(Context context, String key, Set<String> val, boolean commit) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putStringSet(key, val);
        if (commit) {
            editor.apply();
        }
        return editor;
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

    public static int getInt(Context context, String prefKey, int defaultValue) {
        try {
            defaultValue = getSharedPreferences(context).getInt(prefKey, defaultValue);
        } catch (Exception e) {
            Logx.debug(Pref.class, e);
        }
        return defaultValue;
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

    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
    }
}

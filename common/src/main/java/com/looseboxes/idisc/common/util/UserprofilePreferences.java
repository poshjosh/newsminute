package com.looseboxes.idisc.common.util;

import android.content.Context;

import com.bc.android.core.util.Util;

/**
 * Created by Josh on 9/23/2016.
 */
public class UserprofilePreferences {

    public String getCountryCode(Context context, String defaultValue) {
        return this.getUserprofileStringPreference(context, "countryCode", defaultValue);
    }

    public void setCountryCode(Context context, String value) {
        this.setUserprofileStringPreference(context, "countryCode", value);
    }

    public String getScreenname(Context context, String defaultValue) {
        return this.getUserprofileStringPreference(context, "screenname", defaultValue);
    }

    public void setScreenname(Context context, String value) {
        this.setUserprofileStringPreference(context, "screenname", value);
    }

    public String getGender(Context context, String defaultValue) {
        return this.getUserprofileStringPreference(context, "gender", defaultValue);
    }

    public void update(Context context, String countryCode, String screenname, String gender) {
        if(countryCode != null) {
            this.setCountryCode(context, countryCode);
        }
        if(screenname != null) {
            this.setScreenname(context, screenname);
        }
        if(gender != null) {
            this.setGender(context, gender);
        }
    }

    public void setGender(Context context, String value) {
        this.setUserprofileStringPreference(context, "gender", value);
    }

    private String getUserprofileStringPreference(Context context, String name, String defaultValue) {
        Util.requireNonNullOrEmpty(name, name + " cannot be null or an empty string");
        return Pref.getString(context, this.getClass().getName()+'.'+name+".preferenceKey", defaultValue);
    }

    private void setUserprofileStringPreference(Context context, String name, String value) {
        Util.requireNonNullOrEmpty(name, "Please provide a name for the preference whose value you want to set");
        Util.requireNonNullOrEmpty(value, name+" cannot be null or an empty string");
        Pref.setString(context, this.getClass().getName() + '.' + name + ".preferenceKey", value);
    }
}

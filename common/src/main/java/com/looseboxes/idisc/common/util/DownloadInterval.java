package com.looseboxes.idisc.common.util;

import android.content.Context;

public class DownloadInterval {

    private Context context;
    private String preferenceKey;
    private long updateIntervalMillis;

    public DownloadInterval(Context context, String preferenceKey, long updateIntervalMillis) {
        this.context = context;
        this.preferenceKey = preferenceKey;
        this.updateIntervalMillis = updateIntervalMillis;
    }

    public void destroy() {
        this.context = null;
        this.preferenceKey = null;
    }

    public boolean isNextDownloadDue() {
        return this.updateIntervalMillis < 1 || isElapsed(getLastDownloadTime(), this.updateIntervalMillis);
    }

    private boolean isElapsed(long start, long period) {
        return start >= 1 && System.currentTimeMillis() - start > period;
    }

    public void setLastDownloadTime(long lastDownloadTime) {
        Pref.setLong(getContext(), getPreferenceKey(), lastDownloadTime);
    }

    public long getLastDownloadTime() {
        return Pref.getLong(getContext(), getPreferenceKey(), -1);
    }

    public String getPreferenceKey() {
        return this.preferenceKey;
    }

    public Context getContext() {
        return this.context;
    }

    public long getUpdateIntervalMillis() {
        return this.updateIntervalMillis;
    }
}

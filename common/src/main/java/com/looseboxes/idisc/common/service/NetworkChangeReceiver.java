package com.looseboxes.idisc.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.Util;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (!App.isVisible()) {
            App.setContext(context);
        }
        Logx.log(Log.VERBOSE, getClass(), "Network Changed");
        if (getFailedNetworkAttempts(context) > 0) {
            attemptNetworkTask(context);
        }
        if (Util.isNetworkConnectedOrConnecting(context)) {
            setFailedNetworkAttempts(context, 0);
        } else {
            setFailedNetworkAttempts(context, getFailedNetworkAttempts(context) + 1);
        }
    }

    protected void attemptNetworkTask(Context context) {
        ServiceScheduler.scheduleNetworkService(context);
    }

    public static void setFailedNetworkAttempts(Context context, int n) {
        try {
            Pref.setLong(context.getApplicationContext(), getFailedNetworkAttemptsPreferenceKey(), (long) n);
        } catch (Exception e) {
            Logx.log(DownloadService.class, e);
        }
    }

    public static int getFailedNetworkAttempts(Context context) {
        try {
            return (int) Pref.getLong(context.getApplicationContext(), getFailedNetworkAttemptsPreferenceKey(), 0);
        } catch (Exception e) {
            Logx.log(DownloadService.class, e);
            return 0;
        }
    }

    private static String getFailedNetworkAttemptsPreferenceKey() {
        return DownloadService.class.getName() + ".FailedNetworkAttempts.preferencekey";
    }
}

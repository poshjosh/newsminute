package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

/**
 * Created by Josh on 9/23/2016.
 */
public class LogAppLaunch extends RemoteLog {

    public LogAppLaunch(Context context) {
        super(context, APP_LAUNCH);
        this.addOutputParameter("launchtime", Long.toString(System.currentTimeMillis()));
    }
}


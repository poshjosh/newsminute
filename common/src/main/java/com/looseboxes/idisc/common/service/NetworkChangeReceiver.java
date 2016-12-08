package com.looseboxes.idisc.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        if (!App.isVisible()) {
            App.setContext(context);
        }

        Logx.getInstance().log(Log.VERBOSE, getClass(), "Network Changed. Network connected/connecting: {0}", NewsminuteUtil.isPreferredNetworkConnectedOrConnecting(context));

        if (FeedDownloadManager.isNextDownloadDue(context) && NewsminuteUtil.isPreferredNetworkConnectedOrConnecting(context)) {
            attemptNetworkTask(context);
        }
    }

    protected void attemptNetworkTask(Context context) {
        new ServiceSchedulerImpl().scheduleNetworkService(context);
    }
}

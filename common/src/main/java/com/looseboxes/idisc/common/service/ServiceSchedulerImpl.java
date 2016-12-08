package com.looseboxes.idisc.common.service;

import android.content.Context;

import com.looseboxes.idisc.common.App;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

public class ServiceSchedulerImpl extends com.bc.android.core.util.ServiceScheduler {

    public void scheduleNetworkService(Context context) {

        int delay;
        try {
            delay = App.getPropertiesManager(context).getInt(PropertyName.feedLoadServiceDelay);
        } catch (Exception e) {
            delay = 500;
            Logx.getInstance().log(ServiceSchedulerImpl.class, e);
        }

        final long interval = Pref.getFeedDownloadIntervalMillis(context);

        scheduleDefault(StartFeedLoadServiceReceiver.class, context, delay, interval);
    }
}

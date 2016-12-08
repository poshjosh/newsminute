package com.looseboxes.idisc.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.looseboxes.idisc.common.App;

public class BootCompleteBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (!App.isVisible()) {
            App.setContext(context);
        }
        new ServiceSchedulerImpl().scheduleNetworkService(context);
    }
}

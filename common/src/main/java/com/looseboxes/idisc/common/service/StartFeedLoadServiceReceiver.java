package com.looseboxes.idisc.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.looseboxes.idisc.common.util.Logx;

public class StartFeedLoadServiceReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Logx.log(Log.DEBUG, getClass(), "Starting service: {0}", DownloadService.class.getName());
        context.startService(new Intent(context, DownloadService.class));
    }
}

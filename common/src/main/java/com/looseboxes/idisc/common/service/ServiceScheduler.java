package com.looseboxes.idisc.common.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import java.util.Calendar;

public class ServiceScheduler {
    public static void scheduleNetworkService(Context context) {
        int delay;
        try {
            delay = App.getPropertiesManager(context).getInt(PropertyName.feedLoadServiceDelay);
        } catch (Exception e) {
            delay = 500;
            Logx.log(ServiceScheduler.class, e);
        }
        scheduleDefault(StartFeedLoadServiceReceiver.class, context, delay);
    }

    public static void scheduleDefault(Class serviceClass, Context context, int delay) {
        schedule(serviceClass, context, delay, Pref.getFeedDownloadIntervalMillis(context), 1, true);
    }

    @TargetApi(19)
    public static void schedule(Class serviceClass, Context context, int delay, long interval, int type, boolean inexact) {
        try {
            AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pending = PendingIntent.getBroadcast(context, 0, new Intent(context, serviceClass), PendingIntent.FLAG_UPDATE_CURRENT);
            long startTime = getStartTime(delay, type);
            if (inexact) {
                if (delay < 0) {
                    service.set(type, startTime, pending);
                } else {
                    service.setRepeating(type, startTime, interval, pending);
                }
            } else if (delay >= 0) {
                service.setRepeating(type, startTime, interval, pending);
            } else if (App.isAcceptableVersion(context, 19)) {
                service.setExact(type, startTime, pending);
            } else {
                service.set(type, startTime, pending);
            }
        } catch (Exception e) {
            Logx.log(ServiceScheduler.class, e);
        }
    }

    public static long getStartTime(int delay, int type) {
        long startTime;
        switch(type) {
            case AlarmManager.RTC:
            case AlarmManager.RTC_WAKEUP:
                if(delay > 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    // start delay milliseconds later
                    cal.add(Calendar.MILLISECOND, delay);
                    startTime = cal.getTimeInMillis();
                }else{
                    startTime = System.currentTimeMillis();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return startTime;
    }
}

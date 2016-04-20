package com.looseboxes.idisc.common.notice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification.BigPictureStyle;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Vibrator;
import android.text.Html;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;

public class NotificationHandler {
    public static final int COMMENT_NOTICE_ID = 2;
    public static final int FEED_NOTICE_ID = 1;
    private final Context context;

    public NotificationHandler(Context context) {
        this.context = context;
    }

    public void playNotificationSound() {
        try {
            RingtoneManager.getRingtone(this.context, RingtoneManager.getDefaultUri(COMMENT_NOTICE_ID)).play();
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
    }

    public void vibrate(long millis) {
        try {
            Vibrator vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(millis);
            }
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
    }

    public Builder getBuilder(int smallIcon, Bitmap largeIcon, String noticeTitle, String noticeText) {
        Builder mBuilder = new Builder(this.context);
        if (smallIcon > 0) {
            mBuilder.setSmallIcon(smallIcon);
        }
        mBuilder.setLargeIcon(largeIcon);
        mBuilder.setContentTitle(format(noticeTitle));
        mBuilder.setContentText(format(noticeText));
        return mBuilder;
    }

    @TargetApi(16)
    public BigPictureStyle getBigPictureStyle(Bitmap largeIcon, Bitmap picture, String title, String summary) {
        if (isCompatibleAndroidVersion()) {
            BigPictureStyle style = new BigPictureStyle();
            style.bigLargeIcon(largeIcon);
            style.bigPicture(picture);
            style.setBigContentTitle(format(title));
            style.setSummaryText(format(summary));
            return style;
        }
        throw new UnsupportedOperationException("Api level 16+ required");
    }

    @TargetApi(16)
    public BigTextStyle getBigTextStyle(String title, String summary, String bigText) {
        if (isCompatibleAndroidVersion()) {
            BigTextStyle style = new BigTextStyle();
            style.bigText(format(bigText));
            style.setBigContentTitle(format(title));
            style.setSummaryText(format(summary));
            return style;
        }
        throw new UnsupportedOperationException("Api level 16+ required");
    }

    public boolean fireDefaultNotification(Builder notificationBuilder, Class<? extends Activity> targetActivityClass, String extra_key, String extra_value) {
        boolean shown = showNotification(FEED_NOTICE_ID, notificationBuilder, targetActivityClass, extra_key, extra_value);
        playNotificationSound();
        if (!Pref.isDisableVibration(this.context)) {
            vibrate(500);
        }
        return shown;
    }

    @TargetApi(16)
    public boolean showNotification(int noticeId, Builder notificationBuilder, Class<? extends Activity> targetActivityClass, String extra_key, String extra_value) {
        if (!isCompatibleAndroidVersion()) {
            return false;
        }
        Intent resultIntent = new Intent(this.context, targetActivityClass);
        if (!(extra_key == null || extra_value == null)) {
            resultIntent.putExtra(extra_key, extra_value);
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
        stackBuilder.addParentStack(targetActivityClass);
        stackBuilder.addNextIntent(resultIntent);
        notificationBuilder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        notificationBuilder.setAutoCancel(true);
        ((NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(noticeId, notificationBuilder.build());
        return true;
    }

    private CharSequence format(String s) {
        if (s == null) {
            return null;
        }
        if (s.contains("<")) {
            CharSequence output = Html.fromHtml(s);
            return s;
        }
        String output2 = s;
        return s;
    }

    public boolean isCompatibleAndroidVersion() {
        return App.isAcceptableVersion(this.context, 16);
    }

    public Context getContext() {
        return this.context;
    }
}

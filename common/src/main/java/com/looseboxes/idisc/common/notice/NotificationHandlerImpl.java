package com.looseboxes.idisc.common.notice;

/**
 * Created by Josh on 12/3/2016.
 */

import android.app.NotificationManager;
import android.content.Context;

import com.bc.android.core.notice.NotificationHandler;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;

public class NotificationHandlerImpl extends NotificationHandler {

    private final int noticeiconResourceid;

    public NotificationHandlerImpl(Context context) {
        super(context);
        this.noticeiconResourceid = App.getNoticeIconResourceId(context);
    }

    public void onViewed(int id) {
        ((NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
    }

    public String format(String s) {
        return s == null ? null : s.replaceAll("\\s|\\p{Punct}", "").toLowerCase();
    }

    public String getTitle() {
        return getContext().getString(R.string.msg_defaultnoticetext, new Object[]{getAppLabel()});
    }

    public String getAppLabel() {
        return getContext().getString(R.string.app_label);
    }

    public final int getNoticeiconResourceid() {
        return noticeiconResourceid;
    }
}

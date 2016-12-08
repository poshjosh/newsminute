package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.looseboxes.idisc.common.User;

/**
 * Created by Josh on 7/5/2016.
 */
public class RemoteLog extends AbstractReadBoolean {

    public static final int INSTALLATION_ERROR = 1;
    public static final int SERVICE_UNAVAILABLE = 2;
    public static final int APP_LAUNCH = 3;

    public RemoteLog(Context context, int type) {
        super(context, "");

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter("id", Integer.toString(type));

        this.setNoUI(true);
    }

    public void onSuccess(Boolean download) { }

    public String getOutputKey() {
        return "log";
    }
}

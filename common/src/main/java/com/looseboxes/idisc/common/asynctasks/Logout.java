package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.Logx;

public class Logout extends DefaultReadTask<Object> {
    private final Context context;

    public Logout(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_logout);
    }

    public void onSuccess(Object download) {
        User.getInstance().setDetails(this.context, null);
        Logx.debug(getClass(), "Logout successful. Server response: {0}", download);
        if (!isNoUI()) {
            displayMessage("Logout successful", 1);
        }
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "logout";
    }
}

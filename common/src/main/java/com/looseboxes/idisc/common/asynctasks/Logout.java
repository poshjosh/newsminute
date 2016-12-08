package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.bc.android.core.util.Logx;

public class Logout extends AbstractReadTask<Object> {

    public Logout(Context context) {
        super(context, context.getString(R.string.err_logout));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!this.isNoUI()) {
            Popup.getInstance().show(this.getContext(), R.string.msg_loggingout, Toast.LENGTH_SHORT);
        }
    }

    public void onSuccess(Object download) {
        User.getInstance().setDetails(this.getContext(), null);
        Logx.getInstance().debug(getClass(), "Logout successful. Server response: {0}", download);
        if (!isNoUI()) {
            displayMessage(this.getContext().getString(R.string.msg_logout_success), Toast.LENGTH_SHORT);
        }
    }

    public String getOutputKey() {
        return "logout";
    }
}

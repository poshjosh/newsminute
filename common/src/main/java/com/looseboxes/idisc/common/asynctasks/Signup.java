package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;

public class Signup extends AbstractReadTask<Object> {

    public Signup(Context context, String email, String username, char[] password, boolean sendActivationMail) {

        super(context, context.getString(R.string.err_signup));

        Util.requireNonNullOrEmpty(email, context.getString(R.string.err_required_s, AuthuserNames.emailaddress));

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter(AuthuserNames.emailaddress, email);
        this.addOutputParameter(FeeduserNames.emailAddress, email);
        if (username != null) {
            this.addOutputParameter(AuthuserNames.username, username);
        }
        if (password != null) {
            this.addOutputParameter(FeeduserNames.password, new String(password));
        }
        this.addOutputParameter("sendregistrationmail", Boolean.toString(sendActivationMail));
    }

    public String getOutputKey() {
        return "signup";
    }

    public void onSuccess(Object download) {
        Context context = this.getContext();
        String msg;
        if (Boolean.TRUE.equals(download)) {
            String app_label = context.getString(R.string.app_label);
            msg = context.getString(R.string.msg_signupsuccess, new Object[]{app_label});
        } else {
            msg = context.getString(R.string.err_signup);
        }
        Popup.getInstance().show(context, msg, Toast.LENGTH_SHORT);
    }
}

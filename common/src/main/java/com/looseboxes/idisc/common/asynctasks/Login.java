package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;
import com.bc.android.core.util.Logx;

public class Login extends AbstractReadTask<Object> {

    public Login(Context context, String email, String password) {
        super(context, context.getString(R.string.err_login));
        Util.requireNonNullOrEmpty(email, context.getString(R.string.err_required_s, AuthuserNames.emailaddress));
        Util.requireNonNullOrEmpty(password, context.getString(R.string.err_required_s, AuthuserNames.password));

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter(AuthuserNames.emailaddress, email);
        this.addOutputParameter(FeeduserNames.emailAddress, email);
        this.addOutputParameter(AuthuserNames.password, password);
    }

    public void onSuccess(Object download) {
        Logx.getInstance().debug(getClass(), "Login successful. Server response: {0}", download);
        if (!isNoUI()) {
            displayMessage("Login Successful", 0);
        }
    }

    public String getOutputKey() {
        return "login";
    }
}

package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;

public class Requestpassword extends AbstractReadBoolean {

    public Requestpassword(Context context, String emailAddress) {
        super(context, context.getString(R.string.err_requestpassword));
        Util.requireNonNullOrEmpty(emailAddress, context.getString(R.string.err_required_s, AuthuserNames.emailaddress));
        addOutputParameter(AuthuserNames.emailaddress, emailAddress);
        addOutputParameter(FeeduserNames.emailAddress, emailAddress);
    }

    public void onSuccess(Boolean success) {
        if (success.booleanValue()) {
            final Context context = this.getContext();
            String msg = context.getString(R.string.msg_emailsentto_s_checkemail, User.getInstance().getEmailAddress(getContext()));
            Popup.getInstance().alert(context, msg, null, context.getString(R.string.msg_ok));
        }
    }

    public String getOutputKey() {
        return "requestpassword";
    }
}

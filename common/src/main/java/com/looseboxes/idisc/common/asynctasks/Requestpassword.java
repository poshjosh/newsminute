package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.notice.Popup;
import java.util.Collections;

public class Requestpassword extends DefaultReadTask<Boolean> {
    private final Context context;

    public Requestpassword(Context context, String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        setOutputParameters(Collections.singletonMap(AuthuserNames.emailaddress, emailAddress));
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_requestpassword);
    }

    public void onSuccess(Boolean success) {
        if (success.booleanValue()) {
            Popup.alert(this.context, "An email has been sent to " + User.getInstance().getEmailAddress(getContext()) + ". Please check your email for instructions");
        }
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "requestpassword";
    }
}

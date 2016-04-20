package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Activateuser extends DefaultReadTask<Boolean> {
    private final Context context;

    public Activateuser(Context context, String code) throws IOException {
        if (code == null) {
            throw new NullPointerException();
        }
        String id = App.getId(context);
        if (id == null) {
            throw new UnsupportedOperationException();
        }
        Map<String, String> params = new HashMap(4, 1);
        params.put("code", code);
        params.put(InstallationNames.installationkey, id);
        setOutputParameters(params);
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_activation);
    }

    public void onSuccess(Boolean success) {
        if (success.booleanValue()) {
            Popup.alert(this.context, "An email has been sent to " + User.getInstance().getEmailAddress(getContext()) + ". Please check your email for instructions");
            return;
        }
        Logx.debug(getClass(), "Request NOT SUCCESSFUL", success);
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "requestpassword";
    }
}

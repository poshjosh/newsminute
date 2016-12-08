package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import java.io.IOException;

public class Activateuser extends AbstractReadBoolean {

    public Activateuser(Context context, String code) throws IOException {

        super(context, context.getString(R.string.err_activation));

        Util.requireNonNull(code);

        String id = Util.requireNonNull(App.getId(context));

        this.addOutputParameter("code", code);
        this.addOutputParameter(InstallationNames.installationkey, id);
    }

    public void onSuccess(Boolean success) {
        if (success.booleanValue()) {
            final Context context = this.getContext();
            String msg = context.getString(R.string.msg_emailsentto_s_checkemail, User.getInstance().getEmailAddress(context));
            Popup.getInstance().alert(this.getContext(), msg, null, context.getString(R.string.msg_ok));
            return;
        }
        Logx.getInstance().debug(getClass(), "Request NOT SUCCESSFUL", success);
    }

    public String getOutputKey() {
        return "requestpassword";
    }
}

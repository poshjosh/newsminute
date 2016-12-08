package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;

import org.json.simple.JSONObject;

public abstract class Getuser extends AbstractReadTask<JSONObject> {

    public Getuser(Context context, String email, String username, String password, boolean create) {

        this(context, context.getString(R.string.err_loading_userdetails),
                email, username, password, create);
    }

    public Getuser(Context context, String errorMessage,
                   String email, String username, String password, boolean create) {

        super(context, errorMessage);

        Util.requireNonNullOrEmpty(email, context.getString(R.string.err_required_s, AuthuserNames.emailaddress));

        // This must come first
        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter(AuthuserNames.emailaddress, email);
        this.addOutputParameter(FeeduserNames.emailAddress, email);
        if (username != null) {
            this.addOutputParameter(AuthuserNames.username, username);
        }
        if (password != null) {
            this.addOutputParameter(FeeduserNames.password, password);
        }
        this.addOutputParameter("create", Boolean.toString(create));
    }

    public String getOutputKey() {
        return "getuser";
    }
}

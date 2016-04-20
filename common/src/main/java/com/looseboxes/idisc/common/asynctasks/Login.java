package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;
import com.looseboxes.idisc.common.util.Logx;

import java.util.HashMap;
import java.util.Map;

public class Login extends DefaultReadTask<Object> {
    private final Context context;

    public Login(Context context, String email, String password) {
        if (email == null || password == null) {
            throw new NullPointerException();
        }
        Map<String, String> loginParams = new HashMap(4, 1.0f);
        loginParams.put(AuthuserNames.emailaddress, email);
        loginParams.put(FeeduserNames.password, password);
        setOutputParameters(loginParams);
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_login);
    }

    public void onSuccess(Object download) {
        Logx.debug(getClass(), "Login successful. Server response: {0}", download);
        if (!isNoUI()) {
            displayMessage("Login Successful", 0);
        }
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "login";
    }

    protected void checkNull(Map<String, String> params, String key) throws NullPointerException {
        if (params.get(key) == null) {
            throw new NullPointerException("Required: " + key);
        }
    }

    public void setOutputParameters(Map<String, String> loginParameters) {
        checkNull(loginParameters, AuthuserNames.emailaddress);
        checkNull(loginParameters, FeeduserNames.password);
        super.setOutputParameters(loginParameters);
    }
}

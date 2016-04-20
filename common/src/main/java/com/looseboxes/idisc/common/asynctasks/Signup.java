package com.looseboxes.idisc.common.asynctasks;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;

import java.util.HashMap;
import java.util.Map;

public abstract class Signup extends DefaultReadTask<Object> {
    public Signup(String email, String username, char[] password, boolean sendActivationMail) {
        if (email == null) {
            throw new NullPointerException();
        }
        Map<String, String> loginParams = new HashMap(6, 1.0f);
        loginParams.put(AuthuserNames.emailaddress, email);
        if (username != null) {
            loginParams.put(AuthuserNames.username, username);
        }
        if (password != null) {
            loginParams.put(FeeduserNames.password, new String(password));
        }
        loginParams.put("sendregistrationmail", Boolean.toString(sendActivationMail));
        setOutputParameters(loginParams);
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_signup);
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "signup";
    }

    public void setOutputParameters(Map<String, String> loginParameters) {
        checkNull(loginParameters, AuthuserNames.emailaddress);
        super.setOutputParameters(loginParameters);
    }

    protected void checkNull(Map<String, String> params, String key) throws NullPointerException {
        if (params.get(key) == null) {
            throw new NullPointerException("Required: " + key);
        }
    }
}

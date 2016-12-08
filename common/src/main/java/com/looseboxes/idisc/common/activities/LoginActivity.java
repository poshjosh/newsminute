package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.listeners.AbstractOnClickListenerForAsyncTask;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

public class LoginActivity extends AbstractSingleTopActivity {

    private class LoginButtonOnclickListener extends AbstractOnClickListenerForAsyncTask {

        private LoginButtonOnclickListener() {
            super(LoginActivity.this);
        }

        protected boolean accept(View v) {
            return v.equals(this.getActivity().findViewById(R.id.login_okbutton));
        }

        protected void execute(View v) {
            final Activity activity = this.getActivity();
            Editable email = ((EditText) getActivity().findViewById(R.id.login_email)).getText();
            Editable password = ((EditText) getActivity().findViewById(R.id.login_password)).getText();
            if(email == null) {
                Popup.getInstance().show(activity, R.string.msg_enter_email, Toast.LENGTH_SHORT);
            }else if(password == null) {
                Popup.getInstance().show(activity, R.string.msg_enter_password, Toast.LENGTH_SHORT);
            }else {
                Popup.getInstance().show(activity, R.string.msg_loggingin, Toast.LENGTH_SHORT);
                User.getInstance().login(activity, email.toString(), null, password.toString());
            }
        }

        protected int getErrorMessageResourceId() {
            return R.string.err_login;
        }
    }

    public int getContentViewId() {
        return R.layout.login;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {
            super.doCreate(savedInstanceState);
            OnClickListener listener = new LoginButtonOnclickListener();
            ((Button) findViewById(R.id.login_okbutton)).setOnClickListener(listener);
            ((Button) findViewById(R.id.login_skipbutton)).setOnClickListener(listener);
        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }
}

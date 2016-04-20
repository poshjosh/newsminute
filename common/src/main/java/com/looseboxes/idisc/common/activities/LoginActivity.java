package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.listeners.AbstractOnClickListenerForAsyncTask;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;

public class LoginActivity extends AbstractSingleTopActivity {

    private class LoginButtonOnclickListener extends AbstractOnClickListenerForAsyncTask {
        private LoginButtonOnclickListener() {
            super(LoginActivity.this);
        }

        protected boolean accept(View v) {
            return v.equals(LoginActivity.this.findViewById(R.id.login_okbutton));
        }

        protected void execute(View v) {
            Editable email = ((EditText) getActivity().findViewById(R.id.login_email)).getText();
            Editable password = ((EditText) getActivity().findViewById(R.id.login_password)).getText();
            if (email != null && password != null) {
                Popup.show(LoginActivity.this, (Object) "...Loging in", 0);
                User.getInstance().login(LoginActivity.this, email.toString(), null, password.toString());
            }
        }

        protected int getErrorMessageResourceId() {
            return R.string.err_login;
        }
    }

    public int getContentView() {
        return R.layout.login;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {
            super.doCreate(savedInstanceState);
            doCreate();
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
    }

    private void doCreate() {
        OnClickListener listener = new LoginButtonOnclickListener();
        ((Button) findViewById(R.id.login_okbutton)).setOnClickListener(listener);
        ((Button) findViewById(R.id.login_skipbutton)).setOnClickListener(listener);
    }
}

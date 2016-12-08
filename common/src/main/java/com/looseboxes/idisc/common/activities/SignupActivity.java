package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.listeners.CheckIfExistsOnFocusChange;

public class SignupActivity extends AbstractSingleTopActivity implements OnClickListener {

    public static final String EXTRA_STRING_CALLING_ACTIVITY_CLASS_NAME = SignupActivity.class.getName()+".callingActivityClassname.extra.string";

    private EditText emailView;
    private EditText passwordView;
    private EditText usernameView;

    public int getContentViewId() {
        return R.layout.signup;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {

            super.doCreate(savedInstanceState);

            this.emailView = (EditText) findViewById(R.id.signup_email);
            this.usernameView = (EditText) findViewById(R.id.signup_username);
            String screenname = User.getInstance().getScreenname(this, null);
            if (screenname != null) {
                this.usernameView.setText(screenname);
            }
            this.passwordView = (EditText) findViewById(R.id.signup_password);
            ((Button) findViewById(R.id.signup_okbutton)).setOnClickListener(this);
            ((Button) findViewById(R.id.signup_skipbutton)).setOnClickListener(this);
            ((Button) findViewById(R.id.navigation_nextbutton)).setOnClickListener(this);
            ((Button) findViewById(R.id.navigation_backbutton)).setOnClickListener(this);

            TextView progressText = (TextView) findViewById(R.id.signup_title);

            this.emailView.setOnFocusChangeListener(
                    new CheckIfExistsOnFocusChange("feeduser", FeeduserNames.emailAddress, progressText)
            );
            this.usernameView.setOnFocusChangeListener(
                    new CheckIfExistsOnFocusChange("feeduser", AuthuserNames.username, progressText)
            );

        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }

    public void onClick(View v) {

        try {

            Logx.getInstance().log(Log.VERBOSE, getClass(), "onClick");

            final int id = v.getId();

            if (id == R.id.signup_okbutton || id == R.id.navigation_nextbutton) {

                Editable email = this.emailView.getText();
                Editable username = this.usernameView.getText();
                Editable password = this.passwordView.getText();

                if (email == null) {

                    Popup.getInstance().show(this, getString(R.string.msg_enter_email), Toast.LENGTH_SHORT);

                } else if (password == null) {

                    Popup.getInstance().show(this, getString(R.string.msg_enter_password), Toast.LENGTH_SHORT);

                } else {

                    this.signup();
                }

                if(id == R.id.navigation_nextbutton) {

                    startActivity(new Intent(this, MainActivity.class));
                }
            }else
            if(id == R.id.signup_skipbutton || id == R.id.navigation_backbutton) {

                startActivity(new Intent(this, MainActivity.class));
            }
        } catch (Exception e) {

            Popup.getInstance().show(this, R.string.err_signup, Toast.LENGTH_LONG);

            Logx.getInstance().log(getClass(), e);

        } finally {

            if(this.isCallingActivityClass(WelcomeOptionsActivity.class)) {

                ((DefaultApplication) getApplication()).onWelcomeProcessFinished(this);
            }
        }
    }

    public boolean isCallingActivityClass(Class<? extends Activity> aClass) {

        final String callingActivityClassname = this.getIntent().getStringExtra(EXTRA_STRING_CALLING_ACTIVITY_CLASS_NAME);

        Logx.getInstance().debug(this.getClass(), "Calling activity class name: {0}", callingActivityClassname);

        return aClass.getName().equals(callingActivityClassname);
    }


    private boolean signup() {

        if(!Util.isNetworkConnectedOrConnecting(this)) {
            Popup.getInstance().show(this, R.string.err_internetunavailable, Toast.LENGTH_SHORT);
            return false;
        }

        Editable emailEditable = this.emailView.getText();
        Editable usernameEditable = this.usernameView.getText();
        Editable passwordEditable = this.passwordView.getText();

        if (emailEditable == null || passwordEditable == null) {
            return false;
        }

        String username;
        if (usernameEditable == null) {
            username = null;
        } else {
            username = usernameEditable.toString();
        }

        Popup.getInstance().show(this, R.string.msg_signingup, Toast.LENGTH_SHORT);

        User.getInstance().signup(this, emailEditable.toString(), username, passwordEditable.toString());

        return true;
    }
}

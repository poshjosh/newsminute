package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.asynctasks.Signup;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.CheckIfValueExists;
import com.looseboxes.idisc.common.util.Logx;
import java.util.Collections;
import java.util.Map;

public class SignupActivity extends AbstractSingleTopActivity implements OnClickListener {
    private CheckIfValueExists confirm;
    private EditText emailView;
    private EditText passwordView;
    private TextView progressText;
    private EditText usernameView;

    /* renamed from: com.looseboxes.idisc.common.activities.SignupActivity.1 */
    class AnonymousClass1 extends CheckIfValueExists {
        AnonymousClass1(Context x0) {
            super(x0);
        }

        protected void proceed(String table, Map<String, String> map) {
            boolean success = SignupActivity.this.signup();
        }
    }

    /* renamed from: com.looseboxes.idisc.common.activities.SignupActivity.2 */
    class AnonymousClass2 extends Signup {
        AnonymousClass2(String x0, String x1, char[] x2, boolean x3) {
            super(x0, x1, x2, x3);
        }

        public void onSuccess(Object download) {
            String msg;
            if (Boolean.TRUE.equals(download)) {
                String app_label = SignupActivity.this.getString(R.string.app_label);
                msg = SignupActivity.this.getString(R.string.msg_signupsuccess, new Object[]{app_label});
            } else {
                msg = SignupActivity.this.getString(R.string.err_signup);
            }
            Popup.alert(SignupActivity.this, msg);
        }

        public Context getContext() {
            return SignupActivity.this;
        }
    }

    public int getContentView() {
        return R.layout.signup;
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
        this.emailView = (EditText) findViewById(R.id.signup_email);
        this.usernameView = (EditText) findViewById(R.id.signup_username);
        String screenname = User.getInstance().getScreenName(this);
        if (screenname != null) {
            this.usernameView.setText(screenname);
        }
        this.passwordView = (EditText) findViewById(R.id.signup_password);
        ((Button) findViewById(R.id.signup_okbutton)).setOnClickListener(this);
        ((Button) findViewById(R.id.signup_skipbutton)).setOnClickListener(this);
        ((Button) findViewById(R.id.welcomeprocess_nextbutton)).setOnClickListener(this);
        ((Button) findViewById(R.id.welcomeprocess_backbutton)).setOnClickListener(this);
    }

    public void onClick(View v) {
        try {
            Logx.log(Log.VERBOSE, getClass(), "onClick");
            int id = v.getId();
            if (id == R.id.signup_okbutton || id == R.id.welcomeprocess_nextbutton) {
                Editable email = this.emailView.getText();
                Editable username = this.usernameView.getText();
                Editable password = this.passwordView.getText();
                if (email == null) {
                    Popup.show((Activity) this, getString(R.string.msg_enter_email), 0);
                    return;
                } else if (password == null) {
                    Popup.show((Activity) this, getString(R.string.msg_enter_password), 0);
                    ((DefaultApplication) getApplication()).onWelcomeProcessFinished(this);
                    startActivity(new Intent(this, MainActivity.class));
                    return;
                } else {
                    if (this.confirm == null) {
                        this.confirm = new AnonymousClass1(this);
                        if (this.progressText == null) {
                            this.progressText = (TextView) findViewById(R.id.signup_title);
                        }
                        this.confirm.setProgressText(this.progressText);
                    }
                    this.confirm.execute("feeduser", Collections.singletonMap(FeeduserNames.emailaddress, email.toString()));
                }
            } else if (id != R.id.signup_skipbutton && id == R.id.welcomeprocess_backbutton) {
            }
            ((DefaultApplication) getApplication()).onWelcomeProcessFinished(this);
            startActivity(new Intent(this, MainActivity.class));
        } catch (Exception e) {
            Popup.show((Activity) this, (Object) "Failed to sign up", 1);
            Logx.log(getClass(), e);
        } finally {
            ((DefaultApplication) getApplication()).onWelcomeProcessFinished(this);
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private boolean signup() {
        Editable email = this.emailView.getText();
        Editable username = this.usernameView.getText();
        Editable password = this.passwordView.getText();
        if (email == null || password == null) {
            return false;
        }
        String str;
        String obj = email.toString();
        if (username == null) {
            str = null;
        } else {
            str = username.toString();
        }
        new AnonymousClass2(obj, str, password.toString().toCharArray(), true).execute();
        return true;
    }
}

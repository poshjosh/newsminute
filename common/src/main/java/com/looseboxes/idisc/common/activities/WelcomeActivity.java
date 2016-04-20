package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.StorageIOException;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.util.CheckIfValueExists;
import com.looseboxes.idisc.common.util.Logx;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class WelcomeActivity extends AbstractSingleTopActivity implements OnClickListener {
    private CheckIfValueExists confirm;
    private EditText editText;
    private TextView progressText;

    /* renamed from: com.looseboxes.idisc.common.activities.WelcomeActivity.1 */
    class AnonymousClass1 extends CheckIfValueExists {
        AnonymousClass1(Context x0) {
            super(x0);
        }

        protected void proceed(String table, Map<String, String> values) {
            WelcomeActivity.this.install((String) values.get(InstallationNames.screenname));
        }
    }

    public int getContentView() {
        return R.layout.welcome;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {
            super.doCreate(savedInstanceState);
            doCreate();
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
    }

    private String generateUniqueId(long time) {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 22, 0, 0, 0);
        return "user_" + Long.toHexString(time - cal.getTimeInMillis()) + "_" + ((int) (Math.random() * 10.0d));
    }

    private void doCreate() {
        ((TextView) findViewById(R.id.welome_textview2)).setText(getString(R.string.msg_subscriptionmonths_d, new Object[]{Integer.valueOf(12)}));
        this.editText = (EditText) findViewById(R.id.welcome_screenname_input);
        this.editText.setText(generateUniqueId(System.currentTimeMillis()));
        ((Button) findViewById(R.id.welcome_agreebutton)).setOnClickListener(this);
    }

    public void onClick(View v) {
        try {
            String screenname;
            Logx.log(Log.VERBOSE, getClass(), "onClick");
            if (this.editText.getText() == null) {
                screenname = null;
            } else {
                screenname = this.editText.getText().toString();
            }
            if (screenname == null || screenname.isEmpty()) {
                screenname = generateUniqueId(System.currentTimeMillis());
            }
            if (this.confirm == null) {
                this.confirm = new AnonymousClass1(this);
                if (this.progressText == null) {
                    this.progressText = (TextView) findViewById(R.id.welcome_progresstext);
                }
                this.confirm.setProgressText(this.progressText);
            }
            this.confirm.execute("installation", Collections.singletonMap(InstallationNames.screenname, screenname));
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private boolean install(String screenname) {

        boolean installed = false;

        try{

            // Install the app, if not already installed
            App.install(WelcomeActivity.this.getApplicationContext(), screenname);

            installed = true;

        }catch(StorageIOException e) {
            App.handleInstallationError(WelcomeActivity.this, e, null);
        }catch(IOException e) {
            App.handleInstallationError(WelcomeActivity.this, e, null);
        }catch(RuntimeException e) {
            Logx.log(this.getClass(), e);
        }finally{

            try{

                Intent welcomeOptions = new Intent(WelcomeActivity.this, WelcomeOptionsActivity.class);

                WelcomeActivity.this.startActivity(welcomeOptions);

            }catch(Exception e) {
                com.looseboxes.idisc.common.notice.Popup.show(WelcomeActivity.this, "Error displaying Welcome Options", Toast.LENGTH_LONG);
                Logx.log(this.getClass(), e);
            }
        }

        return installed;
    }
}

package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.android.core.io.StorageIOException;
import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.listeners.CheckIfExistsOnFocusChange;

import java.io.IOException;
import java.util.Calendar;

public class WelcomeActivity extends AbstractSingleTopActivity implements OnClickListener {

    private EditText screennameEditText;

    private RadioGroup genderRadioGroup;

    private boolean installationAttempted;

    private final String generatedScreenname;

    public WelcomeActivity() {
        this.generatedScreenname = generateUniqueId(System.currentTimeMillis());
    }

    public int getContentViewId() {
        return R.layout.welcome;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {

            super.doCreate(savedInstanceState);

            ((TextView) findViewById(R.id.welome_textview2)).setText(getString(R.string.msg_subscriptionmonths_d, new Object[]{Integer.valueOf(12)}));

            this.screennameEditText = (EditText) findViewById(R.id.userprofile_screenname);
            this.screennameEditText.setText(generatedScreenname);

            this.genderRadioGroup = (RadioGroup)this.findViewById(R.id.userprofile_radio_gender);

            ((Button) findViewById(R.id.welcome_agreebutton)).setOnClickListener(this);

            TextView progressText = (TextView)this.findViewById(R.id.userprofile_screenname_reason);

            this.screennameEditText.setOnFocusChangeListener(
                    new CheckIfExistsOnFocusChange("installation", InstallationNames.screenname, progressText){
                        @Override
                        @MainThread
                        protected void after(String downloaded) {
                            WelcomeActivity.this.install();
                        }
                    }
            );

        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }

    public void onClick(View v) {

        Logx.getInstance().log(Log.VERBOSE, getClass(), "onClick");

        this.install();
    }

    private void install() {

        final String screenname = this.getText(this.screennameEditText, generatedScreenname);

        this.install(User.getInstance().getCountryCode(this, null), screenname, this.getGender(null));
    }

    private void install(String countryCode, String screenname, String gender) {

        if(installationAttempted) {
            return;
        }

        installationAttempted = true;

        try{

            // Install the app, if not already installed
            App.install(this.getApplicationContext(), countryCode, screenname, gender);

        }catch(StorageIOException e) {
            App.handleInstallationError(this, e, null);
        }catch(IOException e) {
            App.handleInstallationError(this, e, null);
        }catch(RuntimeException e) {
            Logx.getInstance().log(this.getClass(), e);
        }finally{

            try{

                Intent welcomeOptions = new Intent(this, WelcomeOptionsActivity.class);

                WelcomeActivity.this.startActivity(welcomeOptions);

            }catch(Exception e) {
                Popup.getInstance().show(this, "Error displaying Welcome Options", Toast.LENGTH_LONG);
                Logx.getInstance().log(this.getClass(), e);
            }
        }
    }

    private String getGender(String defaultValue) {
        final int id = this.genderRadioGroup.getCheckedRadioButtonId();
        if(id == -1) {
            return defaultValue;
        }else {
            Button button = (Button) this.genderRadioGroup.findViewById(id);
            return button.getText().toString();
        }
    }

    private String getText(TextView textView, String defaultValue) {
        String output;
        if (textView.getText() == null) {
            output = null;
        } else {
            output = textView.getText().toString();
        }
        if (output == null || output.isEmpty()) {
            output = defaultValue;
        }
        return output;
    }

    private String generateUniqueId(long time) {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, 0, 22, 0, 0, 0);
        return "user_" + Long.toHexString(time - cal.getTimeInMillis()) + "_" + ((int) (Math.random() * 10.0d));
    }
}

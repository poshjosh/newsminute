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
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;

public class WelcomeOptionsActivity extends AbstractSingleTopActivity {

    private class AgreeButtonListener implements OnClickListener {
        private AgreeButtonListener() {
        }

        public void onClick(View v) {
            Activity activity = WelcomeOptionsActivity.this;
            try {
                String text;
                Editable editable = ((EditText) activity.findViewById(R.id.welomeoptions_autosearch)).getText();
                Logx.log(Log.DEBUG, getClass(), "User entered autosearch text:\n{0}", editable);
                if (editable == null) {
                    text = null;
                } else {
                    String sval = editable.toString().trim();
                    if (sval.isEmpty()) {
                        text = null;
                    } else {
                        text = sval;
                    }
                }
                Pref.setAutoSearchText(activity, text);
            } catch (Exception e) {
                Popup.show(WelcomeOptionsActivity.this, (Object) "Error updating Options", 1);
                Logx.log(getClass(), e);
            } finally {
                activity.startActivity(new Intent(activity, SignupActivity.class));
            }
        }
    }

    private class BackButtonListener implements OnClickListener {
        private BackButtonListener() {
        }

        public void onClick(View v) {
            try {
                WelcomeOptionsActivity.this.finish();
            } catch (RuntimeException e) {
                Logx.log(getClass(), e);
            }
        }
    }

    public int getContentView() {
        return R.layout.welcomeoptions;
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
        EditText editText = (EditText) findViewById(R.id.welomeoptions_autosearch);
        Button agreeButton = (Button) findViewById(R.id.welcomeoptions_okbutton);
        OnClickListener listener = new AgreeButtonListener();
        agreeButton.setOnClickListener(listener);
        ((Button) findViewById(R.id.welcomeprocess_nextbutton)).setOnClickListener(listener);
        ((Button) findViewById(R.id.welcomeprocess_backbutton)).setOnClickListener(new BackButtonListener());
    }
}

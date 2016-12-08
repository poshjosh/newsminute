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
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.util.Pref;

public class WelcomeOptionsActivity extends AbstractSingleTopActivity {

    private class AgreeButtonListener implements OnClickListener {
        public void onClick(View v) {
            Activity activity = WelcomeOptionsActivity.this;
            try {
                String text;
                Editable editable = ((EditText) activity.findViewById(R.id.welomeoptions_autosearch)).getText();
                Logx.getInstance().log(Log.DEBUG, getClass(), "User entered autosearch text:\n{0}", editable);
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

                Popup.getInstance().show(WelcomeOptionsActivity.this, R.string.err, 1);

                Logx.getInstance().log(getClass(), e);

            } finally {
                Intent intent = new Intent(activity, SignupActivity.class);
                intent.putExtra(SignupActivity.EXTRA_STRING_CALLING_ACTIVITY_CLASS_NAME, WelcomeOptionsActivity.class.getName());
                activity.startActivity(intent);
            }
        }
    }

    private class BackButtonListener implements OnClickListener {
        public void onClick(View v) {
            try {
                WelcomeOptionsActivity.this.finish();
            } catch (RuntimeException e) {
                Logx.getInstance().log(getClass(), e);
            }
        }
    }

    public int getContentViewId() {
        return R.layout.welcomeoptions;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {
            super.doCreate(savedInstanceState);
            EditText editText = (EditText) findViewById(R.id.welomeoptions_autosearch);
            Button agreeButton = (Button) findViewById(R.id.welcomeoptions_okbutton);
            OnClickListener listener = new AgreeButtonListener();
            agreeButton.setOnClickListener(listener);
            ((Button) findViewById(R.id.navigation_nextbutton)).setOnClickListener(listener);
            ((Button) findViewById(R.id.navigation_backbutton)).setOnClickListener(new BackButtonListener());
        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }
}

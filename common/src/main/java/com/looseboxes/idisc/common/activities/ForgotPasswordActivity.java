package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.Requestpassword;
import com.looseboxes.idisc.common.listeners.AbstractOnClickListenerForAsyncTask;
import com.bc.android.core.util.Logx;

public class ForgotPasswordActivity extends AbstractSingleTopActivity {

    private class OnclickListener extends AbstractOnClickListenerForAsyncTask {

        /* renamed from: com.looseboxes.idisc.common.activities.ForgotPasswordActivity.OnclickListener.1 */
        class AnonymousClass1 extends Requestpassword {
            AnonymousClass1(Context x0, String x1) {
                super(x0, x1);
            }

            protected void onPostExecute(String download) {
                try {
                    super.onPostExecute(download);
                } finally {
                    ForgotPasswordActivity.this.finish();
                }
            }
        }

        private OnclickListener() {
            super(ForgotPasswordActivity.this);
        }

        protected boolean accept(View v) {
            return v.equals(ForgotPasswordActivity.this.findViewById(R.id.forgotpassword_okbutton));
        }

        protected void execute(View v) {
            Editable email = ((EditText) getActivity().findViewById(R.id.forgotpassword_email)).getText();
            if (email != null) {
                String sval = email.toString();
                if (!sval.trim().isEmpty()) {
                    new AnonymousClass1(getActivity(), sval).execute();
                }
            }
        }

        protected int getErrorMessageResourceId() {
            return R.string.err_unexpected;
        }
    }

    public int getContentViewId() {
        return R.layout.forgotpassword;
    }

    protected void doCreate(Bundle savedInstanceState) {
        try {
            super.doCreate(savedInstanceState);
            doCreate();
        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }

    private void doCreate() {
        Button okBtn = (Button) findViewById(R.id.forgotpassword_okbutton);
        Button skipBtn = (Button) findViewById(R.id.forgotpassword_skipbutton);
        OnclickListener listener = new OnclickListener();
        okBtn.setOnClickListener(listener);
        skipBtn.setOnClickListener(listener);
    }
}

package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.widget.TextView;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.DefaultReadTask;
import com.looseboxes.idisc.common.notice.Popup;

import java.util.HashMap;
import java.util.Map;

public abstract class CheckIfValueExists {
    private boolean busy;
    private final Context context;
    private TextView progressText;

    /* renamed from: com.looseboxes.idisc.common.util.CheckIfValueExists.1 */
    class AnonymousClass1 extends DefaultReadTask<Boolean> {
        final /* synthetic */ String val$table;
        final /* synthetic */ Map val$values;

        AnonymousClass1(String str, Map map) {
            this.val$table = str;
            this.val$values = map;
        }

        protected void onCancelled(String s) {
            if (CheckIfValueExists.this.progressText != null) {
                CheckIfValueExists.this.progressText.setText(R.string.err);
            }
        }

        protected void processError() {
            if (hasError()) {
                try {
                    super.processError();
                } finally {
                    if (CheckIfValueExists.this.progressText != null) {
                        CheckIfValueExists.this.progressText.setText(R.string.err);
                    }
                    CheckIfValueExists.this.finish(this.val$table, this.val$values);
                }
            } else {
                CheckIfValueExists.this.busy = false;
            }
        }

        public void onSuccess(Boolean exists) {
            if (exists.booleanValue()) {
                if (CheckIfValueExists.this.progressText != null) {
                    CheckIfValueExists.this.progressText.setText(R.string.msg_screenname_exists);
                }
                CheckIfValueExists.this.busy = false;
                return;
            }
            CheckIfValueExists.this.finish(this.val$table, this.val$values);
            if (CheckIfValueExists.this.progressText != null) {
                CheckIfValueExists.this.progressText.setText(R.string.msg_continue);
            }
        }

        public String getOutputKey() {
            return "valueexists";
        }

        public String getLocalFilename() {
            throw new UnsupportedOperationException();
        }

        public Context getContext() {
            return CheckIfValueExists.this.context;
        }

        public String getErrorMessage() {
            return "";
        }
    }

    protected abstract void proceed(String str, Map<String, String> map);

    public CheckIfValueExists(Context context) {
        this.context = context;
    }

    private void finish(String table, Map<String, String> values) {
        try {
            proceed(table, values);
        } finally {
            this.busy = false;
        }
    }

    public void execute(String table, Map<String, String> values) {
        if (this.busy) {
            Popup.show(this.context, R.string.msg_pleasewait, 0);
            return;
        }
        try {
            if (Util.isNetworkConnectedOrConnecting(this.context)) {
                this.busy = true;
                DefaultReadTask<Boolean> task = new AnonymousClass1(table, values);
                if (this.progressText != null) {
                    this.progressText.setText(R.string.msg_pleasewait);
                }
                Map<String, String> params = new HashMap(values.size() + 1, 1.0f);
                params.put("table", table);
                params.putAll(values);
                task.setOutputParameters(params);
                task.setNoUI(true);
                this.busy = true;
                task.execute();
                return;
            }
            this.busy = true;
            finish(table, values);
            this.busy = false;
        } catch (Exception e) {
            this.busy = false;
            Logx.log(getClass(), e);
        } catch (Throwable th) {
            this.busy = false;
        }
    }

    public boolean isBusy() {
        return this.busy;
    }

    public TextView getProgressText() {
        return this.progressText;
    }

    public void setProgressText(TextView progressText) {
        this.progressText = progressText;
    }
}

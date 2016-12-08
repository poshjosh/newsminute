package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;

/**
 * Created by Josh on 9/21/2016.
 */
public abstract class CheckIfExists extends AbstractReadBoolean {

    private boolean busy;

    private final String name;

    private final TextView progressText;

    public CheckIfExists(Context context, String table, String name, String value, TextView progressText) {
        super(context, context.getString(R.string.err_unexpected));
        Util.requireNonNullOrEmpty(table, "Table name cannot be null or an empty string");
        Util.requireNonNullOrEmpty(name, "null");
        Util.requireNonNullOrEmpty(value, name+" cannot be null or an empty string");
        this.name = name;
        this.progressText = progressText;
        this.addOutputParameter("table", table);
        this.addOutputParameter(name, value);
    }

    public AsyncTask<String, ProgressStatus, String> execute() {

        if (this.busy) {
            this.askUserToPleaseWait(Toast.LENGTH_LONG);
            return this;
        }else{
            return super.execute();
        }
    }

    protected abstract void doSuccess(Boolean exists);

    @Override
    @CallSuper
    @MainThread
    protected void onPreExecute() {

        super.onPreExecute();

        if (this.busy) {
            throw new UnsupportedOperationException();
        }

        this.busy = true;

        this.askUserToPleaseWait(Toast.LENGTH_SHORT);
    }

    @Override
    public final void onSuccess(Boolean exists) {

        Logx.getInstance().debug(this.getClass(), "Exists: {0}", exists);

        try {

            String message;
            if (exists.booleanValue()) {
                message = this.getContext().getString(R.string.msg_s_alreadytaken, name);
                this.displayMessage(message, Toast.LENGTH_SHORT);
            } else {
                message = this.getContext().getString(R.string.msg_continue);
            }
            this.displayMessage(message, Toast.LENGTH_SHORT);

            this.doSuccess(exists);

        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        } finally {
            this.busy = false;
        }
    }

    @Override
    @CallSuper
    @MainThread
    protected void onCancelled(String s) {
        try {
            super.onCancelled(s);
        }finally{
            this.busy = false;
        }
    }

    @Override
    @CallSuper
    @MainThread
    protected void onPostExecute(String download) {
        try {
            super.onPostExecute(download);
        }finally{
            this.busy = false;
        }
    }

    public String getOutputKey() {
        return "valueexists";
    }

    private void askUserToPleaseWait(int length) {
        String message = this.getContext().getString(R.string.msg_pleasewait);
        this.displayMessage(message, length);
    }

    private void displayMessage(String message, int length) {
        this.setProgressText(message);
        Popup.getInstance().show(this.getContext(), message, length);
    }

    private void setProgressText(@StringRes int message) {
        if (this.progressText != null) {
            this.progressText.setText(message);
        }
    }

    private void setProgressText(CharSequence message) {
        if (this.progressText != null) {
            this.progressText.setText(message);
        }
    }

    public final TextView getProgressText() {
        return progressText;
    }

    public final boolean isBusy() {
        return busy;
    }
}

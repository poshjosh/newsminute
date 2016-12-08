package com.looseboxes.idisc.common.listeners;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.CheckIfExists;

/**
 * Created by Josh on 9/24/2016.
 */
public class CheckIfExistsOnFocusChange implements View.OnFocusChangeListener {

    private CheckIfExists checkIfExists;
    private final String table;
    private final String name;
    private final TextView progressText;

    public CheckIfExistsOnFocusChange(String table, String name, TextView progressText) {
        this.table = table;
        this.name = name;
        this.progressText = progressText;
    }

    @MainThread
    protected void before() { }
    @MainThread
    protected void processServerResponse(@Nullable Boolean exists) { }
    @MainThread
    protected void after(String downloaded) { }

    /**
     * Called when the focus state of a view has changed.
     *
     * @param v        The view whose state has changed.
     * @param hasFocus The new focus state of v.
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus) {
            this.checkIfValueExists(v);
        }
    }

    private void checkIfValueExists(final View v) {

        final Context context = v.getContext();

        if(!Util.isNetworkConnectedOrConnecting(context)) {
            Popup.getInstance().show(context, R.string.err_internetunavailable, Toast.LENGTH_SHORT);
            return;
        }

        final TextView textView;
        try{
            textView = (TextView)v;
        }catch (ClassCastException e) {
            return;
        }

        CharSequence text = textView.getText();

        if(text == null || text.length() == 0) {
            return;
        }

        if(this.checkIfExists == null || this.checkIfExists.isCompleted()) {

            this.checkIfExists = new CheckIfExists(context, table, name, text.toString(), this.progressText) {
                @Override
                protected void before() {
                    super.before();
                    CheckIfExistsOnFocusChange.this.before();
                }
                @Override
                protected void doSuccess(Boolean exists) {

                    if(exists) {
                        textView.setText(null);
                    }

                    CheckIfExistsOnFocusChange.this.processServerResponse(exists);
                }
                @Override
                protected void after(String downloaded) {
                    super.after(downloaded);
                    CheckIfExistsOnFocusChange.this.after(downloaded);
                }
            };
        }

        this.checkIfExists.execute();
    }
}

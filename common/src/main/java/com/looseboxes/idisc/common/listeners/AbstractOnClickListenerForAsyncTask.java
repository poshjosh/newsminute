package com.looseboxes.idisc.common.listeners;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

public abstract class AbstractOnClickListenerForAsyncTask implements OnClickListener {
    private final Activity activity;

    protected abstract boolean accept(View view);

    protected abstract void execute(View view);

    protected abstract int getErrorMessageResourceId();

    public AbstractOnClickListenerForAsyncTask(Activity activity) {
        this.activity = activity;
    }

    public void onClick(View v) {
        try {
            Logx.getInstance().log(Log.VERBOSE, getClass(), "onClick");
            if (accept(v)) {
                execute(v);
            } else {
                this.activity.finish();
            }
        } catch (Exception e) {
            Popup.getInstance().show(this.activity, getErrorMessageResourceId(), 1);
            Logx.getInstance().log(getClass(), e);
        }
    }

    public Activity getActivity() {
        return this.activity;
    }
}

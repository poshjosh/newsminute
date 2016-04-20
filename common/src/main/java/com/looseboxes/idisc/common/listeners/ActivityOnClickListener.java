package com.looseboxes.idisc.common.listeners;

import android.app.Activity;
import android.view.View.OnClickListener;

public interface ActivityOnClickListener extends OnClickListener {
    Activity getActivity();

    void setActivity(Activity activity);
}

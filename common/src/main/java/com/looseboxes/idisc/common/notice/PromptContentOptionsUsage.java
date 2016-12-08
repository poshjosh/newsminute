package com.looseboxes.idisc.common.notice;

import android.content.DialogInterface;

import com.bc.android.core.notice.DialogManager;
import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.R;

/**
 * Created by Josh on 10/25/2016.
 */
public class PromptContentOptionsUsage extends DialogManager {

    public PromptContentOptionsUsage() {

        super(R.layout.contentoptions_usage, R.string.msg_tips, Popup.NO_RES, R.string.msg_ok, Popup.NO_RES, R.string.msg_dontshowagain);
    }

    public PromptContentOptionsUsage(float displayFrequency, int launchAttemptsTillPrompt, int daysUntilPrompt) {

        super(R.layout.contentoptions_usage, R.string.msg_tips, Popup.NO_RES, R.string.msg_ok, Popup.NO_RES, R.string.msg_dontshowagain, displayFrequency, launchAttemptsTillPrompt, daysUntilPrompt);
    }

    @Override
    public void onOkButtonClicked(DialogInterface dialog, int which) { }
}

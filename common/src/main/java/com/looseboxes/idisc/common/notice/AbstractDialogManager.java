package com.looseboxes.idisc.common.notice;

import android.support.annotation.StringRes;

import com.bc.android.core.notice.DialogManager;
import com.looseboxes.idisc.common.R;

/**
 * Created by Josh on 10/25/2016.
 */
public abstract class AbstractDialogManager extends DialogManager {

    public AbstractDialogManager(@StringRes int dialogTitle, @StringRes int dialogText) {

        this(dialogTitle, dialogText, 1.0f, 0, 0);
    }

    public AbstractDialogManager(@StringRes int dialogTitle, @StringRes int dialogText,
                                 float displayFrequency, int launchAttemptsTillPrompt, int daysUntilPrompt) {

        super(R.layout.regulatedprompt_textview, dialogTitle, dialogText,
                R.string.msg_ok, R.string.msg_later, R.string.msg_dontshowagain,
                displayFrequency, launchAttemptsTillPrompt, daysUntilPrompt);
    }
}

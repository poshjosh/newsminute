package com.looseboxes.idisc.common.notice;

import android.content.DialogInterface;
import android.support.annotation.StringRes;

import com.looseboxes.idisc.common.util.Pref;

/**
 * Created by Josh on 10/25/2016.
 */
public class PromptWifiOnly extends AbstractDialogManager {

    public PromptWifiOnly(@StringRes int dialogTitle, @StringRes int dialogText) {
        super(dialogTitle, dialogText);
    }

    public PromptWifiOnly(@StringRes int dialogTitle, @StringRes int dialogText, float displayFrequency, int launchAttemptsTillPrompt, int daysUntilPrompt) {
        super(dialogTitle, dialogText, displayFrequency, launchAttemptsTillPrompt, daysUntilPrompt);
    }

    @Override
    public void onOkButtonClicked(DialogInterface dialog, int which) {

        Pref.setWifiOnly(this.getContext(), true);
    }
}

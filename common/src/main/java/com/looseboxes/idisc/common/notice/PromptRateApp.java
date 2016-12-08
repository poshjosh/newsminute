package com.looseboxes.idisc.common.notice;

import android.content.DialogInterface;
import android.support.annotation.StringRes;

import com.bc.android.core.AppCore;

/**
 * Created by Josh on 10/25/2016.
 */
public class PromptRateApp extends AbstractDialogManager {

    public PromptRateApp(@StringRes int dialogTitle, @StringRes int dialogText) {
        super(dialogTitle, dialogText);
    }

    public PromptRateApp(@StringRes int dialogTitle, @StringRes int dialogText, float displayFrequency, int launchAttemptsTillPrompt, int daysUntilPrompt) {
        super(dialogTitle, dialogText, displayFrequency, launchAttemptsTillPrompt, daysUntilPrompt);
    }

    @Override
    public void onOkButtonClicked(DialogInterface dialog, int which) {

        AppCore.startPlayStoreActivity(this.getContext(), this.getContext().getPackageName());
    }
}


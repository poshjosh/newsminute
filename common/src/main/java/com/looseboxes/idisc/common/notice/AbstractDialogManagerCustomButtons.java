package com.looseboxes.idisc.common.notice;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bc.android.core.notice.DialogManagerCustomButtons;
import com.looseboxes.idisc.common.R;

/**
 * Created by Josh on 10/25/2016.
 */
public abstract class AbstractDialogManagerCustomButtons extends DialogManagerCustomButtons {

    public AbstractDialogManagerCustomButtons(@Nullable String dialogTitle, @NonNull String dialogText) {

        this(dialogTitle, dialogText, 1.0f, 0, 0);
    }

    public AbstractDialogManagerCustomButtons(@Nullable String dialogTitle, @NonNull String dialogText,
                                              float displayFrequency, int appLaunchesUntilPrompt, int daysUntilPrompt) {

        this(R.layout.regulatedprompt, R.id.regulatedprompt_edittext,
                dialogTitle, dialogText,
                R.id.regulatedprompt_okbutton, R.id.regulatedprompt_laterbutton, R.id.regulatedprompt_neverbutton,
                displayFrequency, appLaunchesUntilPrompt, daysUntilPrompt);
    }

    public AbstractDialogManagerCustomButtons(@LayoutRes int layoutResId, @IdRes int textViewResId,
                                              @Nullable String dialogTitle, @NonNull String dialogText,
                                              @IdRes int okButtonId, @IdRes int neutralButtonId, @IdRes int negativeButtonId) {
        super(layoutResId, textViewResId, dialogTitle, dialogText, okButtonId, neutralButtonId, negativeButtonId);
    }

    public AbstractDialogManagerCustomButtons(@LayoutRes int layoutResId, @IdRes int textViewResId,
                                              @Nullable String dialogTitle, @NonNull String dialogText,
                                              @IdRes int okButtonId, @IdRes int neutralButtonId, @IdRes int negativeButtonId,
                                              float displayFrequency, int appLaunchesUntilPrompt, int daysUntilPrompt) {

        super(layoutResId, textViewResId,
                dialogTitle, dialogText,
                okButtonId, neutralButtonId, negativeButtonId,
                displayFrequency, appLaunchesUntilPrompt, daysUntilPrompt);
    }
}

package com.looseboxes.idisc.common.notice;

import android.content.Context;
import android.view.View;

import com.bc.android.core.AppCore;
import com.looseboxes.idisc.common.R;

public class PromptRateAppWithAppThemeButtons extends AbstractDialogManagerCustomButtons {

    private final String appPackageName;

    public PromptRateAppWithAppThemeButtons(Context context, float displayFrequency, int appLaunchesUntilPrompt, int daysUntilPrompt) {

        super(context.getString(R.string.msg_rate_app),
                context.getString(R.string.msg_rate_app_text),
                displayFrequency, appLaunchesUntilPrompt, daysUntilPrompt);

        this.appPackageName = context.getPackageName();
    }

    @Override
    public void onOkButtonClicked(View v) {

        AppCore.startPlayStoreActivity(v.getContext(), appPackageName);
    }
}

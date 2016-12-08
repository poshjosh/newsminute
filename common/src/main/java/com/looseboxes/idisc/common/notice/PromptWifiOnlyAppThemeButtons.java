package com.looseboxes.idisc.common.notice;

import android.content.Context;
import android.view.View;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.Pref;

/**
 * Created by Josh on 10/24/2016.
 */
public class PromptWifiOnlyAppThemeButtons extends AbstractDialogManagerCustomButtons {

    public PromptWifiOnlyAppThemeButtons(Context context, float displayFrequency, int appLaunchesUntilPrompt, int daysUntilPrompt) {

        super(context.getString(R.string.pref_wifionly_title),
                context.getString(R.string.msg_wifionly_reason),
                displayFrequency, appLaunchesUntilPrompt, daysUntilPrompt);
    }

    @Override
    public void onOkButtonClicked(View v) {
        Pref.setWifiOnly(v.getContext(), true);
    }
}

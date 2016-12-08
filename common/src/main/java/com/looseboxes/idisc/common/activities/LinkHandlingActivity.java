package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.handlers.LinkHandler;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

public class LinkHandlingActivity extends AbstractSingleTopActivity {

    public static final String EXTRA_STRING_LINK_TO_DISPLAY = LinkHandlingActivity.class.getName() + ".linkToDisplay";

    private LinkHandler linkHandler;

    public int getContentViewId() {
        return R.layout.linkhandling;
    }

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        this.linkHandler = ((DefaultApplication) getApplication()).createLinkHandler(this);
    }

    protected void handleIntent(Intent intent) {
        try {
            Uri uri;
            String linkToDisplay = intent.getStringExtra(EXTRA_STRING_LINK_TO_DISPLAY);
            if (linkToDisplay != null) {
                uri = Uri.parse(linkToDisplay);
            } else {
                uri = intent.getData();
            }
            boolean handled = false;
            if (uri != null) {
                if (linkHandler != null) {
                    linkHandler.handleLink(uri);
                    handled = true;
                }
            }
            if (!handled) {
                Popup.getInstance().show(this, getString(R.string.err_error_displaying_s, new Object[]{uri}), 0);
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }
}

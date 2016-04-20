package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.handlers.LinkHandler;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;

public class LinkHandlingActivity extends AbstractSingleTopActivity {
    public static final String EXTRA_STRING_LINK_TO_DISPLAY;
    private LinkHandler _lh;

    static {
        EXTRA_STRING_LINK_TO_DISPLAY = LinkHandlingActivity.class.getName() + ".linkToDisplay";
    }

    public int getContentView() {
        return R.layout.linkhandling;
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
                LinkHandler linkHandler = getLinkHandler();
                if (linkHandler != null) {
                    linkHandler.handleLink(uri);
                    handled = true;
                }
            }
            if (!handled) {
                Popup.show((Activity) this, getString(R.string.err_error_displaying_s, new Object[]{uri}), 0);
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private LinkHandler getLinkHandler() {
        if (this._lh == null) {
            this._lh = ((DefaultApplication) getApplication()).createLinkHandler(this);
        }
        return this._lh;
    }
}

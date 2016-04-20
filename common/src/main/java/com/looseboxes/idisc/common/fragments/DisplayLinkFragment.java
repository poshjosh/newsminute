package com.looseboxes.idisc.common.fragments;

import com.looseboxes.idisc.common.R;

public class DisplayLinkFragment extends BrowserFragment {
    public int getUrlInput() {
        return R.id.urlbar_urlinput;
    }

    public int getUrlButton() {
        return R.id.urlbar_button1;
    }

    public int getContentView() {
        return R.layout.displaylink;
    }

    public int getWebViewId() {
        return R.id.displaylink_display;
    }
}

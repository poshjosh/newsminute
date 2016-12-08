package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.fragments.DisplayLinkFragment;

public class DisplayLinkActivity extends WebContentActivity<DisplayLinkFragment> {

    public DisplayLinkActivity() { }

    public DisplayLinkFragment createFragment() {
        return new DisplayLinkFragment();
    }
}

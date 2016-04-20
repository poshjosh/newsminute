package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.fragments.DisplayFeedFragment;

public class DisplayFeedActivity extends AbstractDisplayFeedActivity<DisplayFeedFragment> {
    public static final String EXTRA_LONG_SELECTED_FEEDID;
    public static final String EXTRA_STRING_SELECTED_FEED_JSON;

    static {
        EXTRA_STRING_SELECTED_FEED_JSON = DisplayFeedActivity.class.getPackage().getName() + ".SelectedFeed";
        EXTRA_LONG_SELECTED_FEEDID = DisplayFeedActivity.class.getPackage().getName() + ".SelectedFeedID";
    }

    public DisplayFeedFragment createFragment() {
        return new DisplayFeedFragment();
    }
}

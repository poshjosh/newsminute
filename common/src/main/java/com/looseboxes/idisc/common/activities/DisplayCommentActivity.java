package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.fragments.DisplayCommentFragment;

public class DisplayCommentActivity extends AbstractDisplayFeedActivity<DisplayCommentFragment> {
    public static final String EXTRA_BOOL_CLEAR_NOTICES_ON_DESTROY;
    public static final String EXTRA_INT_SELECTED_NOTICE_POSITION;

    static {
        EXTRA_INT_SELECTED_NOTICE_POSITION = DisplayCommentActivity.class.getName() + ".selectedNoticePosition";
        EXTRA_BOOL_CLEAR_NOTICES_ON_DESTROY = DisplayCommentActivity.class.getName() + ".clearNoticesOnDestroy";
    }

    public DisplayCommentFragment createFragment() {
        return new DisplayCommentFragment();
    }
}

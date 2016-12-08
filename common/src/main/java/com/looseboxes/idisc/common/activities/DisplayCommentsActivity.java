package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.fragments.DisplayCommentsFragment;

public class DisplayCommentsActivity extends DisplayCommentActivity {

    public DisplayCommentsFragment createFragment() {
        return new DisplayCommentsFragment();
    }
}

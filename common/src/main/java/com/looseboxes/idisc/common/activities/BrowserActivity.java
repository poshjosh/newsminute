package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.fragments.BrowserFragment;

public abstract class BrowserActivity<K extends BrowserFragment> extends WebContentActivity<K> {
    protected BrowserActivity() {
    }
}

package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.fragments.WebContentFragment;

public abstract class WebContentActivity<K extends WebContentFragment> extends AdsActivity {

    public static final String EXTRA_STRING_CONTENT_CHARSET = WebContentActivity.class.getName() + ".contentCharset";
    public static final String EXTRA_STRING_CONTENT_TO_DISPLAY = WebContentActivity.class.getName() + ".contentToDisplay";
    public static final String EXTRA_STRING_CONTENT_TYPE = WebContentActivity.class.getName() + ".contentType";
    public static final String EXTRA_STRING_LINK_TO_DISPLAY = WebContentActivity.class.getName() + ".linkToDisplay";

    private K webContentFragment;

    protected abstract K createFragment();

    @Override
    public String getShareText() {
        return this.webContentFragment.getShareText();
    }

    protected void handleIntent(Intent intent) {
        this.webContentFragment.update(intent.getExtras(), true);
    }

    public int getContentViewId() {
        return this.webContentFragment.getContentViewId();
    }

    public K getFragment() {
        return this.webContentFragment;
    }

    protected boolean updateFragment(Bundle savedInstanceState) {

        if (((DefaultApplication) getApplication()).isDualPaneMode()) {
            finish();
        }

        this.webContentFragment = this.createFragment();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            webContentFragment.update(extras, true);
        }

        getFragmentManager().beginTransaction().add(android.R.id.content, webContentFragment).commit();

        return true;
    }
}

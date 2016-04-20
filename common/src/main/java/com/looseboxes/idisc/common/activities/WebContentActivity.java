package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.fragments.WebContentFragment;

public abstract class WebContentActivity<K extends WebContentFragment> extends AdsActivity {
    public static final String EXTRA_STRING_CONTENT_CHARSET;
    public static final String EXTRA_STRING_CONTENT_TO_DISPLAY;
    public static final String EXTRA_STRING_CONTENT_TYPE;
    public static final String EXTRA_STRING_LINK_TO_DISPLAY;
    private K _wcf;

    protected abstract K createFragment();

    static {
        EXTRA_STRING_LINK_TO_DISPLAY = WebContentActivity.class.getName() + ".linkToDisplay";
        EXTRA_STRING_CONTENT_TO_DISPLAY = WebContentActivity.class.getName() + ".contentToDisplay";
        EXTRA_STRING_CONTENT_TYPE = WebContentActivity.class.getName() + ".contentType";
        EXTRA_STRING_CONTENT_CHARSET = WebContentActivity.class.getName() + ".contentCharset";
    }

    protected void handleIntent(Intent intent) {
        K fragment = getFragment(false);
        if (fragment == null) {
            throw new NullPointerException();
        }
        fragment.update(intent.getExtras(), true);
    }

    public int getContentView() {
        return getFragment(true).getContentView();
    }

    public K getFragment(boolean create) {
        if (this._wcf == null && create) {
            this._wcf = createFragment();
        }
        return this._wcf;
    }

    protected boolean updateFragment(Bundle savedInstanceState) {
        if (((DefaultApplication) getApplication()).isDualPaneMode()) {
            finish();
        }
        WebContentFragment webContentFragment = getFragment(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            webContentFragment.update(extras, true);
        }
//        getFragmentManager().beginTransaction().add(16908290, webContentFragment).commit();
        getFragmentManager().beginTransaction().add(android.R.id.content, webContentFragment).commit();
        return true;
    }
}

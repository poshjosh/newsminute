package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import com.looseboxes.idisc.common.fragments.DisplayAdvertFragment;
import com.looseboxes.idisc.common.util.AdvertDisplayFinishedListener;

public class DisplayAdvertActivity extends WebContentActivity<DisplayAdvertFragment> implements AdvertDisplayFinishedListener {
    public DisplayAdvertFragment createFragment() {
        return new DisplayAdvertFragment();
    }

    public void onAdvertDisplayFinished(Bundle bundle) {
        if (bundle == null) {
            throw new NullPointerException();
        }
        Intent intent = new Intent(this, DisplayFeedActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}

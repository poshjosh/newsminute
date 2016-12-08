package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedResultHandler;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedManager;

import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PreferenceFeedsActivity extends DefaultFeedListActivity
        implements PreferencefeedResultHandler {

    private boolean roundTrip;

    private final PreferenceType preferenceType;

    private final int tabId;

    public PreferenceFeedsActivity(PreferenceType preferenceType, int tabId) {
        this.preferenceType = preferenceType;
        this.tabId = tabId;
    }

    @Override
    public int getTabId() {
        return tabId;
    }

    public final PreferenceType getPreferenceType() {
        return this.preferenceType;
    }

    protected void onNoResult(Collection<JSONObject> items) {

        if (this.roundTrip) {

            finish();

        }else{

            final String message = getString(R.string.msg_youhaveno_s, new Object[]{getPreferenceType().name()});

            Popup.getInstance().show(this, message, Toast.LENGTH_SHORT);

            PreferencefeedManager pfm = new PreferencefeedManager(this, false);

            boolean ignoreSchedule = true;

            pfm.update(ignoreSchedule);
        }
    }

    @Override
    @CallSuper
    protected void doCreate(Bundle savedInstanceState) {

        super.doCreate(savedInstanceState);

        this.displayFeeds();
    }

    @Override
    public void refreshDisplay() {
        this.clearDisplay();
        this.displayFeeds();
    }

    @Override
    public void clearDisplay() {
        this.clearDisplayedFeeds();
    }

    @Override
    public void processDownloadResponse(List downloadedFeeds, boolean success) {
        try {
            this.roundTrip = true;
            this.displayFeeds();
        }finally{
            this.roundTrip = false;
        }
    }

    @Override
    public void processSyncResponse(List addedFeeds, List removedFeeds, boolean success) {
        try {
            this.roundTrip = true;
            this.displayFeeds();
        }finally{
            this.roundTrip = false;
        }
    }

    private void displayFeeds() {
        Preferencefeeds pf = new Preferencefeeds(this, this.getPreferenceType());
        Set<JSONObject> cached = pf.getFeeds();
        this.displayFeeds(cached, false);
    }

    @Override
    public void processSyncFailure() {
        Popup.getInstance().show(this, R.string.err_syncing, Toast.LENGTH_LONG);
        this.finish();
    }

    @Override
    public void processDownloadFailure() {
        Popup.getInstance().show(this, R.string.err_syncing, Toast.LENGTH_LONG);
        this.finish();
    }
}

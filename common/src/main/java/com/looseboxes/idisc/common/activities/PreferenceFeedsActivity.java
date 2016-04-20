package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.CachedSet;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;

public class PreferenceFeedsActivity extends DefaultFeedListActivity {

    private PreferenceFeedsManager _pfm;

    private boolean finishOnNoResult;
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

    protected String getNoResultMessageId() {
        return getString(R.string.msg_youhaveno_s, new Object[]{getPreferenceType().name()});
    }

    public boolean isReloadDisplay() {
        return true;
    }

    protected void onResume() {
        this.finishOnNoResult = false;
        super.onResume();
    }

    public boolean isToFinishOnNoResult() {
        return this.finishOnNoResult;
    }

    public Collection<JSONObject> getFeedsToDisplay() {
        PreferenceFeedsManager fm = getFeedsManager();
        CachedSet<JSONObject> cached = fm.getFeeds();
        if (User.getInstance().isLoggedIn(this)) {
            fm.update(false);
        } else if (cached == null || cached.isEmpty()) {
            Popup.show((Activity) this, getString(R.string.err_login_required), 1);
            finish();
        }
        return cached;
    }

    protected PreferenceFeedsManager getFeedsManager() {
        if (this._pfm == null) {
            this._pfm = new PreferenceFeedsManager(PreferenceFeedsActivity.this) {

                public PreferenceType getPreferenceType() {
                    return PreferenceFeedsActivity.this.getPreferenceType();
                }

                protected void processSyncResponse(List addedFeeds, List removedFeeds, boolean success) {
                    PreferenceFeedsActivity.this.finishOnNoResult = true;
                    PreferenceFeedsActivity.this.displayFeeds(getFeeds());
                }

                protected void processSyncFailure() {
                    PreferenceFeedsActivity.this.finish();
                }

                protected void processDownloadResponse(List downloadedFeeds, boolean success) {
                    PreferenceFeedsActivity.this.finishOnNoResult = true;
                    PreferenceFeedsActivity.this.displayFeeds(getFeeds());
                }

                protected void processDownloadFailure() {
                    PreferenceFeedsActivity.this.finish();
                }
            };
        }
        return this._pfm;
    }
}

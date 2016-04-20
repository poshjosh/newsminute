package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.fragments.DisplayFeedFragment;
import com.looseboxes.idisc.common.fragments.WebContentFragment;
import com.looseboxes.idisc.common.handlers.FeedDisplayHandler;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class DefaultFeedListActivity extends ListFragmentActivity {
    private FeedFilter feedFilter;

    public int getDetailsFragmentId() {
        return R.id.details;
    }

    public int getListFragmentId() {
        return R.id.titles;
    }

    protected WebContentFragment createDetailsFragment() {
        return new DisplayFeedFragment();
    }

    public boolean isClearPrevious() {
        return false;
    }

    public boolean isReloadDisplay() {
        return false;
    }

    public int getContentView() {
        return R.layout.main;
    }

    public boolean isToFinishOnNoResult() {
        return true;
    }

    protected String getNoResultMessageId() {
        return getString(R.string.msg_nofeed);
    }

    protected void onNoResult(List toDisplay) {
        Popup.show((Activity) this, getNoResultMessageId(), 0);
        if (isToFinishOnNoResult()) {
            finish();
            return;
        }
        ProgressBar progressBar = getListFragment().getEmptyViewProgressBar(false);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public Collection<JSONObject> getFeedsToDisplay() {
        return FeedDownloadManager.getDownload(this);
    }

    public FeedFilter getFeedFilter() {
        return null;
    }

    private boolean accept(Feed feed) {
        if (this.feedFilter == null) {
            this.feedFilter = getFeedFilter();
        }
        return this.feedFilter == null ? true : this.feedFilter.accept(feed);
    }

    protected void onResume() {
        super.onResume();
        try {
            FeedDisplayHandler fdh = getFeedDisplayHandler();
            if (isClearPrevious() && !fdh.isEmpty()) {
                fdh.clear();
            }
            if (fdh.isEmpty() || isReloadDisplay()) {
                displayFeeds(getFeedsToDisplay());
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    public void displayFeeds(Collection<JSONObject> feeds) {
        List<JSONObject> toDisplay;
        Integer num = null;
        if (feeds == null || feeds.isEmpty()) {
            toDisplay = null;
        } else {
            toDisplay = new ArrayList();
            Feed feed = new Feed();
            for (JSONObject json : feeds) {
                feed.setJsonData(json);
                if (accept(feed)) {
                    toDisplay.add(json);
                }
            }
        }
        Class cls = getClass();
        String str = "Accepted {0} of {1} feeds for display";
        Object[] objArr = new Object[2];
        objArr[0] = toDisplay == null ? null : Integer.valueOf(toDisplay.size());
        if (feeds != null) {
            num = Integer.valueOf(feeds.size());
        }
        objArr[1] = num;
        Logx.debug(cls, str, objArr);
        if (toDisplay == null || toDisplay.isEmpty()) {
            onNoResult(toDisplay);
        } else {
            getFeedDisplayHandler().displayFeeds(toDisplay);
        }
    }
}

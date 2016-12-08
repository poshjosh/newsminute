package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bc.android.core.ui.ListState;
import com.bc.android.core.util.Preferences;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.fragments.DefaultListFragment;
import com.looseboxes.idisc.common.fragments.DisplayFeedFragment;
import com.looseboxes.idisc.common.fragments.WebContentFragment;
import com.looseboxes.idisc.common.handlers.FeedListDisplayHandler;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Date;

public abstract class DefaultFeedListActivity extends ListFragmentActivity{

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

    public int getContentViewId() {
        return R.layout.main;
    }

    public FeedFilter getFeedFilter() {
        if(feedFilter == null) {
            feedFilter = this.createFeedFilter();
        }
        return feedFilter;
    }

    public FeedFilter createFeedFilter() {
        return null;
    }

    protected void onNoResult(Collection<JSONObject> items) { }

    protected void clearDisplayedFeeds() {

        try {

            final FeedListDisplayHandler fdh = this.getFeedListDisplayHandler();

            if (!fdh.isEmpty()) {
                fdh.clear();
            }
        } catch (Exception e) {

            Logx.getInstance().log(getClass(), e);
        }
    }

    protected int displayFeeds(Collection<JSONObject> toDisplay, boolean append) {

        int output;

        try {

            final FeedListDisplayHandler fdh = this.getFeedListDisplayHandler();

            if(!append) {
                fdh.clear();
            }

            Logx.getInstance().debug(this.getClass(), "To display {0} feeds", toDisplay == null ? null : toDisplay.size());

            if(toDisplay == null || toDisplay.isEmpty()) {
                this.onNoResult(toDisplay);
                output = 0;
            }else {
                output = fdh.displayFeeds(toDisplay, this.getFeedFilter());
            }

        } catch (Exception e) {

            Logx.getInstance().log(getClass(), e);

            output = 0;
        }

        return output;
    }

    public FeedListDisplayHandler getFeedListDisplayHandler() {
        DefaultListFragment defaultListFragment = this.getListFragment();
        FeedListDisplayHandler fdh = defaultListFragment.getFeedListDisplayHandler();
        return fdh;
    }
}

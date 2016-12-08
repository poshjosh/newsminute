package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.widget.ProgressBar;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByCategory;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractCategoriesActivity extends DefaultFeedListActivity {

    public abstract Set<String> getSelected();

    @Override
    @CallSuper
    protected void doCreate(Bundle savedInstanceState) {

        final ProgressBar progressBar = (ProgressBar)this.findViewById(R.id.main_progressbar_indeterminate);

        try {
            this.setVisibility(progressBar, ProgressBar.VISIBLE);

            super.doCreate(savedInstanceState);

            displayFeeds(FeedDownloadManager.getDownload(this), false);

        }finally {
            setVisibility(progressBar, ProgressBar.GONE);
        }
    }

    protected void onNoResult(Collection<JSONObject> toDisplay) {
        Popup.getInstance().show(this, R.string.msg_nofeedsforcategories, 1);
        finish();
    }

    /**
     * @see #onSearchRequested()
     * @return A Bundle which will be used by the {@link #onSearchRequested()}} method as the
     * <code>Bundle</code> parameter to method {@link #startSearch(String, boolean, Bundle, boolean)}}
     */
    @Override
    public Bundle getBundleDataForSearchableActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME, FilterByCategory.class.getName());
        bundle.putStringArrayList(FilterByCategory.EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES, new ArrayList(getSelected()));
        return bundle;
    }

    public FeedFilter createFeedFilter() {
        return new FilterByCategory(this, new ArrayList(getSelected()));
    }
}

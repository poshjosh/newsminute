package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.os.Bundle;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByFeedSource;
import java.util.ArrayList;

public class SourcesActivity extends DefaultFeedListActivity {
    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES;
    private FeedFilter _ff;

    static {
        EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES = FilterByFeedSource.EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES;
    }

    public int getTabId() {
        return R.id.tabs_sources;
    }

    public ArrayList<String> getSelectedSources() {
        return getIntent().getStringArrayListExtra(EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES);
    }

    public Bundle getBundleDataForSearchableActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME, FilterByFeedSource.class.getName());
        bundle.putStringArrayList(FilterByFeedSource.EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES, getSelectedSources());
        return bundle;
    }

    public FeedFilter getFeedFilter() {
        if (this._ff == null) {
            this._ff = new FilterByFeedSource((Context) this, getSelectedSources());
        }
        return this._ff;
    }
}

package com.looseboxes.idisc.common.activities;

import android.os.Bundle;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByHeadlines;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HeadlinesActivity extends AbstractCategoriesActivity {

    public int getTabId() {
        return R.id.tabs_headlines;
    }

    public Set<String> getSelected() {
        Set<String> categories = new HashSet();
        categories.add(getString(R.string.msg_headlines));
        return categories;
    }

    public Bundle getBundleDataForSearchableActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME, FilterByHeadlines.class.getName());
        bundle.putStringArrayList(FilterByHeadlines.EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES, new ArrayList(getSelected()));
        return bundle;
    }

    public FeedFilter createFeedFilter() {
        return new FilterByHeadlines(this, new ArrayList(getSelected()));
    }
}

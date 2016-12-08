package com.looseboxes.idisc.common.activities;

import android.os.Bundle;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByFeedSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SourcesActivity extends AbstractCategoriesActivity {

    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES = FilterByFeedSource.EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES;

    public int getTabId() {
        return R.id.tabs_sources;
    }

    @Override
    public Set<String> getSelected() {
        ArrayList<String> output = getIntent().getStringArrayListExtra(EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES);
        return output == null ? Collections.EMPTY_SET : new HashSet(output);
    }

    @Override
    public Bundle getBundleDataForSearchableActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME, FilterByFeedSource.class.getName());
        bundle.putStringArrayList(FilterByFeedSource.EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES, new ArrayList(getSelected()));
        return bundle;
    }

    @Override
    public FeedFilter createFeedFilter() {
        return new FilterByFeedSource(this, new ArrayList<>(getSelected()));
    }
}

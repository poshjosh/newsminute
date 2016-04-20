package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.os.Bundle;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByCategory;
import java.util.ArrayList;
import java.util.Set;

public abstract class AbstractCategoriesActivity extends DefaultFeedListActivity {
    private FeedFilter feedFilter;

    public abstract Set<String> getCategories();

    protected String getNoResultMessageId() {
        return getString(R.string.msg_nofeedsforcategories);
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
        bundle.putStringArrayList(FilterByCategory.EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES, new ArrayList(getCategories()));
        return bundle;
    }

    public FeedFilter getFeedFilter() {
        if (this.feedFilter == null) {
            this.feedFilter = new FilterByCategory((Context) this, new ArrayList(getCategories()));
        }
        return this.feedFilter;
    }
}

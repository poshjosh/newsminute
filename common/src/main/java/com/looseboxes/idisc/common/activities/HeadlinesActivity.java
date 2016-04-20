package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByHeadlines;
import com.looseboxes.idisc.common.notice.Popup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeadlinesActivity extends AbstractCategoriesActivity {
    private FeedFilter feedFilter;

    public int getTabId() {
        return R.id.tabs_headlines;
    }

    public Set<String> getCategories() {
        Set<String> categories = new HashSet();
        categories.add(getString(R.string.msg_headlines));
        return categories;
    }

    protected void onNoResult(List toDisplay) {
        Popup.show((Activity) this, R.string.msg_nofeedsforcategories, 1);
        finish();
    }

    public Bundle getBundleDataForSearchableActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME, FilterByHeadlines.class.getName());
        bundle.putStringArrayList(FilterByHeadlines.EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES, new ArrayList(getCategories()));
        return bundle;
    }

    public FeedFilter getFeedFilter() {
        if (this.feedFilter == null) {
            this.feedFilter = new FilterByHeadlines((Context) this, new ArrayList(getCategories()));
        }
        return this.feedFilter;
    }
}

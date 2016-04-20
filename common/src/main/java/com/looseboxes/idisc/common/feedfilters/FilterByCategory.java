package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.util.Logx;
import java.io.Serializable;
import java.util.List;

public class FilterByCategory implements FeedFilterWithBundle, Serializable {
    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES;
    private Bundle bundle;
    private List<String> categories;
    private final Context context;

    static {
        EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES = FilterByFeedSource.class.getName() + ".SelectedCategories.extrastringset";
    }

    public FilterByCategory(Context context) {
        this.context = context;
    }

    public FilterByCategory(Context context, Bundle bundle) {
        this.context = context;
        this.bundle = bundle;
    }

    public FilterByCategory(Context context, List<String> categories) {
        this.context = context;
        this.categories = categories;
    }

    public boolean accept(Feed feed) {
        boolean matches = false;
        List<String> preferedCats = getCategories();
        if (preferedCats == null || (preferedCats.isEmpty() && this.bundle != null)) {
            preferedCats = this.bundle.getStringArrayList(EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES);
        }
        String feedTitle = feed.getTitle();
        String feedCats = feed.getCategories();
        for (String preferedCat : preferedCats) {
            matches = feed.matches(this.context, preferedCat);
            if (matches) {
                break;
            }
        }
        Logx.log(Log.VERBOSE, getClass(), "Matches: {0}, feed categories: {1}\nfeed title: {2}", Boolean.valueOf(matches), feedCats, feedTitle);
        return matches;
    }

    public Context getContext() {
        return this.context;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}

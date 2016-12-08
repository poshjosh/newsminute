package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.jsonview.Feed;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class FilterByCategory implements FeedFilterWithAcceptables, Serializable {

    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES = FilterByFeedSource.class.getName() + ".SelectedCategories.extrastringset";

    private boolean acceptedByDefault;

    private Collection<String> acceptables;

    private final Context context;

    public FilterByCategory(Context context, Bundle bundle) {
        this(context, bundle, false);
    }

    public FilterByCategory(Context context, Bundle bundle, boolean acceptedByDefault) {
        this(context, bundle.getStringArrayList(EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES), acceptedByDefault);
    }

    public FilterByCategory(Context context, Collection<String> categories) {
        this(context, categories, false);
    }

    public FilterByCategory(Context context, Collection<String> acceptables, boolean acceptedByDefault) {
        this.context = context;
        this.acceptables = acceptables == null || acceptables.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableCollection(acceptables);
        this.acceptedByDefault = acceptedByDefault;
    }

    public boolean accept(Feed feed) {
        boolean matches = acceptedByDefault;
        String feedCats = feed.getCategories();
        for (String category : acceptables) {
            matches = feed.matches(this.context, category);
            if (matches) {
                break;
            }
        }
        Logx.getInstance().log(Log.VERBOSE, getClass(), "Matches: {0}, feed categories: {1}\nfeed title: {2}", Boolean.valueOf(matches), feedCats, feed.getTitle());
        return matches;
    }

    public final boolean isAcceptedByDefault() {
        return acceptedByDefault;
    }

    public final Context getContext() {
        return this.context;
    }

    @Override
    public final Collection<String> getAcceptables() {
        return this.acceptables;
    }
}

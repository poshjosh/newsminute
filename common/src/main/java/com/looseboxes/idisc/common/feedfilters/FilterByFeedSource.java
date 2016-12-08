package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.os.Bundle;

import com.looseboxes.idisc.common.jsonview.Feed;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class FilterByFeedSource implements FeedFilterWithAcceptables, Serializable {

    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES = FilterByFeedSource.class.getName() + ".SelectedCategories.extrastringset";

    private final boolean acceptedByDefault;
    private final Context context;
    private Collection<String> acceptables;

    public FilterByFeedSource(Context context, Bundle bundle) {
        this(context, bundle, false);
    }

    public FilterByFeedSource(Context context, Bundle bundle, boolean acceptedByDefault) {
        this(context, bundle.getStringArrayList(EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES), acceptedByDefault);
    }

    public FilterByFeedSource(Context context, Collection<String> acceptables) {
        this(context, acceptables, false);
    }

    public FilterByFeedSource(Context context, Collection<String> acceptables, boolean acceptedByDefault) {
        this.context = context;
        this.acceptables = acceptables == null || acceptables.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableCollection(acceptables);
        this.acceptedByDefault = acceptedByDefault;
    }

    public boolean accept(Feed feed) {
        return acceptables.isEmpty() ? acceptedByDefault : acceptables.contains(feed.getSourceName());
    }

    public final Context getContext() {
        return this.context;
    }

    public final Collection<String> getAcceptables() {
        return this.acceptables;
    }
}

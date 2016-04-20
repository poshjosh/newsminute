package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.os.Bundle;
import com.looseboxes.idisc.common.jsonview.Feed;
import java.io.Serializable;
import java.util.List;

public class FilterByFeedSource implements FeedFilterWithBundle, Serializable {
    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES;
    private Bundle bundle;
    private final Context context;
    private List<String> sources;

    static {
        EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES = FilterByFeedSource.class.getName() + ".SelectedCategories.extrastringset";
    }

    public FilterByFeedSource(Context context) {
        this.context = context;
    }

    public FilterByFeedSource(Context context, Bundle bundle) {
        this.context = context;
        this.bundle = bundle;
    }

    public FilterByFeedSource(Context context, List<String> sources) {
        this.context = context;
        this.sources = sources;
    }

    public boolean accept(Feed feed) {
        List<String> selection = getSources();
        if (selection == null || (selection.isEmpty() && this.bundle != null)) {
            selection = this.bundle.getStringArrayList(EXTRA_STRING_ARRAYLIST_SELECTED_SOURCES);
        }
        if (selection == null || selection.isEmpty()) {
            return true;
        }
        return selection.contains(feed.getSourceName(getContext()));
    }

    public Context getContext() {
        return this.context;
    }

    public List<String> getSources() {
        return this.sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public Bundle getBundle() {
        return this.bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}

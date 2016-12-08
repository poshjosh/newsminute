package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.os.Bundle;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class FilterByHeadlines extends FilterByCategory {

    private Pattern headlinePattern;

    public FilterByHeadlines(Context context, Collection<String> categories, boolean acceptedByDefault) {
        super(context, categories, acceptedByDefault);
        this.headlinePattern = Pattern.compile(App.getPropertiesManager(getContext()).getString(PropertyName.headlinePattern), Pattern.CASE_INSENSITIVE);
    }

    public FilterByHeadlines(Context context, Collection<String> categories) {
        super(context, categories);
        this.headlinePattern = Pattern.compile(App.getPropertiesManager(getContext()).getString(PropertyName.headlinePattern), Pattern.CASE_INSENSITIVE);
    }

    public FilterByHeadlines(Context context, Bundle bundle, boolean acceptedByDefault) {
        super(context, bundle, acceptedByDefault);
        this.headlinePattern = Pattern.compile(App.getPropertiesManager(getContext()).getString(PropertyName.headlinePattern), Pattern.CASE_INSENSITIVE);
    }

    public FilterByHeadlines(Context context, Bundle bundle) {
        super(context, bundle);
        this.headlinePattern = Pattern.compile(App.getPropertiesManager(getContext()).getString(PropertyName.headlinePattern), Pattern.CASE_INSENSITIVE);
    }

    public boolean accept(Feed feed) {
        if(this.headlinePattern.matcher(feed.getTitle()).find()) {
            return true;
        }else{
            return super.accept(feed);
        }
    }

    public final Pattern getHeadlinePattern() {
        return this.headlinePattern;
    }
}

package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.jsonview.JsonObjectSorter;
import com.looseboxes.idisc.common.jsonview.SortScore;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;

import org.json.simple.JSONObject;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Josh on 11/19/2016.
 */
public class FeedFilterImpl extends JsonObjectSorter implements SortScore, FeedFilter {

    private final Context context;
    private final long addedValuePreferredCategory;
    private final long addedValuePreferredSource;
    private final long valuePerHit;
    private final long addedValueMax;
    private final long defaultTime;

    private final FeedFilterWithAcceptables sourceFilter;
    private final FeedFilterWithAcceptables categoryFilter;

    public FeedFilterImpl(Context context, long defaultTime, boolean acceptedByDefault) {

        this(context, Pref.getPreferredCategories(context), Pref.getPreferredSources(context),
                defaultTime, acceptedByDefault);
    }

    public FeedFilterImpl(Context context,
                          Set<String> preferredCategories, Set<String> preferredSources,
                          long defaultTime, boolean acceptedByDefault) {

        super(FeedNames.feeddate, new Feed());

        this.context = context;

        this.sourceFilter = new FilterByFeedSource(context, preferredSources, acceptedByDefault);
        this.categoryFilter = new FilterByCategory(context, preferredCategories, acceptedByDefault);

        this.defaultTime = defaultTime;
        if(defaultTime < 0) {
            throw new IllegalArgumentException();
        }

        PropertiesManager props = App.getPropertiesManager(context);

        this.valuePerHit = TimeUnit.MINUTES.toMillis(props.getInt(PropertiesManager.PropertyName.addedValuePerHitMinutes));

        this.addedValueMax = TimeUnit.MINUTES.toMillis(props.getInt(PropertiesManager.PropertyName.addedValueLimitMinutes));

        this.addedValuePreferredCategory = TimeUnit.MINUTES.toMillis(props.getInt(PropertiesManager.PropertyName.addedValuePreferredCategoryMinutes));

        this.addedValuePreferredSource = TimeUnit.MINUTES.toMillis(props.getInt(PropertiesManager.PropertyName.addedValuePreferredSourceMinutes));
    }

    @Override
    public long computeScoreOfSorting(JSONObject json) {

        Feed feed = (Feed) getJsonView();

        feed.setJsonData(json);

        long time = this.computeTime(json, defaultTime);

        final int hitcount = feed.getHitcount(this.context);

        if (hitcount > 0) {

            time += getAddedValueTime(hitcount);
        }

        if(this.addedValuePreferredCategory > 0 && this.categoryFilter.accept(feed)) {

            time += this.addedValuePreferredCategory;
        }

        if(this.addedValuePreferredSource > 0 && this.sourceFilter.accept(feed)) {

            time += this.addedValuePreferredSource;
        }

        return time;
    }

    @Override
    public boolean accept(Feed feed) {

        return (this.sourceFilter.accept(feed) || this.categoryFilter.accept(feed)) && this.computeTime(feed, -1L) > defaultTime;
    }

    @Override
    public long computeTime(JSONObject json, long defaultValue) {

        Feed feed = (Feed) getJsonView();

        feed.setJsonData(json);

        return this.computeTime(feed, defaultValue);
    }

    public long computeTime(Feed feed, long defaultValue) {

        Date date = feed.getDate(getDateColumn(), null);

        final long time = date == null ? defaultValue : date.getTime();

        return time;
    }

    private long getAddedValueTime(int hits) {
        long addedValue = ((long) hits) * this.valuePerHit;
        if (addedValue > this.addedValueMax) {
            return this.addedValueMax;
        }
        return addedValue;
    }

    public final FeedFilterWithAcceptables getCategoryFilter() {
        return categoryFilter;
    }

    public final FeedFilterWithAcceptables getSourceFilter() {
        return sourceFilter;
    }
}

package com.looseboxes.idisc.common.jsonview;

import android.content.Context;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;

public class FeedComparator extends JsonObjectSorter {
    private final long addedValueMax;
    private final Context context;
    private Set<String> preferredCategories;
    private Set<String> preferredSources;
    private final long valuePerHit;

    public FeedComparator(Context context) {
        super(FeedNames.feeddate, new Feed());
        this.context = context;
        PropertiesManager props = App.getPropertiesManager(context);
        this.valuePerHit = TimeUnit.MINUTES.toMillis((long) props.getInt(PropertyName.addedValuePerHitMinutes));
        this.addedValueMax = TimeUnit.MINUTES.toMillis((long) props.getInt(PropertyName.addedValuePerHitMinutes));
    }

    public int compare(JSONObject a, JSONObject b) {
        int output = compareCategories(a, b);
        if (output == 0) {
            output = compareSources(a, b);
        }
        if (output != 0) {
            return output;
        }
        Feed feed = (Feed) getJsonView();
        feed.setJsonData(a);
        long t1 = computeTimeForSorting(feed);
        feed.setJsonData(b);
        long t2 = computeTimeForSorting(feed);
        if (isInvertSort()) {
            output = t1 > t2 ? -1 : t1 < t2 ? 1 : 0;
            return output;
        } else if (t1 > t2) {
            return 1;
        } else {
            return t1 < t2 ? -1 : 0;
        }
    }

    public int compareCategories(JSONObject a, JSONObject b) {
        if (this.preferredCategories == null || this.preferredCategories.isEmpty()) {
            return 0;
        }
        boolean hasA = isAcceptableCategory(a);
        boolean hasB = isAcceptableCategory(b);
        if (hasA) {
            if (hasB) {
                return 0;
            }
            return 1;
        } else if (hasB) {
            return -1;
        } else {
            return 0;
        }
    }

    private boolean isAcceptableCategory(JSONObject data) {
        Feed feed = (Feed) getJsonView();
        feed.setJsonData(data);
        for (String preferredCat : this.preferredCategories) {
            if (feed.matches(this.context, preferredCat)) {
                return true;
            }
        }
        return false;
    }

    public int compareSources(JSONObject a, JSONObject b) {
        if (this.preferredSources == null || this.preferredSources.isEmpty()) {
            return 0;
        }
        Feed feed = (Feed) getJsonView();
        feed.setJsonData(a);
        String catA = feed.getSourceName(this.context);
        feed.setJsonData(b);
        String catB = feed.getSourceName(this.context);
        boolean hasA = this.preferredSources.contains(catA);
        boolean hasB = this.preferredSources.contains(catB);
        if (hasA) {
            if (hasB) {
                return 0;
            }
            return 1;
        } else if (hasB) {
            return -1;
        } else {
            return 0;
        }
    }

    private long computeTimeForSorting(Feed feed) {
        Date date = feed.getDate(getDateColumn());
        long time = date == null ? -1 : date.getTime();
        if (time == -1) {
            return time;
        }
        int hitcount = feed.getHitcount(this.context, false);
        if (hitcount > 0) {
            return time + getAddedValueTime(hitcount);
        }
        return time;
    }

    private long getAddedValueTime(int hits) {
        long addedValue = ((long) hits) * this.valuePerHit;
        if (addedValue > this.addedValueMax) {
            return this.addedValueMax;
        }
        return addedValue;
    }

    private int compareOld(JSONObject a, JSONObject b) {
        int x;
        Feed feed = (Feed) getJsonView();
        synchronized (this) {
            feed.setJsonData(a);
            int hit_a = feed.getHitcount(this.context, false);
            Date date_a = feed.getDate(getDateColumn());
            feed.setJsonData(b);
            int hit_b = feed.getHitcount(this.context, false);
            Date date_b = feed.getDate(getDateColumn());
            x = compareInts(hit_a, hit_b);
            if (x == 0) {
                x = compareDates(date_a, date_b);
            }
        }
        return x;
    }

    public Set<String> getPreferredCategories() {
        return this.preferredCategories;
    }

    public void setPreferredCategories(Set<String> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    public Set<String> getPreferredSources() {
        return this.preferredSources;
    }

    public void setPreferredSources(Set<String> preferredSources) {
        this.preferredSources = preferredSources;
    }
}

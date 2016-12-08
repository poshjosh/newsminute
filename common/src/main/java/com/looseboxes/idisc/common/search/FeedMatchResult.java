package com.looseboxes.idisc.common.search;

import com.looseboxes.idisc.common.jsonview.Feed;

import org.json.simple.JSONObject;

/**
 * Created by poshjosh on 5/3/2016.
 */
public class FeedMatchResult extends SingleMatchResult {

    public FeedMatchResult(Feed feed) {
        super(feed.getHeading(null));
    }

    public FeedMatchResult(JSONObject data, String columnName) {
        super((String)data.get(columnName));
    }
}

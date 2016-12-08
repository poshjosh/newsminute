package com.looseboxes.idisc.common.search;

import com.looseboxes.idisc.common.jsonview.FeedNames;

import org.json.simple.JSONObject;

import java.util.regex.MatchResult;

/**
 * Created by poshjosh on 5/3/2016.
 */
public class FeedSearcherFeedid extends FeedSearcher<Long> {

    public FeedSearcherFeedid() { }

    @Override
    public Long getResult(String searched, JSONObject data, MatchResult found) {
        return (Long)data.get(FeedNames.feedid);
    }
}

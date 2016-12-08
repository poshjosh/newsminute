package com.looseboxes.idisc.common.search;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;

import org.json.simple.JSONObject;

import java.util.regex.MatchResult;

/**
 * Created by Josh on 12/6/2016.
 */
public class FeedSearchResult implements FeedSearcherSearchResult.SearchResult {

    private final Long searchedFeedId;
    private final String searchedText;
    private final JSONObject searchedFeed;
    private final MatchResult matchResult;

    public FeedSearchResult(JSONObject searchedFeed) {
        this(new Feed(), searchedFeed);
    }

    public FeedSearchResult(Feed feed, JSONObject feedData) {
        feed.setJsonData(feedData);
        this.searchedFeedId = feed.getFeedid();
        this.matchResult = new FeedMatchResult(feed);
        this.searchedText = Util.requireNonNullOrEmpty(this.matchResult.group(), "Search text cannot be null");
        this.searchedFeed = feedData;
    }

    public FeedSearchResult(String searchedText, JSONObject searchedFeed, MatchResult matchResult) {
        this.searchedFeedId = (Long)searchedFeed.get(FeedNames.feedid);
        this.matchResult = matchResult;
        this.searchedText = searchedText;
        this.searchedFeed = searchedFeed;
    }

    public Long getSearchedDataId() {
        return this.searchedFeedId;
    }
    public String getSearchedText() {
        return this.searchedText;
    }
    public JSONObject getSearchedData() {
        return this.searchedFeed;
    }
    public MatchResult getMatchResult() { return this.matchResult; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append('{');
        builder.append("searchedFeedId=");
        if(searchedFeedId != null) {
            builder.append(searchedFeedId);
        }
        builder.append(", found=");
        if(matchResult != null) {
            builder.append(matchResult.group());
        }
        builder.append('}');
        return builder.toString();
    }
}

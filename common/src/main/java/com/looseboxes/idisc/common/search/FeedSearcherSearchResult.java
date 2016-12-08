package com.looseboxes.idisc.common.search;

import com.looseboxes.idisc.common.jsonview.FeedNames;

import org.json.simple.JSONObject;

import java.util.regex.MatchResult;

public class FeedSearcherSearchResult extends FeedSearcher<FeedSearcherSearchResult.SearchResult<JSONObject>> {

    public interface SearchResult<E> {

        Long getSearchedDataId();

        E getSearchedData();

        String getSearchedText();

        MatchResult getMatchResult();
    }

    public static class SearchResultImpl implements SearchResult<JSONObject> {

        private final Long searchedFeedId;
        private final String searchedText;
        private final JSONObject searchedFeed;
        private final MatchResult matchResult;

        public SearchResultImpl(String searchedText, JSONObject searchedFeed, MatchResult matchResult) {
            this.searchedFeedId = (Long)searchedFeed.get(FeedNames.feedid);
            this.searchedText = searchedText;
            this.searchedFeed = searchedFeed;
            this.matchResult = matchResult;
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

    public FeedSearcherSearchResult() { }

    @Override
    public SearchResult<JSONObject> getResult(String searched, JSONObject data, MatchResult found) {
        return new SearchResultImpl(searched, data, found);
    }
}

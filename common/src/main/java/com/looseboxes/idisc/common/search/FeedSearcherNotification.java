package com.looseboxes.idisc.common.search;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.AppCore;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.notice.FeedNotificationFilter;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;

/**
 * Created by Josh on 12/7/2016.
 */
public class FeedSearcherNotification extends FeedSearcherSearchResult{

    private final Context context;

    public FeedSearcherNotification(Context context) {
        this.context = context;
    }

    @Override
    protected String getTextToSearch(JSONObject json, int index, String outputIfNone, String outputAtEnd) {
        super.setJsonData(json);
        String output;
        switch(index) {
            case 0: output = this.getSourceName(); break;
            case 1: output = this.getTitle(); break;
            case 2: output = outputIfNone; break;
            case 3: output = outputAtEnd; break;
            default: throw new UnsupportedOperationException();
        }
        return output != outputAtEnd && output == null ? outputIfNone : output;
    }

    public FeedSearcherSearchResult.SearchResult<JSONObject> searchForNotification(
            Feed feedView, List<JSONObject> feeds, boolean aggressive,
            FeedSearcherSearchResult.SearchResult<JSONObject> outputIfNone) {

        if (!isCompatibleAndroidVersion() || feeds == null || feeds.isEmpty()) {
            return outputIfNone;
        }

        final FeedFilter feedFilter = new FeedNotificationFilter(context);

        final int sizeA = feeds == null ? 0 : feeds.size();

        feeds = feedView.getFeeds(feeds, feedFilter);

        final int sizeB = feeds == null ? 0 : feeds.size();

        Logx.getInstance().debug(this.getClass(), "Feed size. Before filter: {0}, after: {1}",  sizeA, sizeB);

        if (feeds == null || feeds.isEmpty()) {
            return outputIfNone;
        }

        FeedSearcherSearchResult.SearchResult<JSONObject> found = this.searchForHotNews(feedView, feeds);

        Logx.getInstance().debug(this.getClass(), "Found hot news: {0}", found == null ? null : found.getSearchedText());

        if(found == null) {

            found = searchForMostCommonUserWordsToBeNotifiedOf(feedView, feeds);

            Logx.getInstance().debug(this.getClass(), "Found user preferred words: {0}", found == null ? null : found.getSearchedText());

            if(found == null) {

                if(aggressive || isLongestFeedDownloadIntervalElapsed()) {

                    found = getMostViewed(feedView, feeds, feedFilter);

                    Logx.getInstance().debug(this.getClass(), "Found most used: {0}", found == null ? null : found.getSearchedText());

                    if(found == null && feedFilter != null) {

                        found = getMostViewed(feedView, feeds, null);

                        Logx.getInstance().log(Log.DEBUG, this.getClass(), "{0}", found);
                    }
                }
            }
        }

        return found == null ? outputIfNone : found;
    }

    protected FeedSearcherSearchResult.SearchResult<JSONObject> searchForHotNews(Feed feed, List<JSONObject> feeds) {

        FeedSearcherSearchResult.SearchResult<JSONObject> output = null;

        final String hotnewsCategoryName = PropertiesManager.PropertyName.hotnewsCategoryName.name();

        for(final JSONObject json : feeds) {
            feed.setJsonData(json);
            String categories = feed.getCategories();
            if(categories == null || categories.isEmpty()) {
                continue;
            }

            final int index = categories.indexOf(hotnewsCategoryName);

            if(index != -1) {

                final Long feedid = feed.getFeedid();

                final MatchResult matchResult = new SingleMatchResult(index, hotnewsCategoryName);

                output = new FeedSearcherSearchResult.SearchResult() {
                    @Override
                    public Long getSearchedDataId() {
                        return feedid;
                    }
                    @Override
                    public JSONObject getSearchedData() {
                        return json;
                    }
                    @Override
                    public String getSearchedText() {
                        return hotnewsCategoryName;
                    }
                    @Override
                    public MatchResult getMatchResult() {
                        return matchResult;
                    }
                };

                break;
            }
        }

        return output;
    }

    protected FeedSearcherSearchResult.SearchResult<JSONObject> searchForMostCommonUserWordsToBeNotifiedOf(Feed feed, List<JSONObject> feeds) {

        Map<String, List<FeedSearcherSearchResult.SearchResult<JSONObject>>> found =
                this.searchForUserPreferenceWordsToBeNotifiedOf(context, feeds);

        if (found == null || found.isEmpty()) {
            return null;
        }

        List<FeedSearcherSearchResult.SearchResult<JSONObject>> mostcommon = this.getMostCommon(found);

        if (mostcommon == null || mostcommon.isEmpty()) {
            return null;
        }
        return mostcommon.get(0);
    }

    public FeedSearcherSearchResult.SearchResult<JSONObject> getMostViewed(Feed feed, List<JSONObject> feeds, FeedFilter filter) {
        return toSearchResult(feed, feed.getMostViewed(context, feeds, filter));
    }

    public FeedSearcherSearchResult.SearchResult<JSONObject> getMostRecent(Feed feed, List<JSONObject> feeds) {
        return toSearchResult(feed, feeds.get(feed.getLatestIndex(feeds)));
    }

    private FeedSearcherSearchResult.SearchResult<JSONObject> toSearchResult(Feed feed, JSONObject json) {
        return json == null ? null : new FeedSearchResult(feed, json);
    }

    public boolean isLongestFeedDownloadIntervalElapsed() {

        final long lastFeedNoticeTime = Pref.getLong(context, FileIO.getLastFeedNotificationTimeFilename(), -1L);

        final int longestSyncIntervalOption = Pref.getLastSyncIntervalOption(context, 240);

        return (lastFeedNoticeTime != -1L &&
                (System.currentTimeMillis() - lastFeedNoticeTime) > TimeUnit.MINUTES.toMillis(longestSyncIntervalOption));
    }

    public boolean isCompatibleAndroidVersion() {
        return AppCore.isAcceptableVersion(this.context, 16);
    }
}

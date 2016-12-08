package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.search.FeedSearcherFeedid;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Josh on 8/10/2016.
 */
public class FeedFilterSelectedFeed implements FeedFilter{

    private int accepted;

    private final int size;

    private final Feed selectedFeed;

    private final List<Long> acceptedFeedids;

    public FeedFilterSelectedFeed(Context context, Collection<JSONObject> download, Feed selectedFeed, int size) {

        this.size = size;

        this.selectedFeed = selectedFeed;

        acceptedFeedids = new ArrayList<>(size);

        FeedSearcherFeedid feedSearcher = new FeedSearcherFeedid();

        final String heading = selectedFeed.getHeading(null);

        int totalAdded = 0;

        if(heading != null) {

            Map<String, List<Long>> relatedResults = feedSearcher.searchForLine(heading, download, 0, size);

            int added = this.addFeedidLists(relatedResults.values());

            Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Added {0} of {1} related results",
                    added, relatedResults==null?null:relatedResults.size());

            totalAdded += added;
        }

        if(totalAdded == 0) {

            Map<String, List<Long>> userPreferredResults =
                    feedSearcher.searchForUserPreferenceWordsToBeNotifiedOf(context, download, size);

            int added = this.addFeedidLists(userPreferredResults.values());

            Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Added {0} of {1} user preferred results",
                    added, userPreferredResults == null ? null : userPreferredResults.size());
        }
    }
    int addFeedidLists(Collection<List<Long>> results) {
        int added = 0;
        for(List<Long> feedids:results) {
            added += this.addFeedids(feedids);
        }
        return added;
    }
    int addFeedids(Collection<Long> feedids) {
        int added = 0;
        for(Long feedid:feedids) {
            if(addFeedid(feedid)) {
                ++added;
            }
        }
        return added;
    }
    boolean addFeedid(Long feedid) {
        if(acceptedFeedids.size() >= this.size) {
            return false;
        }else{
            if(selectedFeed != null && selectedFeed.getFeedid().equals(feedid)) {
                return false;
            }else{
                acceptedFeedids.add(feedid);
                return true;
            }
        }
    }

    @Override
    public boolean accept(Feed feed) {
        if(accepted >= size) {
            return false;
        }else {
            return acceptedFeedids.contains(feed.getFeedid());
        }
    }
}

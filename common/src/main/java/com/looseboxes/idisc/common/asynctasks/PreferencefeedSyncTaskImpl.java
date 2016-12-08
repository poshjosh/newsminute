package com.looseboxes.idisc.common.asynctasks;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedSyncResultHandler;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds;
import com.looseboxes.idisc.common.util.DownloadInterval;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Created by Josh on 8/4/2016.
 */
public class PreferencefeedSyncTaskImpl extends PreferencefeedSyncTask {

    private DownloadInterval downloadInterval;

    private PreferencefeedSyncResultHandler resultHandler;

    public PreferencefeedSyncTaskImpl(
            DownloadInterval downloadInterval, Preferencefeeds preferencefeeds,
            PreferencefeedSyncResultHandler resultHandler) {

        super(preferencefeeds);

        this.downloadInterval = Util.requireNonNull(downloadInterval);

        this.resultHandler = resultHandler;
    }

    @Override
    protected void after(String downloaded) {
        super.after(downloaded);
        this.downloadInterval.destroy();
        this.downloadInterval = null;
        this.resultHandler = null;
    }

    public void processError() {
        super.processError();
        if(resultHandler != null) {
            resultHandler.processSyncFailure();
        }
    }

    protected void processParsed(List<JSONObject> addedFeeds, List<JSONObject> removedFeeds, List<Long> feedids) {

        this.downloadInterval.setLastDownloadTime(System.currentTimeMillis());

        this.log(addedFeeds, removedFeeds, feedids);

        final Preferencefeeds preferencefeeds = this.getPreferencefeeds();

        if (addedFeeds != null && !addedFeeds.isEmpty()) {
            preferencefeeds.addAll(addedFeeds);
            Set<Long> addedFeedids = preferencefeeds.getFeedIds(addedFeeds);
            preferencefeeds.getAddedFeedids().removeAll(addedFeedids);
        }

        if (removedFeeds != null && !removedFeeds.isEmpty()) {
            preferencefeeds.removeAll(removedFeeds);
            Set<Long> removedFeedids = preferencefeeds.getFeedIds(removedFeeds);
            preferencefeeds.getDeletedFeedids().removeAll(removedFeedids);
        }

        if(resultHandler != null) {
            resultHandler.processSyncResponse(addedFeeds, removedFeeds, isPositiveCompletion());
        }
    }

    private void log(List<JSONObject> addedFeeds, List<JSONObject> removedFeeds, List<Long> feedids) {
        if (Logx.getInstance().isLoggable(3)) {
            StringBuilder builder = new StringBuilder();
            int addedSize = addedFeeds == null ? 0 : addedFeeds.size();
            builder.append("Added ").append(addedSize).append(" feeds");
            int removedSize = removedFeeds == null ? 0 : removedFeeds.size();
            builder.append("\nRemoved ").append(removedSize).append(" feeds");
//            builder.append("\nFeed IDs list:\n").append(feedids);
            Feed feed = new Feed();
            if (addedSize > 0) {
                int i = 0;
                for(JSONObject added:addedFeeds) {
                    feed.setJsonData(added);
                    builder.append("\nAdded["+(i++)+"]: ").append(feed.getHeading("Error accessing feed heading"));
                }
            }
            if (removedSize > 0) {
                int i = 0;
                for(JSONObject removed:removedFeeds) {
                    feed.setJsonData(removed);
                    builder.append("\nRemoved["+(i++)+"]: ").append(feed.getHeading("Error accessing feed heading"));
                }
            }
            Logx.getInstance().debug(getClass(), builder.toString());
            if (!isNoUI()) {
                Popup.getInstance().alert(getContext(), builder.toString(), null, getContext().getString(R.string.msg_ok));
            }
        }
    }
}


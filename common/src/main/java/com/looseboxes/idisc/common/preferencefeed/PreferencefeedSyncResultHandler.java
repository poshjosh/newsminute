package com.looseboxes.idisc.common.preferencefeed;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by Josh on 8/4/2016.
 */
public interface PreferencefeedSyncResultHandler {

    void processSyncResponse(List<JSONObject> addedFeeds, List<JSONObject> removedFeeds, boolean isPositiveCompletion);

    void processSyncFailure();
}

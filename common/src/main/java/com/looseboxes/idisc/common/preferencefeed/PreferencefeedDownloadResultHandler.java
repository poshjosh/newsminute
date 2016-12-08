package com.looseboxes.idisc.common.preferencefeed;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by Josh on 8/4/2016.
 */
public interface PreferencefeedDownloadResultHandler {

    void processDownloadResponse(List<JSONObject> list, boolean success);

    void processDownloadFailure();
}

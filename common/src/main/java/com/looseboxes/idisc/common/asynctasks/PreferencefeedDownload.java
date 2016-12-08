package com.looseboxes.idisc.common.asynctasks;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedResultHandler;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds;
import com.looseboxes.idisc.common.util.DownloadInterval;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by Josh on 8/4/2016.
 */
public class PreferencefeedDownload extends FeedDownloadTask {

    private final DownloadInterval downloadInterval;

    private final Preferencefeeds preferencefeeds;

    private final PreferencefeedResultHandler resultHandler;

    public PreferencefeedDownload(
                                  DownloadInterval downloadInterval, Preferencefeeds preferencefeeds,
                                  PreferencefeedResultHandler resultHandler) {
        super(preferencefeeds.getContext());
        this.downloadInterval = Util.requireNonNull(downloadInterval);
        this.preferencefeeds = Util.requireNonNull(preferencefeeds);
        this.resultHandler = resultHandler;
    }

    public String getOutputKey() {
        return FileIO.getFeedskey(this.preferencefeeds.getPreferenceType());
    }

    public void processError() {
        super.processError();
        if(resultHandler != null) {
            resultHandler.processDownloadFailure();
        }
    }

    public void onSuccess(List<JSONObject> download) {
        this.downloadInterval.setLastDownloadTime(System.currentTimeMillis());
        if (download != null && !download.isEmpty()) {
            synchronized (preferencefeeds) {
                preferencefeeds.addAll(download);
            }
        }
        if(resultHandler != null) {
            resultHandler.processDownloadResponse(download, isPositiveCompletion());
        }
    }
}


package com.looseboxes.idisc.common.preferencefeed;

/**
 * Created by Josh on 8/4/2016.
 */
public interface PreferencefeedResultHandler
        extends PreferencefeedSyncResultHandler, PreferencefeedDownloadResultHandler {

    void refreshDisplay();

    void clearDisplay();
}

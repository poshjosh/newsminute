package com.looseboxes.idisc.common.preferencefeed;

import android.content.Context;

import com.looseboxes.idisc.common.activities.PreferenceFeedsActivity;
import com.looseboxes.idisc.common.asynctasks.AbstractReadTask;
import com.looseboxes.idisc.common.asynctasks.PreferencefeedSyncTaskImpl;
import com.looseboxes.idisc.common.asynctasks.PreferencefeedDownload;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.util.DownloadInterval;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Set;

public class PreferencefeedManager extends Preferencefeeds {

    private boolean noUI;

    private DownloadInterval downloadInterval;

    private PreferencefeedResultHandler resultHandler;

    public PreferencefeedManager(PreferenceFeedsActivity activity, boolean noUI) {
        this(activity, -1, activity.getPreferenceType(), noUI, activity);
    }

    public PreferencefeedManager(PreferenceFeedsActivity activity, long updateIntervalMillis, boolean noUI) {
        this(activity, updateIntervalMillis, activity.getPreferenceType(), noUI, activity);
    }

    public PreferencefeedManager(Context context, PreferenceType preferenceType, boolean noUI, PreferencefeedResultHandler resultHandler) {
        this(context, -1, preferenceType, noUI, resultHandler);
    }

    public PreferencefeedManager(Context context, long updateIntervalMillis, PreferenceType preferenceType, boolean noUI, PreferencefeedResultHandler resultHandler) {
        super(context, preferenceType);
        this.noUI = noUI;
        this.downloadInterval = updateIntervalMillis < 1 ? new PreferencefeedDownloadInterval(context, preferenceType) :
                new PreferencefeedDownloadInterval(context, preferenceType, updateIntervalMillis);
        this.resultHandler = resultHandler;
    }

    @Override
    public void destroy() {
        super.destroy();
        this.downloadInterval.destroy();
        this.downloadInterval = null;
        this.resultHandler = null;
    }

    public String getOutputKey() {

        return FileIO.getFeedskey(getPreferenceType());
    }

    public String getFilename() {
        return FileIO.getFeedsFilename(getPreferenceType());
    }

    public void update(boolean ignoreSchedule) {
        Set<JSONObject> feeds = this.getFeeds();
        if (ignoreSchedule || feeds == null || feeds.isEmpty() || this.downloadInterval.isNextDownloadDue()) {
            update();
        }
    }

    protected void update() {
        if (isVirgin()) {
            this.createDownloadTask().execute();
        } else {
            this.createSyncTask().execute();
        }
    }

    public AbstractReadTask<List<JSONObject>> createDownloadTask() {
        PreferencefeedDownload downloadTask = new PreferencefeedDownload(this.downloadInterval, this, this.resultHandler);
        downloadTask.setNoUI(this.noUI);
        return downloadTask;
    }

    public AbstractReadTask<JSONObject> createSyncTask() {
        PreferencefeedSyncTaskImpl syncTask = new PreferencefeedSyncTaskImpl(this.downloadInterval,  this, this.resultHandler);
        syncTask.setNoUI(this.noUI);
        return syncTask;
    }

    public boolean add(JSONObject element) {

        final boolean added = super.add(element);

        if(added && resultHandler != null) {
            resultHandler.refreshDisplay();
        }
//        this.createSyncTask().execute();
        return added;
    }

    public void clear() {

        super.clear();

        if(resultHandler != null) {
            resultHandler.clearDisplay();
        }
//        this.createSyncTask().execute();
    }

    public boolean remove(JSONObject element) {

        final boolean removed = super.remove(element);

        if(removed && resultHandler != null) {

            resultHandler.refreshDisplay();
        }
//        this.createSyncTask().execute();
        return removed;
    }

    public final boolean isNoUI() {
        return this.noUI;
    }
}

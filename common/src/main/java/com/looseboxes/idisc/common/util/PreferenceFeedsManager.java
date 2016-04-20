package com.looseboxes.idisc.common.util;

import android.content.Context;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.activities.PreferenceFeedsActivity;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadTask;
import com.looseboxes.idisc.common.asynctasks.FeedPreferenceSynchronizationTask;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PreferenceFeedsManager {
    private CachedSet<Long> _afm_accessViaGetter;
    private Feed _f;
    private CachedSet<JSONObject> _f_accessViaGetter;
    private CachedSet<Long> _iam_accessViaGetter;
    private PreferenceFeedsActivity activity;
    private DownloadInterval downloadInterval;
    private boolean noUI;
    private Object targetLock = new Serializable() { };
    private final PreferenceType preferenceType;

    public enum PreferenceType {
        bookmarks,
        favorites
    }

    private class SynchronizationTask extends FeedPreferenceSynchronizationTask {
        public SynchronizationTask(CachedSet<Long> addfeedids, CachedSet<Long> removefeedids, Set<Long> feedids) {
            super(PreferenceFeedsManager.this.getContext(), addfeedids, removefeedids, feedids, PreferenceFeedsManager.this.getPreferenceType());
        }

        public void processError() {
            super.processError();
            PreferenceFeedsManager.this.processSyncFailure();
        }

        protected void processParsed(List<JSONObject> addedFeeds, List<JSONObject> removedFeeds, List<Long> feedids) {
            boolean noneAdded;
            boolean noneRemoved = false;
            PreferenceFeedsManager.this.downloadInterval.setLastDownloadTime(System.currentTimeMillis());
            PreferenceFeedsManager.this.log(addedFeeds, removedFeeds, feedids);
            PreferenceFeedsManager pfm = PreferenceFeedsManager.this;
            if (addedFeeds == null || addedFeeds.isEmpty()) {
                noneAdded = true;
            } else {
                noneAdded = false;
            }
            if (removedFeeds == null || removedFeeds.isEmpty()) {
                noneRemoved = true;
            }
            synchronized (PreferenceFeedsManager.this.targetLock) {
                CachedSet<JSONObject> feeds = pfm.getFeeds();
                if (!noneAdded) {
                    try {
                        feeds.addAll(addedFeeds);
                    } catch (Throwable th) {
                        feeds.close();
                    }
                }
                Feed feed = PreferenceFeedsManager.this.getFeedFormat();
                if (!noneRemoved) {
                    int textComparisonMaxLen = App.getPropertiesManager(getContext()).getInt(PropertyName.textComparisonMaxLength);
                    Iterator<JSONObject> iter = feeds.iterator();
                    while (iter.hasNext()) {
                        if (feed.contains(removedFeeds, (JSONObject) iter.next(), 0.0f, textComparisonMaxLen)) {
                            iter.remove();
                        }
                    }
                }
                feeds.close();
            }
            PreferenceFeedsManager.this.processSyncResponse(addedFeeds, removedFeeds, isPositiveCompletion());
        }
    }

    private class DownloadTask extends FeedDownloadTask {
        public DownloadTask() {
            super(PreferenceFeedsManager.this.getContext());
        }

        public String getOutputKey() {
            return FileIO.getFeedskey(PreferenceFeedsManager.this.getPreferenceType());
        }

        public void processError() {
            super.processError();
            PreferenceFeedsManager.this.processDownloadFailure();
        }

        public void onSuccess(List<JSONObject> download) {
            PreferenceFeedsManager.this.downloadInterval.setLastDownloadTime(System.currentTimeMillis());
            if (!(download == null || download.isEmpty())) {
                synchronized (PreferenceFeedsManager.this.targetLock) {
                    CachedSet<JSONObject> target = PreferenceFeedsManager.this.getFeeds();
                    try {
                        target.addAll(download);
                        target.close();
                    } catch (Throwable th) {
                        target.close();
                    }
                }
            }
            PreferenceFeedsManager.this.processDownloadResponse(download, isPositiveCompletion());
        }
    }

    public PreferenceFeedsManager(PreferenceFeedsActivity activity) {
        if(activity.getPreferenceType() == null) {
            throw new NullPointerException();
        }
        this.preferenceType = activity.getPreferenceType();
        init(activity, activity.getPreferenceType(), TimeUnit.HOURS.toMillis(App.getPropertiesManager(activity).getLong(PropertyName.syncIntervalHours)));
        this.activity = activity;
    }

    public PreferenceFeedsManager(Context context, PreferenceType preferenceType) {
        if(preferenceType == null) {
            throw new NullPointerException();
        }
        this.preferenceType = preferenceType;
        init(context, preferenceType, TimeUnit.HOURS.toMillis(App.getPropertiesManager(context).getLong(PropertyName.syncIntervalHours)));
    }

    public PreferenceFeedsManager(PreferenceFeedsActivity activity, long updateIntervalMillis) {
        if(activity.getPreferenceType() == null) {
            throw new NullPointerException();
        }
        this.preferenceType = activity.getPreferenceType();
        init(activity, activity.getPreferenceType(), updateIntervalMillis);
        this.activity = activity;
    }

    public PreferenceFeedsManager(Context context, long updateIntervalMillis, PreferenceType preferenceType) {
        if(preferenceType == null) {
            throw new NullPointerException();
        }
        this.preferenceType = preferenceType;
        init(context, preferenceType, updateIntervalMillis);
    }

    public PreferenceType getPreferenceType(){
        return this.preferenceType;
    }

    private void init(Context context, PreferenceType preferenceType, long updateIntervalMillis) {
        if(preferenceType == null) {
            throw new NullPointerException();
        }
        this.downloadInterval = new DownloadInterval(context, getClass().getName() + "." + preferenceType.name() + ".lastDownloadTime.long", updateIntervalMillis);
    }

    protected boolean isVirgin() {
        CachedSet<Long> addFeedids = getAddedFeedids();
        CachedSet<Long> removeFeedids = getDeletedFeedids();
        CachedSet feeds = getFeeds();
        return (addFeedids == null || addFeedids.isEmpty()) && ((removeFeedids == null || removeFeedids.isEmpty()) && (feeds == null || feeds.isEmpty()));
    }

    protected void processSyncResponse(List<JSONObject> list, List<JSONObject> list2, boolean success) {
    }

    protected void processDownloadResponse(List<JSONObject> list, boolean success) {
    }

    protected void processDownloadFailure() {
    }

    protected void processSyncFailure() {
    }

    public boolean contains(JSONObject element) {
        boolean z;
        synchronized (this.targetLock) {
            Collection<JSONObject> list = getFeeds();
            if (list == null) {
                z = false;
            } else {
                z = getFeedFormat().contains(list, element, 0.0f, App.getPropertiesManager(getContext()).getInt(PropertyName.textComparisonMaxLength));
            }
        }
        return z;
    }

    public boolean add(JSONObject element) {
        boolean added = false;
        synchronized (this.targetLock) {
            CachedSet<JSONObject> feeds = getFeeds();
            try {
                added = feeds.add(element);
                if (added) {
                    Feed feedFmt = getFeedFormat();
                    feedFmt.setJsonData(element);
                    addAddedFeedId(feedFmt.getFeedid());
                    clearDisplay();
                }
                feeds.close();
                synchronize();
            } catch (Throwable th) {
                feeds.close();
            }
        }
        return added;
    }

    public void clear() {
        synchronized (this.targetLock) {
            CachedSet<JSONObject> c = getFeeds();
            try {
                Set<Long> feedids = getFeedIds(c);
                c.clear();
                for (Long feedid : feedids) {
                    addDeletedFeedId(feedid);
                }
                clearDisplay();
                c.close();
                synchronize();
            } catch (Throwable th) {
                c.close();
            }
        }
    }

    public boolean remove(JSONObject element) {
        boolean removed;
        synchronized (this.targetLock) {
            CachedSet<JSONObject> feeds = getFeeds();
            if (feeds != null) {
                try {
                    if (!feeds.isEmpty()) {
                        removed = feeds.remove(element);
                        if (removed) {
                            Feed feedFmt = getFeedFormat();
                            feedFmt.setJsonData(element);
                            addDeletedFeedId(feedFmt.getFeedid());
                            clearDisplay();
                        }
                        feeds.close();
                        synchronize();
                    }
                } catch (Throwable th) {
                    feeds.close();
                }
            }
            removed = false;
            if (removed) {
                Feed feedFmt2 = getFeedFormat();
                feedFmt2.setJsonData(element);
                addDeletedFeedId(feedFmt2.getFeedid());
                clearDisplay();
            }
            feeds.close();
            synchronize();
        }
        return removed;
    }

    public void clearDisplay() {
        if (this.activity != null) {
            this.activity.getFeedDisplayHandler().clear();
        }
    }

    public String getOutputKey() {
        return FileIO.getFeedskey(getPreferenceType());
    }

    public String getFilename() {
        return FileIO.getFeedsFilename(getPreferenceType());
    }

    public void update(boolean ignoreSchedule) {
        CachedSet feeds = getFeeds();
        if (ignoreSchedule || feeds == null || feeds.isEmpty() || this.downloadInterval.isNextDownloadDue()) {
            update();
        }
    }

    protected void update() {
        if (isVirgin()) {
            download();
        } else {
            synchronize();
        }
    }

    protected void download() {
        DownloadTask updater = new DownloadTask();
        updater.setNoUI(this.noUI);
        updater.execute();
    }

    protected void synchronize() {
        SynchronizationTask sync = new SynchronizationTask(getAddedFeedids(), getDeletedFeedids(), getFeedIds(getFeeds()));
        sync.setNoUI(this.noUI);
        sync.execute();
    }

    public Set<Long> getFeedIds(Set<JSONObject> feeds) {
        if (feeds == null || feeds.isEmpty()) {
            return null;
        }
        Set<Long> feedids = new HashSet(feeds.size());
        Feed feedFmt = getFeedFormat();
        for (JSONObject feed : feeds) {
            feedFmt.setJsonData(feed);
            feedids.add(feedFmt.getFeedid());
        }
        return feedids;
    }

    public Feed getFeedFormat() {
        if (this._f == null) {
            this._f = new Feed();
        }
        return this._f;
    }

    public static void validatePrefix(String prefix) {
        switch(prefix) {
            case "add":
            case "get":
            case "remove":
                break;
            default:
                throw new UnsupportedOperationException("Expected: add, get or remove, but found: "+prefix);
        }
    }

    public CachedSet<JSONObject> getFeeds() {
        if (this._f_accessViaGetter == null) {
            this._f_accessViaGetter = new CachedSet(getContext(), PreferenceFeedsManager.class.getName() + ".sync." + getPreferenceType());
        }
        return this._f_accessViaGetter;
    }

    private boolean addAddedFeedId(Long feedid) {
        CachedSet<Long> list = getAddedFeedids();
        try {
            boolean add = list.add(feedid);
            return add;
        } finally {
            list.close();
        }
    }

    private boolean isAddedFeed(Long feedid) {
        CachedSet<Long> list = getAddedFeedids();
        return list == null ? false : list.contains(feedid);
    }

    private CachedSet<Long> getAddedFeedids() {
        if (this._afm_accessViaGetter == null) {
            this._afm_accessViaGetter = new CachedSet(getContext(), PreferenceFeedsManager.class.getName() + ".sync." + getPreferenceType() + "_to_add");
        }
        return this._afm_accessViaGetter;
    }

    private boolean addDeletedFeedId(Long feedid) {
        CachedSet<Long> list = getDeletedFeedids();
        try {
            boolean add = list.add(feedid);
            return add;
        } finally {
            list.close();
        }
    }

    private boolean isDeletedFeed(Long feedid) {
        CachedSet<Long> list = getDeletedFeedids();
        return list == null ? false : list.contains(feedid);
    }

    private CachedSet<Long> getDeletedFeedids() {
        if (this._iam_accessViaGetter == null) {
            this._iam_accessViaGetter = new CachedSet(getContext(), PreferenceFeedsManager.class.getName() + ".sync." + getPreferenceType() + "_to_remove");
        }
        return this._iam_accessViaGetter;
    }

    public PreferenceFeedsActivity getActivity() {
        return this.activity;
    }

    public Context getContext() {
        return this.downloadInterval.getContext();
    }

    private void log(List addedFeeds, List removedFeeds, List feedids) {
        if (Logx.isLoggable(3)) {
            StringBuilder builder = new StringBuilder();
            int addedSize = addedFeeds == null ? 0 : addedFeeds.size();
            builder.append("Added ").append(addedSize).append(" feeds");
            int removedSize = removedFeeds == null ? 0 : removedFeeds.size();
            builder.append("\nRemoved ").append(removedSize).append(" feeds");
            builder.append("\nFeed IDs list:\n").append(feedids);
            Feed feed = new Feed();
            if (addedSize > 0) {
                feed.setJsonData((JSONObject) addedFeeds.get(0));
                builder.append("\nAdded[0]: ").append(feed.getHeading("Error accessing feed heading"));
            }
            if (removedSize > 0) {
                feed.setJsonData((JSONObject) removedFeeds.get(0));
                builder.append("\nRemoved[0]: ").append(feed.getHeading("Error accessing feed heading"));
            }
            Logx.debug(getClass(), builder.toString());
            if (!isNoUI()) {
                Popup.alert(getContext(), builder.toString());
            }
        }
    }

    public boolean isNoUI() {
        return this.noUI;
    }

    public void setNoUI(boolean noUI) {
        this.noUI = noUI;
    }
}

package com.looseboxes.idisc.common.preferencefeed;

import android.content.Context;

import com.bc.android.core.io.IOWrapper;
import com.bc.android.core.util.DataSetLimitedSize;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.SetLimitedSize;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.activities.PreferenceFeedsActivity;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.util.PropertiesManager;

import org.json.simple.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Josh on 8/4/2016.
 */
public class Preferencefeeds {

    private Set<Long> addedFeedids;
    private Set<Long> deletedFeedids;
    private Set<JSONObject> feeds;

    private Feed feedFormat;
    private PreferenceType preferenceType;
    private Context context;

    public enum PreferenceType {
        bookmarks,
        favorites
    }

    public Preferencefeeds(PreferenceFeedsActivity activity) {
        this(activity, activity.getPreferenceType());
    }

    public Preferencefeeds(
            Context context, PreferenceType preferenceType) {

        this.context = Util.requireNonNull(context);
        this.preferenceType = Util.requireNonNull(preferenceType);
        this.feedFormat = new Feed();

        final int initialCapacity = 5;
        final int limit = App.getMemoryLimitedInt(context, PropertiesManager.PropertyName.preferenceFeedsBufferSize, 10);

        this.addedFeedids = this.load(getAddedFeedidsName());
        if(this.addedFeedids == null) {
            this.addedFeedids = new SetLimitedSize(initialCapacity, limit);
        }

        this.deletedFeedids = this.load(getDeletedFeedidsName());
        if(this.deletedFeedids == null) {
            this.deletedFeedids = new SetLimitedSize(initialCapacity, limit);
        }

        this.feeds = this.load(getFeedsName());
        if(this.feeds == null) {
            this.feeds = new DataSetLimitedSize(initialCapacity, limit, FeedNames.feedid);
        }
    }

    public void destroy() {
        this.addedFeedids.clear();
        this.addedFeedids = null;
        this.deletedFeedids.clear();
        this.deletedFeedids = null;
        this.context = null;
        this.feedFormat = null;
        this.feeds.clear();
        this.feeds = null;
        this.preferenceType = null;

    }

    public boolean isVirgin() {
        boolean virgin = (addedFeedids == null || addedFeedids.isEmpty()) && ((deletedFeedids == null || deletedFeedids.isEmpty()) && (feeds == null || feeds.isEmpty()));
        return virgin;
    }

    public boolean contains(JSONObject element) {
        return feeds.contains(element);
    }

    public boolean addAll(Collection<JSONObject> all) {
        boolean added = false;
        synchronized (feeds) {
            try {

                added = feeds.addAll(all);

                if(added) {

                    final Set<Long> feedids = getFeedIds(all);

                    this.addAddedFeedIds(feedids);
                }
            }finally {
                if(added) {
                    this.save(this.getFeedsName(), feeds);
                }
            }
        }
        return added;
    }

    public boolean add(JSONObject element) {

        return this.add(element, true);
    }

    public boolean add(JSONObject element, boolean close) {
        boolean added = false;
        synchronized (feeds) {
            try {
                added = feeds.add(element);
                if (added) {
                    feedFormat.setJsonData(element);
                    addAddedFeedId(feedFormat.getFeedid());
                }
            } catch (Throwable th) {
                Logx.getInstance().log(this.getClass(), th);
            }finally{
                if(added && close) {
                    this.save(getFeedsName(), feeds);
                }
            }
        }
        return added;
    }

    public void clear() {
        synchronized (feeds) {
            try {

                final Set<Long> feedids = getFeedIds();

                feeds.clear();

                this.addDeletedFeedIds(feedids);

            } catch (Throwable th) {
                Logx.getInstance().log(this.getClass(), th);
            }finally{
                this.save(getFeedsName(), feeds);
            }
        }
    }

    public boolean removeAll(Collection<JSONObject> all) {
        boolean removed = false;
        synchronized (feeds) {
            try {

                removed = feeds.removeAll(all);

                if(removed) {

                    final Set<Long> feedids = getFeedIds(all);

                    this.addDeletedFeedIds(feedids);
                }
            }finally {
                if (removed) {
                    this.save(getFeedsName(), feeds);
                }
            }
        }
        return removed;
    }

    public boolean remove(JSONObject element) {

        return this.remove(element, true);
    }

    public boolean remove(JSONObject element, boolean close) {
        boolean removed = false;
        synchronized (feeds) {
            try {
                if (!feeds.isEmpty()) {
                    removed = feeds.remove(element);
                    if (removed) {
                        feedFormat.setJsonData(element);
                        addDeletedFeedId(feedFormat.getFeedid());
                    }
                }
            } catch (Throwable th) {
                Logx.getInstance().log(this.getClass(), th);
            }finally {
                if(removed && close) {
                    this.save(getFeedsName(), feeds);
                }
            }
        }
        return removed;
    }

    public Set<Long> getFeedIds() {
        return this.getFeedIds(this.getFeeds());
    }

    public Set<Long> getFeedIds(Collection<JSONObject> feeds) {
        if (feeds == null || feeds.isEmpty()) {
            return null;
        }
        Set<Long> feedids = new HashSet(feeds.size());
        for (JSONObject feed : feeds) {
            feedFormat.setJsonData(feed);
            feedids.add(feedFormat.getFeedid());
        }
        return feedids;
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

    private void addAddedFeedIds(Set<Long> feedids) {
        Iterator<Long> iter = feedids.iterator();
        while(iter.hasNext()) {
            final Long feedid = iter.next();
            this.addAddedFeedId(feedid, !iter.hasNext());
        }
    }

    private boolean addAddedFeedId(Long feedid) {
        return this.addAddedFeedId(feedid, true);
    }

    private boolean addAddedFeedId(Long feedid, boolean close) {
        try {
            final boolean added = addedFeedids.add(feedid);
            if(added) {
                deletedFeedids.remove(feedid);
            }
            return added;
        } finally {
            if(close) {
                this.save(getAddedFeedidsName(), addedFeedids);
                this.save(getDeletedFeedidsName(), deletedFeedids);
            }
        }
    }

    private void addDeletedFeedIds(Set<Long> feedids) {
        Iterator<Long> iter = feedids.iterator();
        while(iter.hasNext()) {
            final Long feedid = iter.next();
            this.addDeletedFeedId(feedid, !iter.hasNext());
        }
    }

    private boolean addDeletedFeedId(Long feedid) {

        return this.addDeletedFeedId(feedid, true);
    }

    private boolean addDeletedFeedId(Long feedid, boolean close) {
        try {
            boolean added = deletedFeedids.add(feedid);
            if(added) {
                addedFeedids.remove(feedid);
            }
            return added;
        } finally {
            if(close) {
                this.save(getAddedFeedidsName(), addedFeedids);
                this.save(getDeletedFeedidsName(), deletedFeedids);
            }
        }
    }

    public boolean isAddedFeed(Long feedid) {
        return addedFeedids == null ? false : addedFeedids.contains(feedid);
    }

    public boolean isDeletedFeed(Long feedid) {
        return deletedFeedids == null ? false : deletedFeedids.contains(feedid);
    }

    public final int size() {
        return feeds == null || feeds.isEmpty() ? 0 : feeds.size();
    }

    public Set load(String filename) {

        final IOWrapper<Set> io = this.getIoWrapper();
        io.setFilename(filename);

        Set loaded = io.getTarget();

        Logx.getInstance().debug(this.getClass(), "For {0}, loaded:\n{1}", filename, loaded);

        return loaded;
    }

    private void save(String filename, Set set) {

        final IOWrapper<Set> ioWrapper = getIoWrapper();
        ioWrapper.setFilename(filename);

        if(set != null && set.isEmpty()) {
            set = null;
        }

        ioWrapper.setTarget(set);

        Logx.getInstance().debug(this.getClass(), "For {0}, saved:\n{1}", filename, set);
    }

    private WeakReference<IOWrapper<Set>> _$ref;
    private IOWrapper<Set> getIoWrapper() {
        IOWrapper<Set> tgt;
        if(_$ref == null || (tgt = _$ref.get()) == null) {
            tgt = new IOWrapper<>(context, null);
            tgt.setUseCache(false);
            _$ref = new WeakReference<>(tgt);
        }
        return tgt;
    }

    private String getAddedFeedidsName() {
        return "com.looseboxes.idisc.common.preferencefeeds." + preferenceType + "_to_add.set";
    }

    private String getDeletedFeedidsName() {
        return "com.looseboxes.idisc.common.preferencefeeds." + preferenceType + "_to_remove.set";
    }

    private String getFeedsName() {
        return "com.looseboxes.idisc.common.preferencefeeds." + preferenceType + "_feeds.set";
    }

    public final Set<JSONObject> getFeeds() {
        return feeds;
    }

    public final Set<Long> getAddedFeedids() {
        return this.addedFeedids;
    }

    public final Set<Long> getDeletedFeedids() {
        return this.deletedFeedids;
    }

    public final PreferenceType getPreferenceType() {
        return preferenceType;
    }

    public final Context getContext() {
        return context;
    }
}

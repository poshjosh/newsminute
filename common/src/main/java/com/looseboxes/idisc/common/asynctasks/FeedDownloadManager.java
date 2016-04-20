package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.io.JsonListIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedComparator;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.util.CachedSet;
import com.looseboxes.idisc.common.util.CloseableSet;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeedDownloadManager extends FeedDownloadTask {
    private static transient JsonListIO<JSONObject> _accessViaGetter;
    private static transient JsonListIO<Map<String, Object>> _cn;
    private static CloseableSet<Long> _iam_accessViaGetter;
    private static Serializable downloadLock;
    private static int removedCount;
    private static int updateCount;
    private Map _dsc;

    /* renamed from: com.looseboxes.idisc.common.asynctasks.FeedDownloadManager.2 */
    class AnonymousClass2 extends AsyncTask {
        final /* synthetic */ List val$downloaded;

        AnonymousClass2(List list) {
            this.val$downloaded = list;
        }

        protected Object doInBackground(Object[] params) {
            long tb4 = System.currentTimeMillis();
            try {
                FeedDownloadManager.this.doOnSuccess(this.val$downloaded);
                Logx.log(Log.DEBUG, getClass(), "Time spent updating downloads: {0}", Long.valueOf(System.currentTimeMillis() - tb4));
            } catch (Exception e) {
                Logx.log(getClass(), e);
                Logx.log(Log.DEBUG, getClass(), "Time spent updating downloads: {0}", Long.valueOf(System.currentTimeMillis() - tb4));
            } catch (Throwable th) {
                Logx.log(Log.DEBUG, getClass(), "Time spent updating downloads: {0}", Long.valueOf(System.currentTimeMillis() - tb4));
            }
            return null;
        }
    }

    static {
        downloadLock = new Serializable() {
        };
    }

    public FeedDownloadManager(Context context) {
        this(context, getLastDownloadTime(context, 0));
    }

    public FeedDownloadManager(Context context, long lastDownloadTime) {
        super(context, lastDownloadTime);
    }

    public void onPostSuccess(List<JSONObject> list) {
    }

    /**
     * This method is final. This is because we want all downloads handled by
     * this class to undergo specific logic. To provide further processing
     * for downloaded material override the {@link #onPostSuccess(java.util.List)}
     * method.
     * @param downloaded The downloaded feeds
     */
    public final void onSuccess(List<JSONObject> downloaded) {
        new AnonymousClass2(downloaded).execute(new Object[0]);
    }

    private void doOnSuccess(List<JSONObject> downloaded) {
        Class cls = FeedDownloadManager.class;
        String str = "Downloaded {0} feeds";
        Object[] objArr = new Object[1];
        objArr[0] = downloaded == null ? null : Integer.valueOf(downloaded.size());
        Logx.log(Log.DEBUG, cls, str, objArr);
        Logx.logLatest(getClass(), "update", downloaded);
        Logx.logCategories("Before update", getClass(), downloaded, "statuses");
        Context context = getContext();
        setLastDownloadTime(context, System.currentTimeMillis());
        List<Map<String, Object>> commentNotifications = updateCommentNotifications(context, downloaded);
        Feed feedView = new Feed();
        List downloaded2 = updateFeeds(context, feedView, downloaded);
        onPostSuccess(downloaded2);
        FeedNotificationHandler notificationHandler = new FeedNotificationHandler(context);
        notificationHandler.showCommentNotice(commentNotifications);
        notificationHandler.showFeedNotice(feedView, downloaded2);
        Logx.log(Log.VERBOSE, getClass(), "Exiting method #onSuccess(java.util.List)");
    }

    private List<Map<String, Object>> updateCommentNotifications(Context context, List<JSONObject> downloaded) {
        if (downloaded == null || downloaded.isEmpty()) {
            return null;
        }
        Iterator<JSONObject> iter = downloaded.iterator();
        while (iter.hasNext()) {
            List<Map<String, Object>> notices = (List) ((JSONObject) iter.next()).get("notices");
            if (notices != null) {
                iter.remove();
                if (!notices.isEmpty()) {
                    getCommentNotificationsIO(context, true).setTarget(notices);
                }
                return getCommentNotifications(context, false);
            }
        }
        return getCommentNotifications(context, false);
    }

    private java.util.List updateFeeds(Context context, Feed feedView, List<JSONObject> downloaded) {
        synchronized(downloadLock) {

            int emptyRemoved = feedView.removeEmptyFeeds(downloaded);

            Logx.log(Log.DEBUG, FeedDownloadManager.class, "Removed {0} empty feeds", emptyRemoved);

            // We only sort the incoming feeds, as we want to preserve
            // the order of the already available feeds
            //
            FeedComparator sorter = new FeedComparator(context);

            Set<String> prefCats = Pref.getPreferredCategories(context);
            sorter.setPreferredCategories(prefCats);
            Logx.debug(this.getClass(), "Preferred categories: {0}", prefCats);

            Set<String> prefSrcs = Pref.getPreferredSources(context);
            sorter.setPreferredSources(prefSrcs);
            Logx.debug(this.getClass(), "Preferred sources: {0}", prefSrcs);

            sorter.setInvertSort(true);

            Collections.sort(downloaded, sorter);

            final int maxCacheSize = getMaxCacheSize(context);

            List<JSONObject> cache = getDownload(context);
            if(cache == null) {
                cache = new ArrayList<>(maxCacheSize);
            }else{
                // We have to remove duplicates at this stage
                // so we have the correct size
                //
                Iterator<JSONObject> iter = downloaded.iterator();
                while(iter.hasNext()) {
                    JSONObject oval = iter.next();
                    if(feedView.contains(context, cache, oval)) {
                        iter.remove();
                    }
                }
            }

            // We remove feeds that this user have deleted
            //
            removeDeleted(context, feedView, downloaded);

            Logx.logLatest(this.getClass(), "cache", cache);

            if(cache.size() + downloaded.size() >= maxCacheSize) {

                final float loadFactor = 0.8f;

                int start = (int)(maxCacheSize * loadFactor);
                int end = cache.size();
                if(start < end) {
                    int excessRemoved = 0;
                    for(int i=start; i<end; i++) {
                        // We have to keep removing the last element
                        cache.remove(cache.size()-1);
                        ++excessRemoved;
                    }
                    removedCount = excessRemoved;

                    Logx.debug(this.getClass(), "Removed {0} excess feeds from cache.", excessRemoved);
                }
            }

            // Add to the head of the current list
            //
//@related_add to head of list Very important for related logic elsewhere to apply
            boolean duplicatesAllowed = true; // No problem as we have already removed duplicates, and this makes the oepration faster

            List<JSONObject> toAdd = this.getToAdd(feedView, cache, downloaded, duplicatesAllowed);

            Logx.debug(FeedDownloadManager.class, "Adding {0} downloaded feeds to cache.", toAdd==null?-1:toAdd.size());

            // Newer feeds are always added to the head of the list
            //
            if(toAdd != null && !toAdd.isEmpty()) {
                cache.addAll(0, toAdd);
            }

            updateCount = toAdd == null ? -1 : toAdd.size();

            setDownload(context, cache);

            this.updateAvailableCategories(context, cache);
            this.updateAvailableSources(context, cache);

//Logx.debug(null, this.getClass(), "Exiting method #updateFeeds(android.content.Context, java.util.List)");

            return toAdd;
        }
    }

    private void updateAvailableCategories(Context context, List<JSONObject> download) {
        if (download != null && !download.isEmpty()) {
            try {
                Set<Object> categories = App.getPropertiesManager(context).getSet(PropertyName.categories);
                Set<String> availCats = new HashSet(categories.size());
                Feed feed = new Feed();
                for (JSONObject o : download) {
                    feed.setJsonData(o);
                    for (Object cat : categories) {
                        String sval = cat.toString();
                        if (feed.matches(context, sval)) {
                            availCats.add(sval);
                            break;
                        }
                    }
                }
                Pref.setAvailableCategories(context, availCats);
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
    }

    private void updateAvailableSources(Context context, List<JSONObject> download) {
        if (download != null && !download.isEmpty()) {
            try {
                Set<String> availSources = new HashSet(getDefaultSources().size());
                Feed feed = new Feed();
                for (JSONObject o : download) {
                    feed.setJsonData(o);
                    String sourceName = feed.getSourceName(context);
                    if (sourceName != null) {
                        availSources.add(sourceName);
                    }
                }
                Pref.setAvailableSources(context, availSources);
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
    }

    private List<JSONObject> getToAdd(Feed feedView, List<JSONObject> addTo, List<JSONObject> toAdd, boolean duplicatesAllowed) {
        if (duplicatesAllowed) {
            return toAdd;
        }
        List<JSONObject> updated = new ArrayList(toAdd.size());
        for (JSONObject o : toAdd) {
            // Use custom contains implementation.. see doc of method
            if (!feedView.contains(getContext(), addTo, o)) {
                updated.add(o);
            }
        }
        return updated;
    }

    public static void removeDeleted(Context context, Feed feedView, List<JSONObject> downloaded) {
        if (downloaded != null && !downloaded.isEmpty()) {
            Iterator<JSONObject> iter = downloaded.iterator();
            while (iter.hasNext()) {
                feedView.setJsonData((JSONObject) iter.next());
                Long feedid = feedView.getFeedid();
                if (feedid != null && isDeletedFeed(context, feedid)) {
                    iter.remove();
                }
            }
        }
    }

    public long getLastDownloadTime() {
        return getLastDownloadTime(getContext(), 0);
    }

    public static long getLastDownloadTime(Context context, long defaultValue) {
        return Pref.getLong(context, getLastDownloadTimePreferenceKey(), defaultValue);
    }

    public static void setLastDownloadTime(Context context, long time) {
        Pref.setLong(context, getLastDownloadTimePreferenceKey(), time);
    }

    private static String getLastDownloadTimePreferenceKey() {
        return FeedDownloadManager.class.getName() + ".lastDownloadTime.long";
    }

    public static JSONObject getFeed(Context ctx, Long feedid) {
        try {
            List<JSONObject> download = getDownload(ctx);
            if (download == null || download.isEmpty()) {
                return null;
            }
            for (JSONObject feed : download) {
                boolean found;
                if (((Long) feed.get(FeedhitNames.feedid)).longValue() == feedid.longValue()) {
                    found = true;
                } else {
                    found = false;
                }
                if (found) {
                    return feed;
                }
            }
            return null;
        } catch (Exception e) {
            Logx.log(FeedDownloadManager.class, e);
            return null;
        }
    }

    private static void setDownload(Context ctx, List<JSONObject> download) {
        synchronized (downloadLock) {
            getDownloadIO(ctx).setTarget(download);
        }
    }

    /**
     * @param ctx
     * @return List; a copy of the last downloaded feeds
     */
    public static List<JSONObject> getDownload(Context ctx) {
        List output;
        synchronized (downloadLock) {
            List<JSONObject> download = (List) getDownloadIO(ctx).getTarget();
//////////////////// NOTE THIS /////////////////////////
            removeDeleted(ctx, new Feed(), download);
            output = (download == null || download.isEmpty()) ? new ArrayList(getMaxCacheSize(ctx)) : new ArrayList(download);
        }
        return output;
    }

    public static List<Map<String, Object>> getCommentNotifications(Context ctx, boolean create) {
        JsonListIO<Map<String, Object>> io = getCommentNotificationsIO(ctx, create);
        if (io == null) {
            return null;
        }
        return (List) io.getTarget();
    }

    public static JsonListIO<JSONObject> getDownloadIO(Context ctx) {
        try {
            if (_accessViaGetter == null) {
                _accessViaGetter = new JsonListIO(ctx, FileIO.getFeedsFilename());
                _accessViaGetter.setListSize(getMaxCacheSize(ctx));
            }
        } catch (Exception e) {
            Logx.log(FeedDownloadManager.class, e);
        }
        return _accessViaGetter;
    }

    public static boolean clearCommentNotifications(Context ctx) {
        JsonListIO<Map<String, Object>> io = getCommentNotificationsIO(ctx, false);
        List<Map<String, Object>> target = io == null ? null : (List) io.getTarget();
        if (target == null || target.isEmpty()) {
            return false;
        }
        io.setTarget(null);
        return true;
    }

    public static JsonListIO<Map<String, Object>> getCommentNotificationsIO(Context ctx, boolean create) {
        try {
            if (_cn == null && create) {
                _cn = new JsonListIO(ctx, FileIO.getCommentNotificationsFilename());
                _cn.setListSize(20);
            }
        } catch (Exception e) {
            Logx.log(FeedDownloadManager.class, e);
        }
        return _cn;
    }

    private Map getDefaultSources() {
        if (this._dsc == null) {
            this._dsc = App.getPropertiesManager(getContext()).getMap(PropertyName.sources);
        }
        return this._dsc;
    }

    public static int getMaxCacheSize(Context context) {
        return App.getPropertiesManager(context).getInt(PropertyName.feedDownloadBufferSize);
    }

    public static int getUpdateCount() {
        return updateCount;
    }

    public static int getRemovedCount() {
        return removedCount;
    }

    public static boolean addDeletedFeedId(Context ctx, Long feedid) {
        CloseableSet<Long> list = getDeletedFeedids(ctx);
        try {
            boolean add = list.add(feedid);
            return add;
        } finally {
            list.close();
        }
    }

    public static boolean isDeletedFeed(Context ctx, Long feedid) {
        CloseableSet<Long> list = getDeletedFeedids(ctx);
        return list == null ? false : list.contains(feedid);
    }

    public static CloseableSet<Long> getDeletedFeedids(Context context) {
        if (_iam_accessViaGetter == null) {
            _iam_accessViaGetter = new CachedSet(context, FileIO.getDeletedFeedidsFilename());
        }
        return _iam_accessViaGetter;
    }
}

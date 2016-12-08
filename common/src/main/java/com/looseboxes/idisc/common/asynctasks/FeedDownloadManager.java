package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bc.android.core.io.JsonListIO;
import com.bc.android.core.util.IntArrayLimitedSize;
import com.bc.android.core.util.IntArrayLimitedSizeWithLocalCache;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FeedFilterImpl;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedComparator;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.CommentNotificationHandler;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.search.FeedSearcherNotification;
import com.looseboxes.idisc.common.search.FeedSearcherSearchResult;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FeedDownloadManager extends FeedDownloadTask {

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final boolean sortOnlyNewlyDownloaded = false;

    private static final int feedsToPromotePerPreference = 5;
    private static final int feedsToPromoteMin = 10;
    private static final int feedsToPromoteMax = 100;

    private static final long feedsToPromoteMaxAge = TimeUnit.HOURS.toMillis(12);

    private static transient JsonListIO<JSONObject> _accessViaGetter;

    private static transient JsonListIO<Map<String, Object>> _cn;

    private static List<JSONObject> newlyDownloadedFeeds;

    private Map _dsc;

    private static OnNewFeedsListener onNewFeedsListener;

    public interface OnNewFeedsListener {

        void onNewFeeds(List<JSONObject> download);
    }

    class HandleDownloadedAsync extends AsyncTask {

        final List<JSONObject> mDownloaded;

        HandleDownloadedAsync(List<JSONObject> downloaded) {
            this.mDownloaded = downloaded;
        }

        protected Object doInBackground(Object[] params) {

            long tb4 = System.currentTimeMillis();

            try {

                final int priority = User.getInstance().isAdmin(FeedDownloadManager.this.getContext()) ? Log.INFO : Log.DEBUG;
                Logx.getInstance().log(priority, getClass(), "Downloaded: {0} news feeds", mDownloaded == null ? null : mDownloaded.size());
                FeedDownloadManager.this.logLatest(getClass(), "downloaded news feeds", mDownloaded);
                FeedDownloadManager.this.logCategories("Before update", getClass(), mDownloaded, "statuses");

                final Context context = getContext();

                setLastDownloadTime(context, System.currentTimeMillis());

                final List<Map<String, Object>> commentNotifications = updateCommentNotifications(context, mDownloaded);

                final Feed feedView = new Feed();

                final List<Long> hotnews_feedids = getHotnewsFeedids(mDownloaded);

                updateHotNews(feedView, hotnews_feedids, mDownloaded);
                updateHotNews(feedView, hotnews_feedids, getCachedFeedsNotCopy(context));

                newlyDownloadedFeeds = updateFeeds(context, feedView, mDownloaded);

                if(onNewFeedsListener != null) {
                    try {
                        onNewFeedsListener.onNewFeeds(newlyDownloadedFeeds);
                    }catch(Exception e) {
                        Logx.getInstance().log(this.getClass(), e);
                    }
                }

                CommentNotificationHandler commentNotificationHandler = new CommentNotificationHandler(context);

                commentNotificationHandler.showCommentNotice(commentNotifications);

                final boolean all = true;

                final boolean aggressiveNotifications = true;

                FeedDownloadManager.this.showFeedNotice(all, aggressiveNotifications);

            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
            }finally{
                if(Logx.getInstance().isLoggable(Log.DEBUG))
                Logx.getInstance().log(Log.DEBUG, getClass(), "Time spent updating downloads: {0}", Long.valueOf(System.currentTimeMillis() - tb4));
            }
            return null;
        }
    }

    public FeedDownloadManager(Context context) {
//        this(context, getLastDownloadTime(context, 0));
        this(context, getAfter(context, 0));
    }

    static long getAfter(Context context, long defaultValue) {
        Date latest = new Feed().getLatestDate(getDownload(context), null);
        Logx.getInstance().debug(FeedDownloadManager.class, "Will download feeds after: {0}", latest);
        return latest == null ? defaultValue : latest.getTime();
    }

    public FeedDownloadManager(Context context, long lastDownloadTime) {
        super(context, lastDownloadTime);
    }

    public static void setOnNewFeedsListener(OnNewFeedsListener onNewFeedsListener) {
        FeedDownloadManager.onNewFeedsListener = onNewFeedsListener;
    }

    public void showFeedNotice(boolean all, boolean aggressive) {

        final List<JSONObject> forNotification = all ?
                FeedDownloadManager.getCachedFeedsNotCopy(this.getContext()) :
                newlyDownloadedFeeds;

        this.showFeedNotice(new Feed(), forNotification, aggressive);
    }

    public void showFeedNotice(Feed feedView, List<JSONObject> feeds, boolean aggressive) {

        if (feeds == null || feeds.isEmpty()) {
            return;
        }

        final Context context = this.getContext();

        FeedNotificationHandler notificationHandler = new FeedNotificationHandler(context);

        if (!notificationHandler.isCompatibleAndroidVersion()) {
            return;
        }

        notificationHandler.removeOrphanActiveNotifications();

        notificationHandler.refreshActiveNotifications();

        if(notificationHandler.isActiveNotificationsWithinLimit()) {

            FeedSearcherNotification searcher = new FeedSearcherNotification(context);

            FeedSearcherSearchResult.SearchResult<JSONObject> outputIfNone = null;

            FeedSearcherSearchResult.SearchResult<JSONObject> toDisplay =
                    searcher.searchForNotification(feedView, feeds, aggressive, outputIfNone);

            if (toDisplay != outputIfNone) {

                Pref.setLong(this.getContext(), FileIO.getLastFeedNotificationTimeFilename(), System.currentTimeMillis());

                notificationHandler.showFeedNotice(feedView, toDisplay);
            }
        }
    }

    @Override
    public AsyncTask<String, ProgressStatus, String> execute() {
        String target = getTarget();
        final int priority = User.getInstance().isAdmin(this.getContext()) ? Log.INFO : Log.DEBUG;
        Logx.getInstance().log(priority, getClass(), "Executing: {0}\nwith: {1}", target, this.getOutputParameters());
        return execute(new String[]{target});
    }
    /**
     * @param downloaded The downloaded feeds
     */
    public void onSuccess(List<JSONObject> downloaded) {

        new HandleDownloadedAsync(downloaded).execute();
    }

    private List<Map<String, Object>> updateCommentNotifications(Context context, List<JSONObject> downloaded) {
        if (downloaded == null || downloaded.isEmpty()) {
            return null;
        }
        Iterator<JSONObject> iter = downloaded.iterator();
        while (iter.hasNext()) {
            JSONObject next = iter.next();
            List<Map<String, Object>> notices = (List)next.get("notices");
            if (notices != null) {
                iter.remove();
                if (!notices.isEmpty()) {
                    getCommentNotificationsIO(context, true).setTarget(notices);
                    return notices;
                }
                break;
            }
        }
        return getCommentNotifications(context, false);
    }

    private List<Long> getHotnewsFeedids(List<JSONObject> downloaded) {

        List<Long> hotnews_feedids = null;

        if (downloaded != null && !downloaded.isEmpty()) {
            Iterator<JSONObject> iter = downloaded.iterator();
            final String hotnewsCategoryName = PropertiesManager.PropertyName.hotnewsCategoryName.name();
            while (iter.hasNext()) {
                JSONObject next = iter.next();
                hotnews_feedids = (List)next.get(hotnewsCategoryName);
                if (hotnews_feedids != null) {
                    iter.remove();
                    break;
                }
            }
        }

        Logx.getInstance().debug(this.getClass(), "Hot news feedids: {0}", hotnews_feedids);

        return hotnews_feedids == null ? Collections.EMPTY_LIST : hotnews_feedids;
    }

    private void updateHotNews(Feed feedView, List<Long> hotnews_feedids, List<JSONObject> toUpdate) {

        if (hotnews_feedids == null || hotnews_feedids.isEmpty() || toUpdate == null || toUpdate.isEmpty()) {
            return;
        }

        final String hotnewsCategoryName = PropertiesManager.PropertyName.hotnewsCategoryName.name();

        for (JSONObject json : toUpdate) {

            feedView.setJsonData(json);

            if(hotnews_feedids.contains(feedView.getFeedid())) {

                String categories = feedView.getCategories();

                if(categories == null) {
                    json.put(FeedNames.categories, hotnewsCategoryName);
                }else{
                    if(!categories.contains(hotnewsCategoryName)) {
                        categories = hotnewsCategoryName + ',' + ' ' + categories;
                        json.put(FeedNames.categories, categories);
                    }
                }
            }
        }
    }

    private List<JSONObject> updateFeeds(Context context, Feed feedView, List<JSONObject> downloaded) {
        try {

            lock.writeLock().lock();

            final int maxCacheSize = getMaxCacheSize(context);

            List<JSONObject> cache = getCachedFeedsNotCopy(context);

            if(cache == null) {

                cache = new ArrayList<>(maxCacheSize);
            }

            this.prepareDownloaded(context, feedView, downloaded);

            final Date earliestTimeCached = feedView.getEarliestDate(cache, null);
            final long time = earliestTimeCached==null?-1L:earliestTimeCached.getTime();
            final Set<String> preferredCategories = Pref.getPreferredCategories(context);
            final Set<String> preferredSources = Pref.getPreferredSources(context);
            final Comparator<JSONObject> feedComparator = Collections.reverseOrder(
                    new FeedComparator(context, preferredCategories, preferredSources, time, false)
            );

            Collections.sort(downloaded, feedComparator);

            truncate(cache, downloaded, maxCacheSize);

            final int priority = User.getInstance().isAdmin(this.getContext()) ? Log.INFO : Log.DEBUG;
            Logx.getInstance().log(priority, getClass(), "Adding: {0} downloaded feeds to cache",
                    downloaded == null ? null : downloaded.size());

            if(downloaded != null && !downloaded.isEmpty()) {

                cache.addAll(0, downloaded);
            }

            if(!sortOnlyNewlyDownloaded) {

                Collections.sort(cache, feedComparator);
            }

            final int maxFeedsToPromote = this.getNumberOfPreferrenceFeedsToPromote(
                    preferredCategories, preferredSources);

            if(maxFeedsToPromote > 0 && maxFeedsToPromote < cache.size()) {

                promoteThenDemote(context, feedView, cache, maxFeedsToPromote);
            }

            setDownload(context, cache);

            this.updateAvailableCategories(context, cache);

            this.updateAvailableSources(context, cache);

            this.logLatest(this.getClass(), "cache", cache);

//Logx.getInstance().debug(null, this.getClass(), "Exiting method #updateFeeds(android.content.Context, java.util.List)");

            return downloaded;

        }finally{

            lock.writeLock().unlock();
        }
    }

    private void truncate(List<JSONObject> cachedFeeds, List<JSONObject> downloadedFeeds, int maxCacheSize) {

        Util.requireNonNull(cachedFeeds);

        if(cachedFeeds.size() + downloadedFeeds.size() >= maxCacheSize) {

            final float loadFactor = 0.8f;

            int start = (int)(maxCacheSize * loadFactor);

            int end = cachedFeeds.size();

            if(start < end) {

                final int sizeB4 = cachedFeeds.size();

                int excessRemoved = 0;

                for(int i=start; i<end; i++) {

                    // We have to keep removing the last element
                    cachedFeeds.remove(cachedFeeds.size()-1);

                    ++excessRemoved;
                }

                final int priority = User.getInstance().isAdmin(this.getContext()) ? Log.INFO : Log.DEBUG;

                Logx.getInstance().log(priority, FeedDownloadManager.class, "Removed {0} of {1} feeds from cache.",
                        excessRemoved, sizeB4);
            }
        }
    }

    private void promoteThenDemote(Context context, Feed feedView, List<JSONObject> feeds, int limit) {
        try {

            lock.writeLock().lock();

            final List<JSONObject> addToHead = new LinkedList<>();
            final List<JSONObject> addToBottom = new LinkedList<>();

            final long minAge = System.currentTimeMillis() - feedsToPromoteMaxAge;

            final FeedFilter feedFilter = new FeedFilterImpl(context, minAge, false);

            final Iterator<JSONObject> iter = feeds.iterator();

            final String advertCategoryName = App.getPropertiesManager(context).getString(PropertyName.advertCategoryName);

            while(iter.hasNext()) {

                JSONObject json = iter.next();

                feedView.setJsonData(json);

                final String categories = (String)json.get(FeedNames.categories);
                if(categories != null && categories.contains(advertCategoryName)) {

                    iter.remove();

                    addToBottom.add(json);

                }else
                if(feedFilter.accept(feedView)) {

                    iter.remove();

                    addToHead.add(json);
                }

                if(addToHead.size() >= limit) {
                    break;
                }
            }

            feeds.addAll(0, addToHead);

            feeds.addAll(addToBottom);

        }finally{

            lock.writeLock().unlock();
        }
    }

    private int getNumberOfPreferrenceFeedsToPromote(Collection<String> preferredCategories, Collection<String> preferredSources) {
//        Collection<String> preferredCategories = Pref.getPreferredCategories(this.getContext());
//        Collection<String> preferredSources = Pref.getPreferredSources(this.getContext());
        final int userPreferencesTotalSize = (preferredCategories == null ? 0 : preferredCategories.size()) +
                (preferredSources == null ? 0 : preferredSources.size());
        if(userPreferencesTotalSize > 0) {
            int feedsToPromote = userPreferencesTotalSize * feedsToPromotePerPreference;
            if(feedsToPromote < feedsToPromoteMin) {
                feedsToPromote = feedsToPromoteMin;
            }else if(feedsToPromote > feedsToPromoteMax) {
                feedsToPromote = feedsToPromoteMax;
            }
            return feedsToPromote;
        }else{
            return 0;
        }
    }

    private void prepareDownloaded(Context context, Feed feedView, List<JSONObject> downloaded) {
        try {

            lock.writeLock().lock();

            final int emptyRemoved = feedView.removeEmptyFeeds(downloaded);

            final int existingRemoved = removeAlreadyExisting(context, feedView, downloaded);

            final int readRemoved = removeRead(context, feedView, downloaded);

            final int deletedRemoved = removeUserDeleted(context, feedView, downloaded);

            Logx.getInstance().log(Log.DEBUG, this.getClass(),
                    "Removed {0} empty, {1} existing, {2} read and {3} already deleted feeds",
                    emptyRemoved, existingRemoved, readRemoved, deletedRemoved);

        }finally{
            lock.writeLock().unlock();
        }
    }

    private int removeAlreadyExisting(Context context, Feed feedView, List<JSONObject> newlyDownloaded) {

        int removed = 0;

        try {

            lock.readLock().lock();

            List<JSONObject> cachedFeeds = getCachedFeedsNotCopy(context);

            if (cachedFeeds != null && !cachedFeeds.isEmpty()) {

                Iterator<JSONObject> iter = newlyDownloaded.iterator();

                while (iter.hasNext()) {

                    JSONObject json = iter.next();

                    if (feedView.contains(context, cachedFeeds, json)) {

                        iter.remove();

                        ++removed;
                    }
                }
            }
        }finally{

            lock.readLock().unlock();
        }

        return removed;
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
                Logx.getInstance().log(getClass(), e);
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
                    String sourceName = feed.getSourceName();
                    if (sourceName != null) {
                        availSources.add(sourceName);
                    }
                }
                Pref.setAvailableSources(context, availSources);
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
            }
        }
    }

    public static int removeRead(Context context, Feed feedView, List<JSONObject> downloaded) {
        int removed = 0;
        if (downloaded != null && !downloaded.isEmpty()) {
            FeedhitManager feedhitManager = null;
            try {
                feedhitManager = new FeedhitManager(context);
                Iterator<JSONObject> iter = downloaded.iterator();
                while (iter.hasNext()) {
                    feedView.setJsonData(iter.next());
                    Long feedid = feedView.getFeedid();
                    if (feedid != null && feedhitManager.isRead(feedid)) {
                        iter.remove();
                        ++removed;
                    }
                }
            }finally {
                if(feedhitManager != null) {
                    feedhitManager.destroy();
                }
            }
        }
        return removed;
    }

    public static int removeUserDeleted(Context context, Feed feedView, List<JSONObject> downloaded) {
        int removed = 0;
        if (downloaded != null && !downloaded.isEmpty()) {
            Iterator<JSONObject> iter = downloaded.iterator();
            while (iter.hasNext()) {
                feedView.setJsonData(iter.next());
                Long feedid = feedView.getFeedid();
                if (feedid != null && isDeletedFeed(context, feedid)) {
                    iter.remove();
                    ++removed;
                }
            }
        }
        return removed;
    }

    public static boolean isNextDownloadDue(Context context) {
        try {
            lock.readLock().lock();
            final long lastDownloadTime = getLastDownloadTime(context, 0);
            final Collection cachedFeeds = getCachedFeedsNotCopy(context);
            return (lastDownloadTime <= 0 && (cachedFeeds == null || cachedFeeds.isEmpty())) ||
                    (lastDownloadTime > 0 && System.currentTimeMillis() - lastDownloadTime > Pref.getFeedDownloadIntervalMillis(context));
        }finally{
            lock.readLock().unlock();
        }
    }

    public static long getLastDownloadTime(Context context) {
        return getLastDownloadTime(context, 0);
    }

    public static long getLastDownloadTime(Context context, long defaultValue) {
        return Pref.getLong(context, getLastDownloadTimePreferenceKey(), defaultValue);
    }

    private static void setLastDownloadTime(Context context, long time) {
        Pref.setLong(context, getLastDownloadTimePreferenceKey(), time);
    }

    private static String getLastDownloadTimePreferenceKey() {
        return FeedDownloadManager.class.getName() + ".lastDownloadTime.long";
    }

    public static JSONObject getFeed(Context ctx, Long feedid) {
        try {

            lock.readLock().lock();

            List<JSONObject> cachedFeeds = getCachedFeedsNotCopy(ctx);

            if (cachedFeeds == null || cachedFeeds.isEmpty()) {

                return null;
            }

            for (JSONObject feed : cachedFeeds) {
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
            Logx.getInstance().log(FeedDownloadManager.class, e);
            return null;
        }finally{
            lock.readLock().unlock();
        }
    }

    private static void setDownload(Context ctx, List<JSONObject> download) {
        try {
            lock.writeLock().lock();
            getDownloadIO(ctx).setTarget(download);
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static List<JSONObject> getCachedFeedsNotCopy(Context ctx) {
        try {
            lock.readLock().lock();

            List<JSONObject> download = (List) getDownloadIO(ctx).getTarget();

            return download;

        }finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param ctx
     * @return List; a copy of the last downloaded feeds
     */
    public static List<JSONObject> getDownload(Context ctx) {
        try {
            lock.readLock().lock();

            List<JSONObject> download = (List) getDownloadIO(ctx).getTarget();

            return (download == null || download.isEmpty()) ?
                    new ArrayList(getMaxCacheSize(ctx)) :
                    new ArrayList(download);
        }finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param ctx
     * @param start
     * @param end
     * @return List; a copy of the last downloaded feeds
     */
    public static List<JSONObject> getDownload(Context ctx, int start, int end) {
        try{

            lock.readLock().lock();

            List<JSONObject> download = (List) getDownloadIO(ctx).getTarget();

            int size = end - start;

            if(size > download.size()) {
                size = download.size();
                end = start + size;
            }

            return (download == null || download.isEmpty()) ?
                    new ArrayList(size) :
                    new ArrayList(download.subList(start, end));
        }finally {
            lock.readLock().unlock();
        }
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
            Logx.getInstance().log(FeedDownloadManager.class, e);
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
            Logx.getInstance().log(FeedDownloadManager.class, e);
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
        return App.getMemoryLimitedInt(context, PropertyName.feedDownloadBufferSize, 50);
    }

    public static void addDeletedFeedId(Context ctx, Long feedid) {
        IntArrayLimitedSize arr = getDeletedFeedids(ctx);
        final int i = feedid.intValue();
        if(!arr.contains(i)) {
            arr.add(i);
            removeUserDeleted(ctx, new Feed(), getDownload(ctx));
        }
    }

    public static boolean isDeletedFeed(Context ctx, Long feedid) {
        IntArrayLimitedSize arr = getDeletedFeedids(ctx);
        return arr == null ? false : arr.contains(feedid.intValue());
    }

    private static IntArrayLimitedSize _$deletedFeedids;
    public static IntArrayLimitedSize getDeletedFeedids(Context context) {
        if (_$deletedFeedids == null) {
            final int size = App.getMemoryLimitedInt(context, PropertyName.deletedFeedidsBufferSize, 10);
            _$deletedFeedids = new IntArrayLimitedSizeWithLocalCache(
                    5, size, context, getDeletedFeedidsFilename(), true);
        }
        return _$deletedFeedids;
    }

    private static String getDeletedFeedidsFilename() {
        return "com.looseboxes.idisc.common.User.deletedFeedids.intarray";
    }

    public static void setNewlyDownloadedFeeds(List<JSONObject> newlyDownloadedFeeds) {
        FeedDownloadManager.newlyDownloadedFeeds = newlyDownloadedFeeds;
    }

    public static List<JSONObject> getNewlyDownloadedFeeds() {
        return newlyDownloadedFeeds == null ? Collections.EMPTY_LIST : newlyDownloadedFeeds;
    }

    private void logLatest(Class aClass, String key, List downloaded) {
        if (Logx.getInstance().isLoggable(3) && downloaded != null && !downloaded.isEmpty()) {
            Feed feedView = new Feed();
            Date newestDate = feedView.getLatestDate(downloaded, null);
            String timeElapsed = newestDate == null ? "null" : feedView.getTimeElapsed(newestDate);
            Logx.getInstance().debug(aClass, "Newest feed in {0} {1} is {2} old", Integer.valueOf(downloaded.size()), key, timeElapsed);
        }
    }

    private void logCategories(String key, Class aClass, List downloaded, String categories) {
        if (Logx.getInstance().isLoggable(2) && downloaded != null && !downloaded.isEmpty()) {
            Feed feedView = new Feed();
            int statusesCount = 0;
            Date latest = null;
            for (Object o : downloaded) {
                feedView.setJsonData((JSONObject)o);
                String s = feedView.getCategories();
                if (s != null && s.toLowerCase().contains(categories.toLowerCase())) {
                    statusesCount++;
                    Date date = feedView.getDate(FeedNames.feeddate, null);
                    if (date != null) {
                        if (latest == null) {
                            latest = date;
                        } else if (date.after(latest)) {
                            latest = date;
                        }
                    }
                }
            }
            String latestDisplay = null;
            if (latest != null) {
                latestDisplay = feedView.getTimeElapsed(latest);
            }
            Logx.getInstance().debug(aClass, key + ", found {0} in {1} feeds\nNewest is {2} old.\nCategories: {3}", Integer.valueOf(statusesCount), Integer.valueOf(downloaded.size()), latestDisplay, categories);
        }
    }
}

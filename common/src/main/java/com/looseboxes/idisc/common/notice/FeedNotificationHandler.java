package com.looseboxes.idisc.common.notice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification.Builder;
import android.app.Notification.Style;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import com.bc.android.core.io.image.PicassoImageManager;
import com.bc.android.core.io.image.PicassoImageManagerImpl;
import com.bc.android.core.util.IntArrayLimitedSize;
import com.bc.android.core.util.IntArrayLimitedSizeWithLocalCache;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.bc.util.IntegerArray;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.search.FeedSearchResult;
import com.looseboxes.idisc.common.search.FeedSearcherSearchResult.SearchResult;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.util.List;

public class FeedNotificationHandler extends NotificationHandlerImpl {

    public static final String EXTRA_JSON_STRING_NOTIFIED_FEED = FeedNotificationHandler.class.getPackage().getName() + ".NotifiedFeedJSON";

    private static final int MAX_CONCURRENT_NOTIFICATIONS = 3;

    private final boolean useSourceName = false;

    private final int textLengthLong;
    private final int textLengthShort;

    private IntArrayLimitedSize viewedNoticeFeedids;

    private IntArrayLimitedSize activeNoticeFeedids;

    class AsyncBitmapLoader implements PicassoImageManager.OnPostBitmapLoadListener {
        final Feed val$feed;
        final SearchResult<JSONObject> val$result;
        AsyncBitmapLoader(Feed feed, SearchResult<JSONObject> searchResult) {
            this.val$feed = feed;
            this.val$result = searchResult;
        }
        public void onPostLoad(Bitmap bitmap) {
            FeedNotificationHandler.this.fireFeedNotice(this.val$feed, this.val$result, bitmap);
        }
    }

    public FeedNotificationHandler(Context context) {
        super(context);
        PropertiesManager pm = App.getPropertiesManager(context);
        this.textLengthShort = pm.getInt(PropertyName.textLengthShort);
        this.textLengthLong = pm.getInt(PropertyName.textLengthLong);
        final int size = App.getMemoryLimitedInt(context, PropertyName.displayedFeedidsBufferSize, 10);
        this.viewedNoticeFeedids = new IntArrayLimitedSizeWithLocalCache(5, size, context, getDisplayedFeedidsFilename(), true);
        this.activeNoticeFeedids = new IntArrayLimitedSizeWithLocalCache(
                MAX_CONCURRENT_NOTIFICATIONS, MAX_CONCURRENT_NOTIFICATIONS, context, getActiveFeedidsFilename(), true);
    }

    public int refreshActiveNotifications() {
        synchronized (this.activeNoticeFeedids) {
            int refreshed = 0;
            if(!this.activeNoticeFeedids.isEmpty()) {
                final int [] arr = this.activeNoticeFeedids.toArray();
                List<JSONObject> cachedFeedData = FeedDownloadManager.getDownload(getContext());
                Feed feed = new Feed();
                for(int e : arr) {
                    for(JSONObject json : cachedFeedData) {
                        feed.setJsonData(json);
                        if(feed.getFeedid() != null && feed.getFeedid().intValue() == e) {
                            this.showFeedNotice(feed, new FeedSearchResult(feed, json));
                            ++refreshed;
                        }
                    }
                }
            }
            Logx.getInstance().debug(this.getClass(), "Refreshed {0} notifications", refreshed);
            return refreshed;
        }
    }

    public boolean showFeedNotice(Feed feed, SearchResult<JSONObject> result) {

        if (!isCompatibleAndroidVersion()) {
            return false;
        }

        final String imageUrl = feed.getImageUrl();
        final boolean hasNoImageUrl = imageUrl == null || imageUrl.isEmpty();
        if (hasNoImageUrl) {

            return fireFeedNotice(feed, result, null);
        }

        PicassoImageManager.OnPostBitmapLoadListener onPostBitmapLoadListener = new AsyncBitmapLoader(feed, result);

        Resources res = getContext().getResources();

        Bitmap image = getImage(feed, result, (int) NewsminuteUtil.dipToPixels(getContext(), res.getDimension(R.dimen.noticeImageLargeWidth)), (int) NewsminuteUtil.dipToPixels(getContext(), res.getDimension(R.dimen.noticeImageLargeHeight)), onPostBitmapLoadListener);

        if (image != null) {

            return fireFeedNotice(feed, result, image);
        }

        return false;
    }

    @TargetApi(16)
    public boolean fireFeedNotice(Feed feed, SearchResult<JSONObject> result, Bitmap image) {

        String summary;
        Style style;

        feed.setJsonData(result.getSearchedData());

        final String sourceName = feed.getSourceName();

        final String heading = feed.getHeading(null, this.textLengthShort);

        final String title = getTitle();

        if (useSourceName && sourceName != null) {
            summary = sourceName + " - " + heading;
        } else {
            summary = heading;
        }

        final String searchedText = result.getSearchedText();
        final String found = result.getMatchResult().group();
        final String target = searchedText.length() <= this.textLengthLong ? searchedText :
                NewsminuteUtil.truncateSides(searchedText, found, searchedText.length() - this.textLengthLong, true);

        String text = Util.isHtml(target) ? useSourceName && sourceName != null ? heading : null : target;

        if (text == null) {
            summary = heading;
        }

        Logx.getInstance().debug(getClass(), "Title: {0}, summary: {1}, bigText: {2}", title, summary, text);

        Builder builder = getBuilder(this.getNoticeiconResourceid(), image, title, summary);
        if (text != null) {
            style = getBigTextStyle(title, summary, text);
        } else if (image != null) {
            if (summary != null) {
                text = summary;
            }
            style = getBigPictureStyle(image, image, title, text);
        } else {
            style = null;
        }
        if (style != null && isCompatibleAndroidVersion()) {
            builder.setStyle(style);
        }

        boolean shown = fireDefaultNotification(builder, MainActivity.class, result);

        return shown;
    }

    public boolean fireDefaultNotification(
            Builder notificationBuilder,
            Class<? extends Activity> targetActivityClass, SearchResult<JSONObject> result) {

        final boolean vibrate = !Pref.isDisableVibration(this.getContext());

        final Long feedid = result.getSearchedDataId();

        final boolean shown = fireDefaultNotification(
                vibrate, feedid.intValue(), notificationBuilder, targetActivityClass,
                EXTRA_JSON_STRING_NOTIFIED_FEED, result.getSearchedData().toJSONString());

        Logx.getInstance().debug(this.getClass(), "Notified feed was shown: {0}, ID: {1}, title: {2}",
                shown, feedid, result.getSearchedData().get(FeedNames.title));

        if (shown) {

            this.addActivelyNotified(feedid);
        }
        return shown;
    }

    public Bitmap getImage(Feed feed, SearchResult<JSONObject> result, int width, int height, PicassoImageManager.OnPostBitmapLoadListener onPostBitmapLoadListener) {
        String imageUrl = feed.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        return loadBitmap(imageUrl, width, height, onPostBitmapLoadListener);
    }

    public Bitmap loadBitmap(String imageUrl, int width, int height, PicassoImageManager.OnPostBitmapLoadListener onPostBitmapLoadListener) throws IllegalStateException {
        final PicassoImageManager picassoImageManager = new PicassoImageManagerImpl(getContext());
        if (onPostBitmapLoadListener == null) {
            try {
                return picassoImageManager.loadBitmap(imageUrl, width, height);
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
                return null;
            }
        }
        picassoImageManager.loadBitmapAsync(imageUrl, width, height, onPostBitmapLoadListener);
        return null;
    }

    private boolean isSitenameContainedInImageUrl(Feed feed) {
        String imageUrl = format(feed.getImageUrl());
        return imageUrl == null ? false : imageUrl.contains(format(feed.getSourceName()));
    }

    public void addFeedViewedFromNotice(Long feedid) {
        final int i = feedid.intValue();
        if(!this.viewedNoticeFeedids.contains(i)) {
            this.viewedNoticeFeedids.add(i);
            Logx.getInstance().debug(this.getClass(),
                    "Added feedid: {0} to already notified feedids: {1}", feedid, this.viewedNoticeFeedids);
        }
    }

    public boolean isFeedViewedFromNotice(Long feedid) {
        return this.viewedNoticeFeedids.contains(feedid.intValue());
    }

    public IntArrayLimitedSize getViewedNoticeFeedids() {
        return this.viewedNoticeFeedids;
    }

    public boolean isActiveNotificationsWithinLimit() {
        final int n = this.activeNoticeFeedids.size();
        Logx.getInstance().debug(this.getClass(), "Active notices: {0}, Max concurrent: {1}", n, MAX_CONCURRENT_NOTIFICATIONS);
        return n < MAX_CONCURRENT_NOTIFICATIONS;
    }

    public void addActivelyNotified(Long feedid) {
        final int i = feedid.intValue();
        if(!this.activeNoticeFeedids.contains(i)) {
            this.activeNoticeFeedids.add(i);
            Logx.getInstance().debug(this.getClass(),
                    "Added feedid: {0} to actively notified feedids: {1}", feedid, this.activeNoticeFeedids);
        }
    }

    public IntegerArray getActiveNotifications(boolean orphan) {

        synchronized (this.activeNoticeFeedids) {

            final IntegerArray output;

            if(!this.activeNoticeFeedids.isEmpty()) {

                final int[] arr = this.activeNoticeFeedids.toArray();

                output = new IntegerArray(arr.length);

                List<JSONObject> cachedFeeds = FeedDownloadManager.getDownload(this.getContext());

                Feed feed = new Feed();

                for (int e : arr) {

                    JSONObject found = null;

                    for(JSONObject json : cachedFeeds) {

                        feed.setJsonData(json);

                        if(feed.getFeedid().intValue() == e) {

                            found = json;

                            break;
                        }
                    }

                    if(orphan && found == null) {
                        output.add(e);
                    }else if (!orphan && found != null){
                        output.add(e);
                    }
                }
            }else{

                output = new IntegerArray(0);
            }

            Logx.getInstance().debug(this.getClass(), "{0} active feedids: {1}",
                    orphan ? "Orphan" : "Non-orphan", output);

            return output;
        }
    }

    public boolean removeActiveNotification(Long feedid) {
        final int toRemove = feedid.intValue();
        synchronized (this.activeNoticeFeedids) {
            if (this.activeNoticeFeedids.contains(toRemove)) {
                final int[] arr = this.activeNoticeFeedids.toArray();
                final IntegerArray update = new IntegerArray(arr.length - 1);
                for (int e : arr) {
                    if (e != toRemove) {
                        update.add(e);
                    }
                }
                this.activeNoticeFeedids.clear();
                this.activeNoticeFeedids.addAll(update);
                Logx.getInstance().debug(this.getClass(),
                        "Removed feedid: {0} to actively notified feedids: {1}", feedid, this.activeNoticeFeedids);
                return true;
            } else {
                return false;
            }
        }
    }

    public int removeOrphanActiveNotifications() {
        int output = 0;
        IntegerArray orphans = this.getActiveNotifications(true);
        if(!orphans.isEmpty()) {
            Logx.getInstance().debug(this.getClass(), "BEFORE removing orphans, feedids of active notifications: {0}", this.activeNoticeFeedids);
            output = this.removeActiveNotifications(orphans);
            Logx.getInstance().debug(this.getClass(), " AFTER removing orphans, feedids of active notifications: {0}", this.activeNoticeFeedids);
        }
        return output;
    }

    public int removeActiveNotifications(IntegerArray toRemove) {
        return this.removeAll(this.activeNoticeFeedids, toRemove);
    }

    private int removeAll(IntegerArray src, IntegerArray toRemove) {
        if(src.isEmpty() || toRemove.isEmpty()) {
            return 0;
        }else {
            final int[] arr = src.toArray();
            final IntegerArray update = new IntegerArray(arr.length - toRemove.size());
            for (int e : arr) {
                if (!toRemove.contains(e)) {
                    update.add(e);
                }
            }
            final int removed = src.size() - update.size();
            src.clear();
            src.addAll(update);
            Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Removed {0} values", removed);
            return removed;
        }
    }

    public boolean isActivelyNotified(Long feedid) {
        return this.activeNoticeFeedids.contains(feedid.intValue());
    }

    public IntArrayLimitedSize getActiveNoticeFeedids() {
        return this.activeNoticeFeedids;
    }

    private String getDisplayedFeedidsFilename() {
        return "com.looseboxes.idisc.common.FeedLoadService.displayedFeeds.intarray";
    }

    private String getActiveFeedidsFilename() {
        return "com.looseboxes.idisc.common.FeedLoadService.activeFeeds.intarray";
    }

    @Override
    public void onViewed(int id) {

        super.onViewed(id);

        final Long feedid = Long.valueOf(id);

        this.removeActiveNotification(feedid);

        this.addFeedViewedFromNotice(feedid);
    }
}

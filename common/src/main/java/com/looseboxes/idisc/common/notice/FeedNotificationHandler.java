package com.looseboxes.idisc.common.notice;

import android.annotation.TargetApi;
import android.app.Notification.Builder;
import android.app.Notification.InboxStyle;
import android.app.Notification.Style;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.DisplayCommentActivity;
import com.looseboxes.idisc.common.activities.DisplayCommentsActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.io.image.ImageManager;
import com.looseboxes.idisc.common.io.image.PicassoImageManager;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedSearcher;
import com.looseboxes.idisc.common.jsonview.FeedSearcher.FeedSearchResult;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.util.CachedSet;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.Util;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FeedNotificationHandler extends NotificationHandler {
    public static final String EXTRA_JSON_STRING_NOTIFIED_FEED;
    private static CachedSet<Long> _iam_accessViaGetter;
    private String _al;
    private FeedSearcher _fs;
    private Date _ma;
    private int _siri;
    private ImageManager _t_accessViaGetter;
    private int textLengthLong;
    private int textLengthShort;

    /* renamed from: com.looseboxes.idisc.common.notice.FeedNotificationHandler.2 */

    class AsyncBitmapLoader implements ImageManager.OnPostBitmapLoadListener {
        final Feed val$feed;
        final FeedSearchResult val$result;
        AsyncBitmapLoader(Feed feed, FeedSearchResult feedSearchResult) {
            this.val$feed = feed;
            this.val$result = feedSearchResult;
        }
        public void onPostLoad(Bitmap bitmap) {
            FeedNotificationHandler.this.fireFeedNotice(this.val$feed, this.val$result, bitmap);
        }
    }

    static {
        EXTRA_JSON_STRING_NOTIFIED_FEED = FeedNotificationHandler.class.getPackage().getName() + ".NotifiedFeedJSON";
    }

    public FeedNotificationHandler(Context context) {
        super(context);
        PropertiesManager pm = App.getPropertiesManager(context);
        this.textLengthShort = pm.getInt(PropertyName.textLengthShort);
        this.textLengthLong = pm.getInt(PropertyName.textLengthLong);
    }

    public void showFeedNotice() {
        showFeedNotice(new Feed(), FeedDownloadManager.getDownload(getContext()));
    }

    public boolean showFeedNotice(List downloaded) {
        if (isCompatibleAndroidVersion()) {
            return showFeedNotice(new Feed(), downloaded);
        }
        return false;
    }

    @TargetApi(16)
    public boolean showCommentNotice(List<Map<String, Object>> notices) {
        if (!isCompatibleAndroidVersion()) {
            return false;
        }
        if (notices == null || notices.isEmpty()) {
            return false;
        }
        try {
            String summary;
            int noticesCount = notices.size();
            int smallIconResId = getSmallIconResourceId();
            String title = getTitle();
            if (noticesCount == 1) {
                summary = getSummary((Map) notices.get(0));
            } else {
                summary = getGroupSummary(notices);
            }
            Builder builder = getBuilder(smallIconResId, null, title, summary);
            if (noticesCount > 1) {
                InboxStyle inboxStyle = new InboxStyle();
                int i = 0;
                int repliesCount = 0;
                for (Map<String, Object> notice : notices) {
                    int n = getRepliesCount(notice);
                    inboxStyle.addLine(getSummary(repliesCount, getReplierScreenName(notice, 0)));
                    repliesCount += n;
                    i++;
                    if (i == 5) {
                        break;
                    }
                }
                inboxStyle.setSummaryText(getContext().getString(R.string.msg_d_repliestoyourcomment, new Object[]{Integer.valueOf(repliesCount)}));
                builder.setStyle(inboxStyle);
            }
            return showNotification(2, builder, noticesCount == 1 ? DisplayCommentActivity.class : DisplayCommentsActivity.class, null, null);
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return false;
        }
    }

    private String getSummary(Map<String, Object> notice) {
        return getSummary(getRepliesCount(notice), getReplierScreenName(notice, 0));
    }

    private String getSummary(int repliesCount, String replierScreenname) {
        if (repliesCount == 1) {
            return getContext().getString(R.string.msg_s_replied, new Object[]{replierScreenname});
        }
        return getContext().getString(R.string.msg_s_and_d_othersreplied, new Object[]{replierScreenname, Integer.valueOf(repliesCount - 1)});
    }

    private int getRepliesCount(Map<String, Object> notice) {
        return ((List) notice.get("replies")).size();
    }

    private String getGroupSummary(List<Map<String, Object>> notices) {
        int commentCount = notices.size();
        int totalReplies = 0;
        for (Map<String, Object> notice : notices) {
            totalReplies += getRepliesCount(notice);
        }
        return getContext().getString(R.string.msg_d_repliesto_d_ofyourcomments, new Object[]{Integer.valueOf(totalReplies), Integer.valueOf(commentCount)});
    }

    private String getReplierScreenName(Map<String, Object> notice, int index) {
        return (String) ((Map) ((Map) ((List) notice.get("replies")).get(index)).get(InstallationNames.installationid)).get(InstallationNames.screenname);
    }

    public boolean showFeedNotice(Feed feedView, List downloaded) {
        if (!isCompatibleAndroidVersion() || downloaded == null || downloaded.isEmpty()) {
            return false;
        }
        FeedSearchResult toDisplay = getResultForDisplay(feedView, downloaded, this.textLengthLong);
        if (toDisplay == null) {
            Date maxage = getMaxAge();
            if (maxage != null) {
                toDisplay = getMostViewedAfter(feedView, downloaded, this.textLengthLong, maxage);
            }
        }
        if (toDisplay != null) {
            return showFeedNotice(feedView, toDisplay);
        }
        return false;
    }

    public boolean showFeedNotice(Feed feed, FeedSearchResult result) {
        if (!isCompatibleAndroidVersion()) {
            return false;
        }
        boolean hasNoImageUrl;
        String imageUrl = feed.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            hasNoImageUrl = true;
        } else {
            hasNoImageUrl = false;
        }
        if (hasNoImageUrl) {
            return fireFeedNotice(feed, result, null);
        }
        ImageManager.OnPostBitmapLoadListener onPostBitmapLoadListener = new AsyncBitmapLoader(feed, result);
        Resources res = getContext().getResources();
        Bitmap image = getImage(feed, result, (int) Util.dipToPixels(getContext(), res.getDimension(R.dimen.noticeImageLargeWidth)), (int) Util.dipToPixels(getContext(), res.getDimension(R.dimen.noticeImageLargeHeight)), onPostBitmapLoadListener);
        if (image != null) {
            return fireFeedNotice(feed, result, image);
        }
        return false;
    }

    @TargetApi(16)
    public boolean fireFeedNotice(Feed feed, FeedSearchResult result, Bitmap image) {
        String summary;
        Style style;
        feed.setJsonData(result.getSearchedFeed());
        String sourceName = feed.getSourceName(getContext());
        String heading = feed.getHeading(null, this.textLengthShort);
        String title = getTitle();
        if (sourceName != null) {
            summary = sourceName + " - " + heading;
        } else {
            summary = heading;
        }
        String text = result.getText().contains("<") ? sourceName != null ? heading : null : result.getText();
        if (text == null) {
            summary = heading;
        }
        Logx.debug(getClass(), "Title: {0}, summary: {1}, bigText: {2}", title, summary, text);
        Builder builder = getBuilder(getSmallIconResourceId(), image, title, summary);
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
        boolean shown = fireDefaultNotification(builder, MainActivity.class, EXTRA_JSON_STRING_NOTIFIED_FEED, result.getSearchedFeed().toJSONString());
        setLastNotifiedFeedId(getContext(), result.getFeedId().longValue());
        return shown;
    }

    public Bitmap getImage(Feed feed, FeedSearchResult result, int width, int height, ImageManager.OnPostBitmapLoadListener onPostBitmapLoadListener) {
        String imageUrl = feed.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        return loadBitmap(imageUrl, width, height, onPostBitmapLoadListener);
    }

    public Bitmap loadBitmap(String imageUrl, int width, int height, ImageManager.OnPostBitmapLoadListener onPostBitmapLoadListener) throws IllegalStateException {
        ImageManager im = getImageManager();
        if (onPostBitmapLoadListener == null) {
            try {
                return im.loadBitmap(imageUrl, width, height);
            } catch (Exception e) {
                Logx.log(getClass(), e);
                return null;
            }
        }
        im.loadBitmapAsync(imageUrl, width, height, onPostBitmapLoadListener);
        return null;
    }

    private ImageManager getImageManager() {
        if (this._t_accessViaGetter == null) {
            this._t_accessViaGetter = new PicassoImageManager(getContext());
        }
        return this._t_accessViaGetter;
    }

    protected FeedSearchResult getResultForDisplay(Feed feed, List download, int displayLen) {
        download = feed.getFeedsAfter(acceptNonDisplayed(feed, download), getMaxAge());
        if (download == null || download.isEmpty()) {
            return null;
        }
        FeedSearcher searcher = new FeedSearcher(getContext());
        Map<String, FeedSearchResult[]> found = searcher.searchIn(getContext(), download, displayLen);
        if (found == null || found.isEmpty()) {
            return null;
        }
        FeedSearchResult[] mostcommon = searcher.getMostCommon(found);
        if (mostcommon == null || mostcommon.length <= 0) {
            return null;
        }
        return mostcommon[0];
    }

    public FeedSearchResult getMostViewedAfter(Feed feed, List feeds, int maxLen, Date date) {
        feeds = feed.getFeedsAfter(feeds, date);
        if (feeds == null || feeds.isEmpty()) {
            return null;
        }
        return getMostViewed(feed, feeds, maxLen);
    }

    public FeedSearchResult getMostViewed(Feed feed, List feeds, int maxLen) {
        return toSearchResult(feed, feed.getMostViewed(getContext(), feeds, false), maxLen);
    }

    public FeedSearchResult getMostRecent(Feed feed, List feeds, int maxLen) {
        return toSearchResult(feed, (JSONObject) feeds.get(feed.getLatestIndex(feeds)), maxLen);
    }

    private FeedSearchResult toSearchResult(Feed feed, JSONObject json, int maxLen) {
        if (json == null) {
            return null;
        }
        feed.setJsonData(json);
        return getFeedSearcher().getSearchResult(feed.getFeedid(), feed.getHeading(null, maxLen), json);
    }

    private Date getMaxAge() {
        if (this._ma == null) {
            try {
                this._ma = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(App.getPropertiesManager(getContext()).getLong(PropertyName.notificationMaxFeedAgeHours)));
            } catch (Exception e) {
                this._ma = null;
                Logx.log(getClass(), e);
            }
        }
        return this._ma;
    }

    private boolean isSitenameContainedInImageUrl(Feed feed) {
        String imageUrl = format(feed.getImageUrl());
        return imageUrl == null ? false : imageUrl.contains(format(feed.getSourceName(getContext())));
    }

    private String format(String s) {
        return s == null ? null : s.replaceAll("\\s|\\p{Punct}", "").toLowerCase();
    }

    private FeedSearcher getFeedSearcher() {
        if (this._fs == null) {
            this._fs = new FeedSearcher(getContext());
        }
        return this._fs;
    }

    public String getTitle() {
        return getContext().getString(R.string.msg_defaultnoticetext, new Object[]{getAppLabel()});
    }

    private String getAppLabel() {
        if (this._al == null) {
            this._al = getContext().getString(R.string.app_label);
        }
        return this._al;
    }

    private int getSmallIconResourceId() {
        if (this._siri < 1) {
            this._siri = App.isAcceptableVersion(getContext(), 21) ? R.drawable.notification_icon : R.mipmap.ic_launcher;
        }
        return this._siri;
    }

    private List acceptNonDisplayed(Feed feed, List download) {
        List nonDisplayed = new ArrayList(download.size());
        for (Object o : download) {
            feed.setJsonData((JSONObject) o);
            if (!isFeedViewedFromNotice(getContext(), feed.getFeedid())) {
                nonDisplayed.add(o);
            }
        }
        return nonDisplayed;
    }

    public static boolean addFeedViewedFromNotice(Context ctx, Long feedid) {
        CachedSet<Long> list = getNoticeFeedids(ctx);
        try {
            boolean add = list.add(feedid);
            return add;
        } finally {
            list.close();
        }
    }

    public static boolean isFeedViewedFromNotice(Context ctx, Long feedid) {
        CachedSet<Long> list = getNoticeFeedids(ctx);
        return list == null ? false : list.contains(feedid);
    }

    public static CachedSet<Long> getNoticeFeedids(Context context) {
        if (_iam_accessViaGetter == null) {
            _iam_accessViaGetter = new CachedSet(context, FileIO.getDisplayedFeedidsFilename());
        }
        return _iam_accessViaGetter;
    }

    public static void onFeedViewed(Context context, Long feedid) {
        addFeedViewedFromNotice(context, feedid);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
        setLastNotifiedFeedId(context, -1);
    }

    public static void onCommentViewed(Context context, Long commentid) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(2);
    }

    public static void setLastNotifiedFeedId(Context context, long feedid) {
        Pref.setLong(context, getLastNotifiedFeedidPreferenceKey(), feedid);
    }

    public static long getLastNotifiedFeedId(Context context) {
        return Pref.getLong(context, getLastNotifiedFeedidPreferenceKey(), -1);
    }

    private static String getLastNotifiedFeedidPreferenceKey() {
        return FeedNotificationHandler.class.getName() + "LastNotifiedFeedId";
    }
}

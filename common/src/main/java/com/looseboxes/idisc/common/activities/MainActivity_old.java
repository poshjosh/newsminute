package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.RemoteLog;
import com.looseboxes.idisc.common.asynctasks.DefaultReadTask;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.io.StorageIOException;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedSearcher;
import com.looseboxes.idisc.common.jsonview.FeedSearcher.FeedSearchResult;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.jsonview.JsonView;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.service.ServiceScheduler;
import com.looseboxes.idisc.common.util.AppRater;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.Util;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MainActivity_old extends DownloadedFeedListActivity {
    public static final String EXTRA_BOOLEAN_CLEAR_PREVIOUS;
    private static long lastFeedDownloadIntervalMillis;
    private static boolean startedFeedLoadService;

    private class DownloadManager extends FeedDownloadManager {
        private DownloadManager(Context context) {
            super(context);
        }

        public void onPostSuccess(List download) {
            if (download == null || download.isEmpty()) {
                onNoResult();
            } else {
                MainActivity_old.this.getFeedDisplayHandler().displayFeeds(download);
            }
        }

        public void processError() {
            super.processError();
            onNoResult();
        }

        public void onCancelled(String result) {
            Logx.log(5, getClass(), isCompleted() ? "Process cancelled after completeion" : getErrorMessage() + "\nProcess Cancelled");
            onNoResult();
        }

        public void displayMessage(Object message, int length) {
            if (!isNoUI()) {
                Popup.show(MainActivity_old.this, message, length);
            }
        }

        private void onNoResult() {
            List lastDownload = FeedDownloadManager.getDownload(MainActivity_old.this);
            if (lastDownload != null && !lastDownload.isEmpty()) {
                MainActivity_old.this.getFeedDisplayHandler().displayFeeds(lastDownload);
            } else if (Util.isNetworkConnectedOrConnecting(getContext())) {
                MainActivity_old.this.sendServiceUnavailable();
            } else {
                Popup.show(getContext(), getContext().getString(R.string.err_noconnection), 0);
            }
        }
    }

    static {
        EXTRA_BOOLEAN_CLEAR_PREVIOUS = MainActivity.class.getName() + ".clearPrevious";
    }

    protected void onResume() {
        log();
        super.onResume();
    }

    public long getLastDownloadTime() {
        return FeedDownloadManager.getLastDownloadTime(this, 0);
    }

    public boolean isClearPrevious() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra(EXTRA_BOOLEAN_CLEAR_PREVIOUS, false);
    }

    public DefaultReadTask createDownloadTask() {
        return new DownloadManager(this);
    }

    public int getTabId() {
        return R.id.tabs_all;
    }

    public int getContentView() {
        return R.layout.main;
    }

    public Collection preDisplayFeeds(Collection feeds) {
        Map<String, FeedSearchResult[]> results = new FeedSearcher((Context) this).searchIn(this, feeds);
        if (!(results == null || results.isEmpty())) {
            getFeedListAdapter().setSearchresults(results);
        }
        return feeds;
    }

    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        try {
            boolean installed = App.isInstalled(this);
            App.init(getApplicationContext());
            if (installed) {
                new AppRater(getApplication().getPackageName()).onAppLaunch(this);
            } else {
                Logx.log(Log.DEBUG, getClass(), "Before welcome screen");
                startActivity(new Intent(this, WelcomeActivity.class));
                Logx.log(Log.DEBUG, getClass(), "Continuing onCreate after displaying welcome screen");
            }
            if (!startedFeedLoadService || isFeedDownloadIntervalChanged()) {
                startedFeedLoadService = true;
                lastFeedDownloadIntervalMillis = Pref.getFeedDownloadIntervalMillis(this);
                ServiceScheduler.scheduleNetworkService(this);
            }
        } catch (StorageIOException e) {
            App.handleInstallationError((Context) this, e, R.string.msg_storageioerror_advice);
        } catch (IOException e2) {
            App.handleInstallationError((Context) this, e2, R.string.msg_restartapp);
        }
    }

    protected void onPostResume() {
        super.onPostResume();
        JSONObject feed = getNotifiedFeed();
        if (feed != null) {
            int pos = getPosition(feed);
            if (pos != -1) {
                scrollTo(pos);
            }
        }
    }

    private void scrollTo(int position) {
        int top;
        ListView listView = getListFragment().getListView();
        View v = listView.getChildAt(0);
        if (v == null) {
            top = 0;
        } else {
            top = v.getTop() - listView.getPaddingTop();
        }
        if (position != -1 && top != -1) {
            listView.setSelectionFromTop(position, top);
        }
    }

    private int getPosition(JSONObject feed) {
        ListAdapter adapter = getListFragment().getListAdapter();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            if (((JSONObject) adapter.getItem(i)).get(FeedhitNames.feedid).equals(feed.get(FeedhitNames.feedid))) {
                return i;
            }
        }
        return -1;
    }

    protected void onNoResult(List toDisplay) {
        List defaultFeeds = JsonView.getDummyFeeds(this);
        if (!(defaultFeeds == null || defaultFeeds.isEmpty())) {
            getFeedDisplayHandler().displayFeeds(defaultFeeds);
        }
        super.onNoResult(toDisplay);
    }

    public boolean isFeedDownloadIntervalChanged() {
        return Pref.getFeedDownloadIntervalMillis(this) != lastFeedDownloadIntervalMillis;
    }

    private JSONObject getNotifiedFeed() {
        JSONObject output;
        boolean noticeFeed = false;
        Intent intent = getIntent();
        if (intent == null) {
            output = null;
        } else {
            String extra = intent.getStringExtra(FeedNotificationHandler.EXTRA_JSON_STRING_NOTIFIED_FEED);
            if (extra != null) {
                noticeFeed = true;
            }
            if (extra == null || extra.isEmpty()) {
                output = null;
            } else {
                try {
                    output = (JSONObject) new JSONParser().parse(extra);
                } catch (Exception e) {
                    output = null;
                    Logx.log(getClass(), e);
                }
            }
        }
        if (output != null && noticeFeed) {
            FeedNotificationHandler.onFeedViewed(this, (Long) output.get(FeedhitNames.feedid));
        }
        return output;
    }

    private void sendServiceUnavailable() {
        RemoteLog.logServiceUnavailable(this);
    }

    private void log() {
        String str = null;
        if (Logx.isLoggable(3)) {
            long lastDownloadTime = getLastDownloadTime();
            long lastDisplayTime = getFeedDisplayHandler().getLastDisplayTime();
            Feed feed = new Feed();
            Class cls = getClass();
            String str2 = "Time since: last update: {0}\nTime since last display: {1}";
            Object[] objArr = new Object[2];
            objArr[0] = lastDownloadTime < 1 ? null : feed.getTimeElapsed(new Date(lastDownloadTime));
            if (lastDisplayTime >= 1) {
                str = feed.getTimeElapsed(new Date(lastDisplayTime));
            }
            objArr[1] = str;
            Logx.debug(cls, str2, objArr);
        }
    }
}

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
import com.looseboxes.idisc.common.jsonview.FeedSearcher;
import com.looseboxes.idisc.common.jsonview.FeedSearcher.FeedSearchResult;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.jsonview.JsonView;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.AppRater;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.Util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MainActivity extends DefaultFeedListActivity {
    public static final String EXTRA_BOOLEAN_CLEAR_PREVIOUS;
    private DefaultReadTask _cdt;
    private long _llnt;

    private class TerminateDownloadTask implements Runnable {
        private final DefaultReadTask downloadTask;

        private TerminateDownloadTask(DefaultReadTask downloadTask) {
            this.downloadTask = downloadTask;
        }

        public void run() {
            Logx.log(Log.VERBOSE, getClass(), "#run");
            Logx.debug(getClass(), "Cancelling update\nStatus: " + this.downloadTask.getProgressStatus());
            try {
                Util.cancel(this.downloadTask);
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
    }

    private class DownloadManager extends FeedDownloadManager {
        private DownloadManager(Context context) {
            super(context);
        }

        public void onPostSuccess(List download) {
            if (download == null || download.isEmpty()) {
                onNoResult();
            } else {
                MainActivity.this.getFeedDisplayHandler().displayFeeds(download);
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
                Popup.show(MainActivity.this, message, length);
            }
        }

        private void onNoResult() {
            List lastDownload = FeedDownloadManager.getDownload(MainActivity.this);
            if (lastDownload != null && !lastDownload.isEmpty()) {
                MainActivity.this.getFeedDisplayHandler().displayFeeds(lastDownload);
            } else if (Util.isNetworkConnectedOrConnecting(getContext())) {
                RemoteLog.logServiceUnavailable(getContext());
            } else {
                Popup.show(getContext(), getContext().getString(R.string.err_noconnection), 0);
            }
        }
    }

    static {
        EXTRA_BOOLEAN_CLEAR_PREVIOUS = MainActivity.class.getName() + ".clearPrevious";
    }

    public boolean isReloadDisplay() {
        return isNextDownloadDue() && !hasNewDownloads();
    }

    public void loadNewer() {
        if (isNextDownloadDue(this._llnt) && !hasNewDownloads()) {
            this._llnt = System.currentTimeMillis();
            createDownloadTask().execute();
        }
    }

    public boolean isToFinishOnNoResult() {
        return false;
    }

    public boolean isClearPrevious() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra(EXTRA_BOOLEAN_CLEAR_PREVIOUS, false);
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
                return;
            }
            Logx.log(Log.DEBUG, getClass(), "Before welcome screen");
            startActivity(new Intent(this, WelcomeActivity.class));
            Logx.log(Log.DEBUG, getClass(), "Continuing onCreate after displaying welcome screen");
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
        DefaultReadTask downloadMgr = createDownloadTask();
        Logx.debug(getClass(), "...Downloading feeds");
        downloadMgr.execute();
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

    public DefaultReadTask createDownloadTask() {
        cancelCurrentDownloadTask();
        this._cdt = new DownloadManager(this);
        return this._cdt;
    }

    private void cancelCurrentDownloadTask() {
        if (this._cdt != null) {
            Util.cancel(this._cdt);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        cancelCurrentDownloadTask();
    }

    public boolean hasNewDownloads() {
        return getLastDownloadTime() > getFeedDisplayHandler().getLastDisplayTime();
    }

    public boolean isNextDownloadDue() {
        return isNextDownloadDue(getLastDownloadTime());
    }

    public boolean isNextDownloadDue(long lastDownloadTime) {
        return (lastDownloadTime <= 0 && isListAdapterEmpty()) || (lastDownloadTime > 0 && System.currentTimeMillis() - lastDownloadTime > Pref.getFeedDownloadIntervalMillis(this));
    }

    public long getLastDownloadTime() {
        return FeedDownloadManager.getLastDownloadTime(this, 0);
    }

    private boolean isListAdapterEmpty() {
        ListAdapter listAdapter = getListFragment().getListAdapter();
        return listAdapter == null ? true : listAdapter.isEmpty();
    }
}

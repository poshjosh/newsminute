package com.looseboxes.idisc.common.activities;

import android.util.Log;
import android.widget.ListAdapter;
import com.looseboxes.idisc.common.asynctasks.DefaultReadTask;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.Util;
import java.util.List;

public abstract class DownloadedFeedListActivity extends DefaultFeedListActivity {
    private long lastExec;

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

    public abstract DefaultReadTask createDownloadTask();

    public abstract long getLastDownloadTime();

    public boolean isClearPrevious() {
        return false;
    }

    public boolean isReloadDisplay() {
        return isNextDownloadDue() && !hasNewDownloads();
    }

    public void loadNewer() {
        if (isReloadDisplay() && System.currentTimeMillis() - this.lastExec > Pref.getFeedDownloadIntervalMillis(this)) {
            this.lastExec = System.currentTimeMillis();
            createDownloadTask().execute();
        }
    }

    public boolean hasNewDownloads() {
        return getLastDownloadTime() > getFeedDisplayHandler().getLastDisplayTime();
    }

    public boolean isToFinishOnNoResult() {
        return false;
    }

    protected void onNoResult(List toDisplay) {
        DefaultReadTask downloadMgr = createDownloadTask();
        Logx.debug(getClass(), "...Downloading feeds");
        downloadMgr.execute();
    }

    public boolean isNextDownloadDue() {
        return isNextDownloadDue(getLastDownloadTime());
    }

    public boolean isNextDownloadDue(long lastDownloadTime) {
        return (lastDownloadTime <= 0 && isListAdapterEmpty()) || (lastDownloadTime > 0 && System.currentTimeMillis() - lastDownloadTime > Pref.getFeedDownloadIntervalMillis(this));
    }

    private boolean isListAdapterEmpty() {
        ListAdapter listAdapter = getListFragment().getListAdapter();
        return listAdapter == null ? true : listAdapter.isEmpty();
    }
}

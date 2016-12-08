package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bc.android.core.asynctasks.ReadJsonObjectWithSingleEntry;
import com.bc.android.core.io.StorageIOException;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.asynctasks.LogAppLaunch;
import com.looseboxes.idisc.common.jsonview.DefaultFeedList;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.notice.PromptRateApp;
import com.looseboxes.idisc.common.notice.PromptWifiOnly;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.looseboxes.idisc.common.util.Pref;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends DefaultFeedListActivity
        implements FeedDownloadManager.OnNewFeedsListener {

    public static final String EXTRA_BOOLEAN_CLEAR_PREVIOUS = MainActivity.class.getName() + ".clearPrevious";

    private ReadJsonObjectWithSingleEntry currentDownloadTask;

    private long lastLoadNewerTime;
    public boolean isNextLoadNewerDue() {
        final long intervalMillis = TimeUnit.SECONDS.toMillis(R.integer.refreshfeedsbyuser_interval_seconds);
        return (lastLoadNewerTime > 0 || (System.currentTimeMillis() - lastLoadNewerTime) >= intervalMillis);
    }


    public void loadNewer() {

        Popup popup = Popup.getInstance();

        ListView listView = this.getListFragment().getListView();

        if(this.isCurrentDownloadTaskRunning()) {

            return;
        }

        if(!this.isNextLoadNewerDue()) {

            popup.showAtTop(listView, R.string.msg_nonewfeeds, Toast.LENGTH_LONG);

            return;
        }

        this.lastLoadNewerTime = System.currentTimeMillis();

        popup.showAtTop(listView, R.string.msg_loadingnewer, Toast.LENGTH_SHORT);

        List<JSONObject> newlyDownloadedFeeds = FeedDownloadManager.getNewlyDownloadedFeeds();

        if(newlyDownloadedFeeds != null && !newlyDownloadedFeeds.isEmpty()) {

            this.displayFeeds(newlyDownloadedFeeds, true);

            FeedDownloadManager.setNewlyDownloadedFeeds(null);

        }else{

            if(NewsminuteUtil.isPreferredNetworkConnectedOrConnecting(this)) {

                ReadJsonObjectWithSingleEntry downloadTask = createDownloadTask(false, null);

                if(downloadTask != null) {
                    downloadTask.execute();
                }
            }else{

                popup.showAtTop(listView, NewsminuteUtil.getNetworkUnavailableMessageResourceId(this), Toast.LENGTH_LONG);
            }
        }
    }

    public boolean isToFinishOnNoResult() {
        return false;
    }

    private boolean isClearPreviousOnCreate(boolean defaultValue) {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra(EXTRA_BOOLEAN_CLEAR_PREVIOUS, defaultValue);
    }

    public int getTabId() {
        return R.id.tabs_all;
    }

    public int getContentViewId() {
        return R.layout.main;
    }

    @Override
    public Set<String> getSearchPhrases() {
        Set<String> output;
        final String autosearchText = Pref.getAutoSearchText(this);
        if (autosearchText == null || autosearchText.isEmpty()) {
            output = Collections.EMPTY_SET;
        }else {
            String[] toFind = autosearchText.split(",");
            if (toFind.length != 0) {
                output = new HashSet(Arrays.asList(toFind));
            }else{
                output = Collections.EMPTY_SET;
            }
        }
        return output;
    }

    @CallSuper
    @Override
    protected void doCreate(final Bundle savedInstanceState) {

        final ProgressBar progressBar = (ProgressBar)this.findViewById(R.id.main_progressbar_indeterminate);

        try {
            this.setVisibility(progressBar, ProgressBar.VISIBLE);

            super.doCreate(savedInstanceState);

            executeMisc();

            displayFeeds(FeedDownloadManager.getDownload(MainActivity.this), MainActivity.this.isClearPreviousOnCreate(true));

        }finally {
            setVisibility(progressBar, ProgressBar.GONE);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        FeedDownloadManager.setOnNewFeedsListener(this);
    }

    @Override
    protected void onNoResult(Collection<JSONObject> toDisplay) {

        if(this.getFeedListDisplayHandler().isEmpty()) {

            List<JSONObject> defaultFeeds = new DefaultFeedList(this);

            if (defaultFeeds != null && !defaultFeeds.isEmpty()) {

                this.displayFeeds(defaultFeeds, false);
            }
        }
    }

    @Override
    public void onNewFeeds(final List<JSONObject> newlyDownloadedFeeds) {

        final ListView listView = this.getListFragment().getListView();

        final boolean listIsVisible = listView.getVisibility() == View.VISIBLE;

        Logx.getInstance().debug(this.getClass(),
                "#onNewsFeeds(List<JSONObject>)\nApp is visible: {0}. List is visible: {1}",
                App.isVisible(), listIsVisible);

        if(App.isVisible() && listIsVisible) {

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        displayFeeds(newlyDownloadedFeeds, true);

                        FeedDownloadManager.setNewlyDownloadedFeeds(null);

                        Popup.getInstance().showAtTop(listView, R.string.msg_newfeedsnotice, Toast.LENGTH_LONG);

                    } catch (Exception e) {
                        Logx.getInstance().log(this.getClass(), e);
                    }
                }
            });
        }
    }

    @Override
    public JSONObject getItemToScrollToOnResume() {
        JSONObject output;
        boolean noticeFeed = false;
        Intent intent = getIntent();
        if (intent == null) {
            output = null;
        } else {
            String extra = intent.getStringExtra(FeedNotificationHandler.EXTRA_JSON_STRING_NOTIFIED_FEED);
            if (extra != null) {
                intent.removeExtra(FeedNotificationHandler.EXTRA_JSON_STRING_NOTIFIED_FEED);
                noticeFeed = true;
            }
            if (extra == null || extra.isEmpty()) {
                output = null;
            } else {
                try {
                    output = (JSONObject) new JSONParser().parse(extra);
                } catch (Exception e) {
                    output = null;
                    Logx.getInstance().log(getClass(), e);
                }
            }
        }
        if (output != null && noticeFeed) {
            FeedNotificationHandler fnh = new FeedNotificationHandler(this);
            Logx.getInstance().debug(this.getClass(), "Item to scroll to on resume has ID: {0}, title: {1}",
                    output.get(FeedNames.feedid), output.get(FeedNames.title));
            fnh.onViewed(((Long) output.get(FeedNames.feedid)).intValue());
        }
        return output;
    }

    public ReadJsonObjectWithSingleEntry createDownloadTask(
            boolean clearCurrent, ReadJsonObjectWithSingleEntry defaultValue) {
        if(clearCurrent) {
            clearCurrentDownloadTask();
        }
        if(currentDownloadTask == null) {
            this.currentDownloadTask = new FeedDownloadManager(this);
            this.currentDownloadTask.setProgressBarIndeterminate((ProgressBar) this.findViewById(R.id.main_progressbar_indeterminate));
            return this.currentDownloadTask;
        }else {
            return defaultValue;
        }
    }

    private boolean isCurrentDownloadTaskRunning() {
        if(this.currentDownloadTask != null) {
            return this.currentDownloadTask.isStarted() &&
                    !this.currentDownloadTask.isCancelled() &&
                    !this.currentDownloadTask.isCompleted();
        }
        return false;
    }

    private void clearCurrentDownloadTask() {
        if (this.currentDownloadTask != null) {
            NewsminuteUtil.cancel(this.currentDownloadTask);
            this.currentDownloadTask = null;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        clearCurrentDownloadTask();
    }

    public boolean hasPendingFeedsToDisplay() {
        return FeedDownloadManager.getLastDownloadTime(this) > this.getListFragment().getFeedListDisplayHandler().getLastDisplayTime();
    }

    private void executeMisc() {
        new Thread() {
            @Override
            public void run() {
                MainActivity.this.doExecuteMisc();
            }
        }.start();
    }

    private void doExecuteMisc() {
        try {

            boolean installed = App.isInstalled(this);

            App.init(getApplicationContext());

            if (installed) {

                new LogAppLaunch(this).execute();

                final boolean displayed = new PromptRateApp(R.string.msg_rate_app, R.string.msg_rate_app_text, 0.33f, 3, 3).attemptShow(this);

                if(!displayed && !Pref.isWifiOnly(this, false)) {

                    new PromptWifiOnly(R.string.pref_wifionly_title, R.string.msg_wifionly_reason, 1.0f, 0, 0).attemptShow(this);
                }

                if(Pref.getPreferredCategories(this).isEmpty() && Pref.getPreferredSources(this).isEmpty()) {

                    try {

//                        Intent categoryPreferencesIntent = new Intent(this, SettingsActivity.class);
//                        categoryPreferencesIntent.putExtra(SettingsActivity.EXTRA_BOOLEAN_DISPLAY_CATEGORIES, true);
                        Intent categoryPreferencesIntent = new Intent(this, SelectPreferredCategoriesActivity.class);
                        this.startActivity(categoryPreferencesIntent);

                    }catch(Exception e) {
                        Logx.getInstance().log(this.getClass(), e);
                    }
                }

                // Requires permission
                // <uses-permission android:name="android.permission.READ_CONTACTS"/>
                //
//                try{
//                    new ContactEmailsExtractor().sendNameEmailDetails(this);
//                }catch(Exception e) {
//                    Logx.getInstance().log(Log.WARN, this.getClass(), "Error extracting contact emails", e);
//                }
            }else{

                Logx.getInstance().log(Log.DEBUG, getClass(), "Before welcome screen");

                startActivity(new Intent(this, WelcomeActivity.class));

                Logx.getInstance().log(Log.DEBUG, getClass(), "Continuing onCreate after displaying welcome screen");
            }
        } catch (StorageIOException e) {
            App.handleInstallationError(this, e, R.string.msg_storageioerror_advice);
        } catch (IOException e2) {
            App.handleInstallationError(this, e2, R.string.msg_restartapp);
        }
    }
}

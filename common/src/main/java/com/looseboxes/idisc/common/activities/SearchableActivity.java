package com.looseboxes.idisc.common.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadTask;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.fragments.DefaultListFragment;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.search.FeedSearcherFeedid;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SearchableActivity extends DefaultFeedListActivity {

    public static String EXTRA_STRING_FEEDFILTER_CLASSNAME = SearchableActivity.class.getName() + ".FeedFilterClassName";

    class SearchFeedsOnServerTask extends FeedDownloadTask {

        private final String query;
        private final TextView textView;

        SearchFeedsOnServerTask(Context context, TextView textView, String query) {
            super(context);
            this.query = query;
            this.textView = textView;
            this.setProgressBarIndeterminate((ProgressBar)SearchableActivity.this.findViewById(R.id.searchresults_progressbar_indeterminate));
            this.setProgressBarLinear((ProgressBar)SearchableActivity.this.findViewById(R.id.searchresults_progressbar_linear));
        }

        @Override
        protected void doOnProgressUpdate(ProgressStatus progressStatus) {

            final int percent = getProgressPercent(progressStatus);

            final String message;
            if(percent < 100) {
                message = this.getContext().getString(R.string.msg_loading) + ": " + percent + '%';
            }else{
                message = this.getContext().getString(R.string.msg_done);
            }

            this.textView.setText(message);
        }

        public void onSuccess(List download) {
            SearchableActivity.this.displayFeeds(download, this.query, true);
        }

        public String getOutputKey() {
            return FileIO.getSearchfeedskey();
        }
    }

    public FeedFilter createFeedFilter() {
        FeedFilter feedFilter = ((DefaultApplication) getApplication()).createFeedFilter(this);
        return feedFilter != null ? feedFilter : super.createFeedFilter();
    }

    public int getTabId() {
        return -1;
    }

    public int[] getTabIds() {
        return null;
    }

    public int getContentViewId() {
        return R.layout.searchresults;
    }

    protected void onNoResult(Collection<JSONObject> items) {

        Popup.getInstance().show(this, R.string.msg_nosearchresult, Toast.LENGTH_SHORT);
    }

    protected void handleIntent(final Intent intent) {

        if ("android.intent.action.SEARCH".equals(intent.getAction())) {

            final ProgressBar progressBarIndeterminate = (ProgressBar)this.findViewById(R.id.searchresults_progressbar_indeterminate);
            final ProgressBar progressBarLinear = (ProgressBar)this.findViewById(R.id.searchresults_progressbar_linear);

            try {

                this.setVisibility(progressBarIndeterminate, ProgressBar.VISIBLE);
                this.setVisibility(progressBarLinear, ProgressBar.VISIBLE);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            SearchableActivity.this.handleSearchIntent(intent);
                        }catch(Exception e) {
                            Logx.getInstance().log(this.getClass(), e);
                        }finally {
                            SearchableActivity.this.setVisibility(progressBarIndeterminate, ProgressBar.GONE);
                            SearchableActivity.this.setVisibility(progressBarLinear, ProgressBar.GONE);
                        }
                    }
                }.start();
            }catch(RuntimeException e){

                this.setVisibility(progressBarIndeterminate, ProgressBar.GONE);
                this.setVisibility(progressBarLinear, ProgressBar.GONE);

                throw e;
            }
        }
    }

    protected void handleSearchIntent(Intent intent) {

        final DefaultListFragment listFragment = this.getListFragment();

        final ListView listView = listFragment.getListView();

        final ProgressBar progressBarLinear = (ProgressBar)this.findViewById(R.id.searchresults_progressbar_linear);

        this.setProgress(listView, progressBarLinear, 10);

        listFragment.setMessageToDisplayWhileLoading(getString(R.string.msg_searchingfeeds));

        String query = intent.getStringExtra(SearchManager.QUERY);

        Logx.getInstance().log(Log.DEBUG, getClass(), "Handling search intent, query: {0}", query);

        final Button serveroption = (Button)findViewById(R.id.searchresults_serveroption);
        this.setText(listView, serveroption, R.string.msg_loadmoreresults);

        this.addSearchOnServerListenerForQuery(serveroption, query);

        this.setText(listView, ((TextView) findViewById(R.id.searchresults_message)), R.string.msg_searchingfeeds);

        final List<JSONObject> toSearch = FeedDownloadManager.getDownload(this);

        this.setProgress(listView, progressBarLinear, 40);

        final List<Long> feedids = this.search(toSearch, query);

        this.setProgress(listView, progressBarLinear, 80);

        final Collection<JSONObject> toDisplay = this.getFeedsToDisplay(serveroption, toSearch, query, feedids);

        this.displayFeeds(toDisplay, query, false);

        this.setProgress(listView, progressBarLinear, 90);
    }

    protected void addSearchOnServerListenerForQuery(final TextView textView, final String query) {
        try {
            textView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SearchableActivity.this.searchOnServer(textView, query);
                }
            });
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    public void displayFeeds(final Collection<JSONObject> toDisplay, final String query, final boolean append) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SearchableActivity.this.clearDisplayedFeeds();
                final int displayed = SearchableActivity.this.displayFeeds(toDisplay, append);
                SearchableActivity.this.updateMessage(displayed, query, append);
            }
        });
    }

    private List<Long> search(Collection<JSONObject> toSearch, String query) {

        FeedSearcherFeedid searcher = new FeedSearcherFeedid();

        List<Long> results = searcher.searchFor(query, toSearch);

        List<Long> output = new ArrayList(results.size());

        for(Long feedid:results) {

            output.add(feedid);
        }

        return output;
    }

    private List<JSONObject> getFeedsToDisplay(
            TextView textView, Collection<JSONObject> toSearch, String query, List<Long> searchedFeedids) {

        if (searchedFeedids == null || searchedFeedids.isEmpty()) {

            this.searchOnServer(textView, query);

            return Collections.EMPTY_LIST;
        }

        Feed feed = new Feed();
        List<JSONObject> found = new ArrayList();
        for (JSONObject json : toSearch) {
            feed.setJsonData(json);
            if (searchedFeedids.contains(feed.getFeedid())) {
                found.add(json);
            }
        }

        Logx.getInstance().log(Log.DEBUG, getClass(), "Results found: {0}", (found == null ? null : found.size()));

        return found;
    }

    private int updateMessage(int size, String query, boolean append) {

        if(append) {

            size += this.getFeedListDisplayHandler().size();
        }

        this.updateMessage(size, query);

        return size;
    }

    private void updateMessage(final int size, final String query) {
        final TextView textView = ((TextView) findViewById(R.id.searchresults_message));
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(size + " search results for '" + query + "'");
            }
        });
    }

    private void searchOnServer(TextView textView, String query) {
        try {
            if (query != null) {

                if(!Util.isNetworkConnectedOrConnecting(this)) {
                    Popup.getInstance().show(this, R.string.err_internetunavailable, Toast.LENGTH_LONG);
                    return;
                }

                FeedDownloadTask downloadTask = new SearchFeedsOnServerTask(this, textView, query);
                downloadTask.addOutputParameter(SearchManager.QUERY, query);
                downloadTask.execute();
            }
        } catch (Exception e) {
            Popup.getInstance().show(SearchableActivity.this, (Object) "Failed to update searchresults", 1);
            Logx.getInstance().log(getClass(), e);
        }
    }
}

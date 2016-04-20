package com.looseboxes.idisc.common.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.AsyncReadTask.ProgressStatus;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadTask;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FeedFilterWithBundle;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedSearcher;
import com.looseboxes.idisc.common.jsonview.FeedSearcher.FeedSearchResult;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;

public class SearchableActivity extends DefaultFeedListActivity {
    public static String EXTRA_STRING_FEEDFILTER_CLASSNAME;
    private String query;

    /* renamed from: com.looseboxes.idisc.common.activities.SearchableActivity.2 */
    class AnonymousClass2 extends FeedDownloadTask {
        final /* synthetic */ Button val$button;

        AnonymousClass2(Context x0, Button button) {
            super(x0);
            this.val$button = button;
        }

        protected void onProgressUpdate(Object... values) {
            if (values != null && values.length > 0) {
                if (values[0] instanceof ProgressStatus) {
                    int percent = getProgressPercent((ProgressStatus)values[0]);
                    this.val$button.setText(SearchableActivity.this.getString(R.string.msg_loading) + ": " + percent + "%");
                    if (percent >= 100) {
                        this.val$button.setText(SearchableActivity.this.getString(R.string.msg_done));
                        return;
                    }
                    return;
                }
                Logx.debug(getClass(), values[0]);
            }
        }

        public String getOutputKey() {
            return FileIO.getSearchfeedskey();
        }

        public void onSuccess(List download) {
            if (!(download == null || download.isEmpty())) {
                SearchableActivity.this.getFeedDisplayHandler().displayFeeds(download);
            }
            SearchableActivity.this.updateMessage((download == null ? null : Integer.valueOf(download.size())).intValue(), SearchableActivity.this.query);
        }

        public Context getContext() {
            return SearchableActivity.this;
        }
    }

    static {
        EXTRA_STRING_FEEDFILTER_CLASSNAME = SearchableActivity.class.getName() + ".FeedFilterClassName";
    }

    public FeedFilter getFeedFilter() {
        FeedFilterWithBundle feedFilter = ((DefaultApplication) getApplication()).createFeedFilter(this);
        return feedFilter != null ? feedFilter : super.getFeedFilter();
    }

    public int getTabId() {
        return -1;
    }

    public int[] getTabIds() {
        return null;
    }

    public int getContentView() {
        return R.layout.searchresults;
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        try {
            super.onPostCreate(savedInstanceState);
            ((Button) findViewById(R.id.searchresults_serveroption)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    try {
                        SearchableActivity.this.downloadFeeds();
                    } catch (Exception e) {
                        Popup.show(SearchableActivity.this, (Object) "Failed to update searchresults", 1);
                        Logx.log(getClass(), e);
                    }
                }
            });
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.query = null;
    }

    public boolean isToFinishOnNoResult() {
        return false;
    }

    protected void handleIntent(Intent intent) {
        if ("android.intent.action.SEARCH".equals(intent.getAction())) {
            getListFragment().setMessageToDisplayWhileLoading(getString(R.string.msg_searchingfeeds));
            getFeedDisplayHandler().getFeedDisplayArray().clear();
            this.query = intent.getStringExtra(SearchManager.QUERY);
            Logx.log(Log.DEBUG, getClass(), "Handling search intent, query: {0}", this.query);
            ((Button) findViewById(R.id.searchresults_serveroption)).setText(R.string.msg_loadmoreresults);
            ((TextView) findViewById(R.id.searchresults_message)).setText(R.string.msg_searchingfeeds);
            super.handleIntent(intent);
        }
    }

    public List<JSONObject> getFeedsToDisplay() {
        List<JSONObject> toSearch = FeedDownloadManager.getDownload(this);
        return getFeeds(toSearch, this.query, search(toSearch, this.query));
    }

    private FeedSearchResult[] search(Collection<JSONObject> toSearch, String query) {
        getFeedDisplayHandler().getFeedDisplayArray().clear();
        FeedSearcher searcher = new FeedSearcher((Context) this);
        searcher.setOutputSize(App.getPropertiesManager(this).getInt(PropertyName.textLengthShort));
        return searcher.searchFor(query, (Collection) toSearch);
    }

    private List<JSONObject> getFeeds(Collection<JSONObject> toSearch, String query, FeedSearchResult[] results) {
        getFeedDisplayHandler().getFeedDisplayArray().clear();
        if (results == null || results.length == 0) {
            updateMessage(0, query);
            return null;
        }
        Feed feed = new Feed();
        List<JSONObject> found = new ArrayList();
        for (JSONObject json : toSearch) {
            feed.setJsonData(json);
            if (get(results, feed.getFeedid()) != null) {
                found.add(json);
            }
        }
        final int size = found == null ? 0 : found.size();
        Logx.log(Log.DEBUG, getClass(), "Results found: {0}", Integer.valueOf(size));
        updateMessage(size, query);
        return found;
    }

    private FeedSearchResult get(FeedSearchResult[] results, Long feedId) {
        for (FeedSearchResult result : results) {
            if (result.getFeedId().equals(feedId)) {
                return result;
            }
        }
        return null;
    }

    private void updateMessage(int found, String query) {
        ((TextView) findViewById(R.id.searchresults_message)).setText(found + " search results for '" + query + "'");
    }

    private void downloadFeeds() {
        if (this.query != null) {
            FeedDownloadTask downloadTask = new AnonymousClass2(this, (Button) findViewById(R.id.searchresults_serveroption));
            downloadTask.getOutputParameters().put(SearchManager.QUERY, this.query);
            downloadTask.execute();
        }
    }
}

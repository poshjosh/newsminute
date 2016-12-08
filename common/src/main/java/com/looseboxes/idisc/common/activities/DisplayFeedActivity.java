package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebView;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FeedFilterSelectedFeed;
import com.looseboxes.idisc.common.fragments.DisplayFeedFragment;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.util.FeedListHtmlBuilder;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DisplayFeedActivity extends WebContentActivity<DisplayFeedFragment> {

    public static final String EXTRA_LONG_SELECTED_FEEDID = DisplayFeedActivity.class.getPackage().getName() + ".SelectedFeedID";
    public static final String EXTRA_STRING_SELECTED_FEED_JSON = DisplayFeedActivity.class.getPackage().getName() + ".SelectedFeed";

    class FeedTitlesDisplayTask extends AsyncTask<Object, Void, List<JSONObject>> {

        private final View ref$view;
        private final Context ref$context;
        private final Feed ref$selectedFeed;

        public FeedTitlesDisplayTask(View view, Feed selectedFeed) {
            this.ref$view = view;
            this.ref$context = view.getContext();
            this.ref$selectedFeed = selectedFeed;
        }

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            ref$view.setVisibility(View.GONE);
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<JSONObject> doInBackground(Object... params) {

            final int displaySize = 5;

            List<JSONObject> toDisplay = new ArrayList<>(displaySize);

            List<JSONObject> download = FeedDownloadManager.getDownload(ref$context);

            if (download != null && !download.isEmpty()) {

                FeedFilter feedFilter = new FeedFilterSelectedFeed(
                        ref$context, download, ref$selectedFeed, displaySize);

                Feed feed = new Feed();

                for(JSONObject json:download) {

                    feed.setJsonData(json);

                    if( feedFilter.accept(feed) ) {

                        toDisplay.add(json);
                    }
                }

                Logx.getInstance().debug(this.getClass(), "Accepted for display by FeedFilter: {0}", toDisplay.size());

                if(toDisplay.size() < displaySize) {
                    final int toAdd = displaySize - toDisplay.size();
                    int added = 0;
                    for(JSONObject json:download) {

                        feed.setJsonData(json);

                        if( !feedFilter.accept(feed) ) {

                            toDisplay.add(json);

                            ++added;
                            if(added >= toAdd) {
                                break;
                            }
                        }
                    }

                    Logx.getInstance().debug(this.getClass(), "Additionally added: {0}", added);
                }
            }

            return toDisplay;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param jsonObjects The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(List<JSONObject> jsonObjects) {

            if(jsonObjects != null && !jsonObjects.isEmpty()) {

                ref$view.setVisibility(View.VISIBLE);

                Logx.getInstance().debug(this.getClass(), "Displaying {0} feeds", jsonObjects==null?null:jsonObjects.size());

                StringBuilder html = new StringBuilder();
                new FeedListHtmlBuilder(this.ref$context).buildHtml(jsonObjects, html);

                WebView webView = (WebView) DisplayFeedActivity.this.findViewById(R.id.feedview_titles_display);

                // First display loading message before the actual loading begins
                NewsminuteUtil.loadData(webView, getString(R.string.msg_loading));
                NewsminuteUtil.loadData(webView, html.toString());
            }
        }
    }

    public DisplayFeedFragment createFragment() {
        return new DisplayFeedFragment();
    }

    @Override
    protected void onResume() {

        super.onResume();

        AsyncTask titlesDisplayTask = new FeedTitlesDisplayTask(
                this.findViewById(R.id.feedview_titles_header),
                this.getFragment().getSelectedFeed());

        titlesDisplayTask.execute((Object[])null);
    }
}

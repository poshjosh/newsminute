package com.looseboxes.idisc.common.handlers;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.activities.JsonListAdapter;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.search.FeedSearcherFeedid;
import com.looseboxes.idisc.common.listeners.ListOnScrollListener;
import com.looseboxes.idisc.common.util.Pref;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeedListDisplayHandler  {

    private final Set<String> searchPhrases;
    private final ListView listView;
    private final JsonListAdapter jsonListAdapter;
    private final ListOnScrollListener scrollListener;

    public FeedListDisplayHandler(ListView listView, Set<String> searchPhrases) {

        this(listView, searchPhrases, Pref.getFeedDownloadLimit(listView.getContext()));
    }

    public FeedListDisplayHandler(ListView listView, Set<String> searchPhrases, int displayBufferSize) {

        this(listView, searchPhrases, displayBufferSize, new ListOnScrollListener());
    }

    public FeedListDisplayHandler(ListView listView, Set<String> searchPhrases, ListOnScrollListener onScrollListener) {

        this(listView, searchPhrases, Pref.getFeedDownloadLimit(listView.getContext()), onScrollListener);
    }

    public FeedListDisplayHandler(ListView listView, Set<String> searchPhrases,
                                  int displayBufferSize, ListOnScrollListener onScrollListener) {

        this(listView, searchPhrases, new JsonListAdapter(listView.getContext(), new ArrayList(displayBufferSize)), onScrollListener);
    }

    public FeedListDisplayHandler(ListView listView, Set<String> searchPhrases,
                                  JsonListAdapter jsonListAdapter, ListOnScrollListener onScrollListener) {
        log("FeedListDisplayHandler(ListView, Set<String>)");
        this.searchPhrases = searchPhrases;
        this.listView = Util.requireNonNull(listView);
        this.jsonListAdapter = Util.requireNonNull(jsonListAdapter);
        this.scrollListener = Util.requireNonNull(onScrollListener);
        listView.setOnScrollListener(scrollListener);
    }

    public int displayFeeds(Collection<JSONObject> feeds, FeedFilter feedFilter) {
        int published;
        try {

            feeds = this.filterFeeds(feeds, feedFilter);

            this.searchFeeds(feeds);

            published = this.publishFeeds(feeds, true);

            if (jsonListAdapter.getEmphasisView() != null) {
                //@todo
            }

        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
            published= 0;
        }
        return published;
    }

    public void searchFeeds(final Collection<JSONObject> feeds) {

        final Set<String> searchPhrases = getSearchPhrases();

        if(searchPhrases != null && !searchPhrases.isEmpty()) {

            AsyncTask task = new AsyncTask(){
                @Override
                protected Object doInBackground(Object... params) {
                    try {

                        final long startTime = System.currentTimeMillis();

                        Map<String, List<Long>> results = new FeedSearcherFeedid().searchFor(searchPhrases, feeds);
                        if (results != null && !results.isEmpty()) {
                            jsonListAdapter.addSearchresultFeedids(results);
                        }

                        Logx.getInstance().debug(this.getClass(), "Time spent searching feeds to display: {0} millis", (System.currentTimeMillis() - startTime));

                    }catch(Exception e) {
                        Logx.getInstance().log(this.getClass(), e);
                    }
                    return this;
                }
            };

            task.execute();
        }
    }

    public Collection<JSONObject> filterFeeds(Collection<JSONObject> feeds, FeedFilter feedFilter) {

        Logx.getInstance().debug(this.getClass(), "FeedFilter: {0}", feedFilter);

        final long startTime = System.currentTimeMillis();

        Collection<JSONObject> toDisplay;

        if (feeds == null || feeds.isEmpty()) {
            toDisplay = null;
        } else {
            toDisplay = new ArrayList();
            Feed feed = new Feed();
            for (JSONObject json : feeds) {
                feed.setJsonData(json);
                if (feedFilter == null || feedFilter.accept(feed)) {
                    toDisplay.add(json);
                }
            }
        }

        Logx.getInstance().debug(this.getClass(), "Filter rate: {0}/{1}. Time spent: {2} millis",
                toDisplay==null?null:toDisplay.size(), feeds==null?null:feeds.size(),
                (System.currentTimeMillis() - startTime));

        return toDisplay;
    }

    public synchronized int publishFeeds(final Collection<JSONObject> toDisplay, boolean notifyDatasetChanged) {

        Logx.getInstance().debug(this.getClass(), "Displaying {0} feeds", toDisplay == null ? null : toDisplay.size());

        if (toDisplay != null && !toDisplay.isEmpty()) {

            printFeedids(Log.VERBOSE, toDisplay);

            jsonListAdapter.setNotifyOnChange(notifyDatasetChanged);

            this.publishFeeds(toDisplay);
        }

        return toDisplay == null ? 0 : toDisplay.size();
    }

    private void publishFeeds(final Collection<JSONObject> toDisplay) {
        try {

            final long startTime = System.currentTimeMillis();

            scrollListener.setDisabled(true);

            if (jsonListAdapter.isEmpty()) {
                jsonListAdapter.addAll(toDisplay);
            } else {
                List<JSONObject> displaying = jsonListAdapter.getFeedDisplayArray();
                List<JSONObject> combined = new ArrayList<>(toDisplay.size() + displaying.size());
                combined.addAll(toDisplay);
                combined.addAll(displaying);
                jsonListAdapter.clear();
                jsonListAdapter.addAll(combined);
//                for (JSONObject json : toDisplay) {
//                    jsonListAdapter.insert(json, 0); // should be slower
//                }
            }

            Logx.getInstance().debug(this.getClass(), "Time spent publishing feeds to display: {0} millis", (System.currentTimeMillis() - startTime));

            setLastDisplayTime(System.currentTimeMillis());

        }finally{

            scrollListener.setDisabled(false);
        }
    }

    public boolean isEmpty() {
        return this.listView.getAdapter().isEmpty();
    }

    public synchronized void clear() {
        try {
            if(!jsonListAdapter.isEmpty()) {
                scrollListener.setDisabled(true);
                scrollListener.reset();
                jsonListAdapter.setNotifyOnChange(true);
                jsonListAdapter.clear();
            }
        } catch (Throwable th) {
            Logx.getInstance().log(this.getClass(), th);
        }finally{
            scrollListener.setDisabled(false);
        }
    }

    private void printFeedids(int priority, Collection<JSONObject> toDisplay) {
        if(!Logx.getInstance().isLoggable(priority)) {
            return;
        }
        StringBuilder builder = new StringBuilder(8 * toDisplay.size());
        builder.append("Displaying ");
        for(JSONObject feed:toDisplay) {
            builder.append(feed.get(FeedNames.feedid)).append(',');
        }
        Logx.getInstance().log(priority, this.getClass(), builder.toString());
    }

    private void setLastDisplayTime(long lval) {
        Pref.setLong(this.listView.getContext(), getLastDisplayTimePreferenceKey(), lval);
    }

    public long getLastDisplayTime() {
        return Pref.getLong(this.listView.getContext(), getLastDisplayTimePreferenceKey(), -1);
    }

    private static String getLastDisplayTimePreferenceKey() {
        return FeedListDisplayHandler.class.getName() + ".lastDisplayTime.long";
    }

    private void log(String key) {
        Logx.getInstance().log(Log.DEBUG, getClass(), key);
    }

    public final Set<String> getSearchPhrases() {
        return searchPhrases;
    }

    public final int size() {
        return jsonListAdapter == null ? 0 : jsonListAdapter.getCount();
    }

    public final JsonListAdapter getJsonListAdapter() {
        return jsonListAdapter;
    }

    public final ListOnScrollListener getScrollListener() {
        return scrollListener;
    }

    public final ListView getListView() {
        return listView;
    }
}

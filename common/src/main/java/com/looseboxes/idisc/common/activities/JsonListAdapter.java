package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;

import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonListAdapter extends JsonObjectListAdapter {

    private final Feed output;
    private final FeedNotificationHandler feedNotificationHandler;

    private List<Long> searchresultFeedids;

    private View emphasisView;

    public JsonListAdapter(Context context, List arr) {
        super(context, arr);
        this.feedNotificationHandler = new FeedNotificationHandler(context);
        this.output = new Feed();
        this.output.setDisplayDateAsTimeElapsed(true);
    }

    protected Long getId(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getFeedid();
    }

    protected String getHeading(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getHeading(getContext().getString(R.string.err), getHeadingLength());
    }

    protected String getInfo(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getInfo(getContext(), "", getInfoLength());
    }

    protected String getAuthor(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getAuthor();
    }

    protected String getImageUrl(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        String imageurl = this.output.getImageUrl();
        if(imageurl == null) {
            imageurl = this.output.getSiteIconurl();
        }
        return imageurl;
    }

    protected String getUrl(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getUrl();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = super.getView(position, convertView, parent);
        TextView textView = (TextView) rowView.findViewById(R.id.listrow_text);
        Long feedId = this.output.getFeedid();
        if (isSearchResult(feedId)) {
            textView.setTypeface(Typeface.DEFAULT, 3);
        }
        if(feedNotificationHandler.isActivelyNotified(feedId)) {
            textView.setTypeface(Typeface.DEFAULT, 3);
            this.emphasisView = textView;
        }
        return rowView;
    }

    private boolean isSearchResult(Long feedid) {
        return searchresultFeedids != null && searchresultFeedids.contains(feedid);
    }

    public String getAuthorFile(String fname) {
        if (this.output.getAuthor() != null) {
            String urlStr = this.output.getUrl();
            if (urlStr != null) {
                try {
                    URL url = new URL(urlStr);
                    return new URL(url.getProtocol(), url.getHost(), fname).toExternalForm();
                } catch (MalformedURLException e) {
                    Logx.getInstance().log(getClass(), e);
                }
            }
        }
        return null;
    }

    public int getPosition(JSONObject feed) {
        final int count = this.getCount();
        for (int i = 0; i < count; i++) {
            if ((this.getItem(i)).get(FeedhitNames.feedid).equals(feed.get(FeedhitNames.feedid))) {
                return i;
            }
        }
        return -1;
    }

    public View getEmphasisView() {
        return this.emphasisView;
    }

    public int addSearchresultFeedids(Map<String, List<Long>> searchresults) {
        if(searchresults == null || searchresults.isEmpty()) {
            return 0;
        }
        if(searchresultFeedids == null) {
            searchresultFeedids = new LinkedList<>();
        }
        int added = 0;
        for(List<Long> resultsList:searchresults.values()) {
            for(Long feedid:resultsList) {
                searchresultFeedids.add(feedid);
                ++added;
            }
        }
        return added;
    }
}

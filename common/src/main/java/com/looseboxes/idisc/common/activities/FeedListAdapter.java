package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedSearcher.FeedSearchResult;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.util.Logx;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

public class FeedListAdapter extends AbstractArrayAdapter {
    private View emphasisView;
    private final Feed output;
    private Map<String, FeedSearchResult[]> searchresults;

    public FeedListAdapter(Context context) {
        super(context);
        this.output = new Feed();
        this.output.setDisplayDateAsTimeElapsed(true);
    }

    public FeedListAdapter(Context context, List arr) {
        super(context, arr);
        this.output = new Feed();
        this.output.setDisplayDateAsTimeElapsed(true);
    }

    protected Long getId(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getFeedid();
    }

    protected String getHeading(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getHeading(getContext().getString(R.string.err), getLen_short());
    }

    protected String getInfo(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getInfo(getContext(), "", getLen_xshort());
    }

    protected String getAuthor(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getAuthor();
    }

    protected String getImageUrl(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getImageUrl();
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
        if (feedId.longValue() != 0 && feedId.longValue() == FeedNotificationHandler.getLastNotifiedFeedId(getContext())) {
            textView.setTypeface(Typeface.DEFAULT, 3);
            this.emphasisView = textView;
        }
        return rowView;
    }

    private boolean isSearchResult(Long feedid) {
        boolean z = false;
        if (!(this.searchresults == null || this.searchresults.isEmpty() || feedid == null)) {
            z = false;
            for (String s : this.searchresults.keySet()) {
                for (FeedSearchResult e : (FeedSearchResult[]) this.searchresults.get(s)) {
                    if (feedid.equals(e.getFeedId())) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public String getAuthorFile(String fname) {
        if (this.output.getAuthor() != null) {
            String urlStr = this.output.getUrl();
            if (urlStr != null) {
                try {
                    URL url = new URL(urlStr);
                    return new URL(url.getProtocol(), url.getHost(), fname).toExternalForm();
                } catch (MalformedURLException e) {
                    Logx.log(getClass(), e);
                }
            }
        }
        return null;
    }

    public View getEmphasisView() {
        return this.emphasisView;
    }

    public Map<String, FeedSearchResult[]> getSearchresults() {
        return this.searchresults;
    }

    public void setSearchresults(Map<String, FeedSearchResult[]> searchresults) {
        this.searchresults = searchresults;
    }
}

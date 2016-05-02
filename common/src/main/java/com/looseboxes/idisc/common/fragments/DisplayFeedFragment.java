package com.looseboxes.idisc.common.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.activities.DisplayFeedActivity;
import com.looseboxes.idisc.common.asynctasks.CommentDownloadTask;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import com.looseboxes.idisc.common.util.Util;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DisplayFeedFragment extends AbstractDisplayFeedFragment implements OnClickListener {
    private JSONObject _sf;
    private Pattern bodyStartPattern;
    private CommentDownloadTask commentDownloadTask;

    class CommentDownloadTaskAsync extends CommentDownloadTask {
        private int displayed;
        final /* synthetic */ ProgressBar val$progressBar;
        final /* synthetic */ TextView val$progressText;

        CommentDownloadTaskAsync(Object x0, int x1, int x2, ProgressBar progressBar, TextView textView) {
            super(x0, x1, x2);
            this.val$progressBar = progressBar;
            this.val$progressText = textView;
        }

        public Context getContext() {
            return DisplayFeedFragment.this.getContext();
        }

        public void onSuccess(JSONArray download) {
            try {
                Logx.log(Log.DEBUG, getClass(), "Processing update");
                CommentListAdapter adapter = (CommentListAdapter) DisplayFeedFragment.this.getCommentList().getAdapter();
                Iterator i$ = download.iterator();
                while (i$.hasNext()) {
                    adapter.add((JSONObject) i$.next());
                    this.displayed++;
                }
                Logx.log(Log.DEBUG, getClass(), "Displayed comments: " + this.displayed);
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }

        protected void onProgressUpdate(Object... values) {
            if (values != null) {
                try {
                    if (values.length <= 0) {
                        return;
                    }
                    if (values[0] instanceof ProgressStatus) {
                        int percent = getProgressPercent((ProgressStatus)values[0]);
                        this.val$progressBar.setProgress(percent);
                        if (percent == 100) {
                            if (this.displayed < 1) {
                                this.val$progressText.setText(R.string.msg_befirsttocomment);
                            } else {
                                this.val$progressText.setText(R.string.msg_comments);
                            }
                            this.val$progressBar.setVisibility(View.GONE);
                            return;
                        }
                        this.val$progressText.setText(DisplayFeedFragment.this.getString(R.string.msg_loadingcomments) + ": " + percent + "%");
                        return;
                    }
                    Logx.debug(getClass(), values[0]);
                } catch (Exception e) {
                    Logx.log(getClass(), e);
                }
            }
        }
    }

//    public String format(String bodyHTML) {
//        bodyHTML = super.format(bodyHTML);
//        bodyHTML = this.addReadfullstoryLink(bodyHTML);
//    }

    private String addReadfullstoryLink(String bodyHTML) {
        try {
            Feed feed = getSelectedFeed();
            if (feed == null) {
                return bodyHTML;
            }
            String toAppend = "<p style=\"width:100%; font-size:1.25em;\"><a href=\"" + feed.getUrl() + "\">" + getString(R.string.msg_readfullstory) + "</a></p>";
            if (!bodyHTML.contains("<body") && !bodyHTML.contains("<BODY")) {
                return toAppend + bodyHTML;
            }
            if (this.bodyStartPattern == null) {
                this.bodyStartPattern = Pattern.compile("<body.*?>", Pattern.CASE_INSENSITIVE);
            }
            Matcher m = this.bodyStartPattern.matcher(bodyHTML);
            StringBuffer buff = new StringBuffer(bodyHTML.length() + toAppend.length());
            if (m.find()) {
                m.appendReplacement(buff, m.group() + toAppend);
            }
            m.appendTail(buff);
            return buff.toString();
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return bodyHTML;
        }
    }

    public int getCommentListResourceId() {
        return R.id.feedview_comments;
    }

    public int getCommentInputResourceId() {
        return R.id.feedview_commentinput;
    }

    public int getContentView() {
        return R.layout.feedview;
    }

    public int getWebViewId() {
        return R.id.feedview_display;
    }

    @Override
    public boolean isSupported(int buttonId) {
        return true;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Button) findViewById(R.id.feedview_sendcomment)).setOnClickListener(this);
        ((Button) findViewById(R.id.feedview_clearcomment)).setOnClickListener(this);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.feedview_sendcomment) {
            onSendCommentButtonClicked(v);
        } else if (id == R.id.feedview_clearcomment) {
            onClearCommentButtonClicked(v);
        } else {
            super.onClick(v);
        }
    }

    protected void updateButtonState(int buttonId) {
        int backgroundResId;
        JSONObject feedData = getSelectedFeed().getJsonData();
        Button v = (Button) findViewById(buttonId);
        int id = v.getId();
        if (id == R.id.contentoptions_favorite) {
            backgroundResId = App.getPreferenceFeedsManager(getContext(), PreferenceType.favorites).contains(feedData) ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp;
        } else if (id == R.id.contentoptions_bookmark) {
            backgroundResId = App.getPreferenceFeedsManager(getContext(), PreferenceType.bookmarks).contains(feedData) ? R.drawable.ic_bookmark_black_24dp : R.drawable.ic_bookmark_border_black_24dp;
        } else {
            return;
        }
        v.setBackgroundResource(backgroundResId);
    }

    public void onPause() {
        super.onPause();
        if (this.commentDownloadTask != null) {
            Util.cancel(this.commentDownloadTask);
        }
    }

    public void onResume() {
        super.onResume();
        TextView progressText = (TextView) findViewById(R.id.downloadcommentsProgressText);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.downloadcommentsProgressBar);
        getCommentList().setEmptyView(progressBar);
        if (this.commentDownloadTask != null) {
            Util.cancel(this.commentDownloadTask);
        }
        this.commentDownloadTask = new CommentDownloadTaskAsync(getSelectedFeed().getFeedid(), 0, 10, progressBar, progressText);
        this.commentDownloadTask.execute();
    }

    public JSONObject getSelectedData() {
        if (this._sf == null) {
            Bundle intent = getArguments();
            boolean noticeFeed = false;
            if (intent == null) {
                this._sf = null;
            } else {
                String extra = intent.getString(DisplayFeedActivity.EXTRA_STRING_SELECTED_FEED_JSON);
                if (extra == null) {
                    long longExtra = intent.getLong(DisplayFeedActivity.EXTRA_LONG_SELECTED_FEEDID, -1);
                    if (longExtra != -1) {
                        this._sf = FeedDownloadManager.getFeed(getContext(), Long.valueOf(longExtra));
                    }
                }
                if (this._sf == null && extra == null) {
                    extra = intent.getString(FeedNotificationHandler.EXTRA_JSON_STRING_NOTIFIED_FEED);
                    if (extra != null) {
                        noticeFeed = true;
                    }
                }
                if (!(this._sf != null || extra == null || extra.isEmpty())) {
                    try {
                        this._sf = (JSONObject) new JSONParser().parse(extra);
                    } catch (Exception e) {
                        Logx.log(getClass(), e);
                    }
                }
            }
            if (this._sf != null && noticeFeed) {
                FeedNotificationHandler.onFeedViewed(getContext(), (Long) this._sf.get(FeedhitNames.feedid));
            }
        }
        return this._sf;
    }
}

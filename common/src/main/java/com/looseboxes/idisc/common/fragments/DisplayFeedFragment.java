package com.looseboxes.idisc.common.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.activities.DisplayFeedActivity;
import com.looseboxes.idisc.common.asynctasks.CommentDownloadTask;
import com.looseboxes.idisc.common.asynctasks.CommentDownloadTaskImpl;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DisplayFeedFragment extends AbstractDisplayFeedFragment
        implements OnClickListener, CommentDownloadTaskImpl.ListFace {

    private JSONObject _selected;

    private CommentDownloadTask commentDownloadTask;

    private TextView _$progressText;
    @Override
    public TextView getProgressText() {
        if(_$progressText == null) {
            _$progressText = (TextView) findViewById(R.id.downloadcommentsProgressText);
        }
        return _$progressText;
    }

    @Override
    public boolean isFragmentAdded() {
        return this.isAdded();
    }

    @Override
    public ArrayAdapter<JSONObject> getListAdapter() {
        return (CommentListAdapter)this.getCommentList().getAdapter();
    }

    public int getCommentListResourceId() {
        return R.id.feedview_comments;
    }

    public int getCommentInputResourceId() {
        return R.id.feedview_commentinput;
    }

    public int getContentViewId() {
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

        this.commentDownloadTask = new CommentDownloadTaskImpl(this.getContext(), getSelectedFeed().getFeedid(), 0, 10, this);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.feedview_progressbar_indeterminate);
        this.commentDownloadTask.setProgressBarIndeterminate(progressBar);
        this.commentDownloadTask.execute();
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
        Button button = (Button) findViewById(buttonId);
        if(button != null) {
            int id = button.getId();
            if (id == R.id.contentoptions_favorite) {
                backgroundResId = new Preferencefeeds(getContext(), PreferenceType.favorites).contains(feedData) ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp;
            } else if (id == R.id.contentoptions_bookmark) {
                backgroundResId = new Preferencefeeds(getContext(), PreferenceType.bookmarks).contains(feedData) ? R.drawable.ic_bookmark_black_24dp : R.drawable.ic_bookmark_border_black_24dp;
            } else {
                return;
            }
            button.setBackgroundResource(backgroundResId);
        }
    }

    public JSONObject getSelectedData() {
        if (this._selected == null) {
            Bundle bundle = getArguments();
            boolean noticeFeed = false;
            if (bundle == null) {
                this._selected = null;
            } else {
                String extra = bundle.getString(DisplayFeedActivity.EXTRA_STRING_SELECTED_FEED_JSON);
                if (extra == null) {
                    long longExtra = bundle.getLong(DisplayFeedActivity.EXTRA_LONG_SELECTED_FEEDID, -1);
                    if (longExtra != -1) {
                        this._selected = FeedDownloadManager.getFeed(getContext(), Long.valueOf(longExtra));
                    }
                }
                if (this._selected == null && extra == null) {
                    extra = bundle.getString(FeedNotificationHandler.EXTRA_JSON_STRING_NOTIFIED_FEED);
                    if (extra != null) {
                        bundle.remove(FeedNotificationHandler.EXTRA_JSON_STRING_NOTIFIED_FEED);
                        noticeFeed = true;
                    }
                }
                if (!(this._selected != null || extra == null || extra.isEmpty())) {
                    try {
                        this._selected = (JSONObject) new JSONParser().parse(extra);
                    } catch (Exception e) {
                        Logx.getInstance().log(getClass(), e);
                    }
                }
            }
            if (this._selected != null && noticeFeed) {
                FeedNotificationHandler fnh = new FeedNotificationHandler(getContext());
                Logx.getInstance().debug(this.getClass(), "Selected feed has ID: {0}, title: {1}",
                        _selected.get(FeedNames.feedid), _selected.get(FeedNames.title));
                fnh.onViewed(((Long) this._selected.get(FeedNames.feedid)).intValue());
            }
        }
        return this._selected;
    }
}

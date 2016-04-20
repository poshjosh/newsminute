package com.looseboxes.idisc.common.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.activities.DisplayCommentActivity;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.Comment;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

public class DisplayCommentFragment extends AbstractDisplayFeedFragment implements OnClickListener {
    private List<Map<String, Object>> _n;

    private class CommentWithReplies extends Comment {
        public CommentWithReplies(JSONObject source) {
            super(source);
        }

        public String getOptions(String defaultValue, int maxLength) {
            String s = super.getOptions(defaultValue, maxLength);
            Object obj = getJsonData().get("repliesCount");
            return obj == null ? s : s + " (" + Integer.parseInt(obj.toString()) + " replies)";
        }
    }

    public int getCommentListResourceId() {
        return R.id.commentview_comments;
    }

    public int getCommentInputResourceId() {
        return R.id.commentview_commentinput;
    }

    public JSONObject getSelectedData() {
        Map<String, Object> notice = getNotice();
        return notice == null ? null : (JSONObject) notice.get("feed");
    }

    public int getContentView() {
        return R.layout.commentview;
    }

    public int getWebViewId() {
        return R.id.commentview_display;
    }

    public boolean isSupported(int buttonId) {
        return false;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Button) findViewById(R.id.commentview_sendcomment)).setOnClickListener(this);
        ((Button) findViewById(R.id.commentview_clearcomment)).setOnClickListener(this);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.commentview_sendcomment) {
            onSendCommentButtonClicked(v);
        } else if (id == R.id.commentview_clearcomment) {
            onClearCommentButtonClicked(v);
        } else {
            super.onClick(v);
        }
    }

    public void onDestroy() {
        try {
            boolean clear = this.getArguments().getBoolean(
                    com.looseboxes.idisc.common.activities.DisplayCommentActivity.EXTRA_BOOL_CLEAR_NOTICES_ON_DESTROY,
                    true);
            if (clear) {
                Context context = this.getContext();
                com.looseboxes.idisc.common.asynctasks.FeedDownloadManager.clearCommentNotifications(context);
            }
        }catch(Exception e) {
            com.looseboxes.idisc.common.util.Logx.log(this.getClass(), e);
        }finally {
            super.onDestroy();
        }
    }

    public void onResume() {
        super.onResume();
        FeedNotificationHandler.onCommentViewed(getContext(), null);
        try {
            populateAdapter((CommentListAdapter) getCommentList().getAdapter());
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void populateAdapter(CommentListAdapter adapter) {
        Map<String, Object> notice = getNotice();
        if (notice == null || notice.isEmpty()) {
            Popup.show(getContext(), R.string.msg_nofeed, 0);
        }
        addNotice(adapter, notice);
    }

    protected Map<String, Object> getNotice() {
        return getNotice(getArguments().getInt(DisplayCommentActivity.EXTRA_INT_SELECTED_NOTICE_POSITION, 0));
    }

    protected Map<String, Object> getNotice(int pos) {
        List<Map<String, Object>> notices = getNotices();
        return (notices == null || notices.isEmpty()) ? null : (Map) notices.get(pos);
    }

    protected List<Map<String, Object>> getNotices() {
        if (this._n == null) {
            this._n = FeedDownloadManager.getCommentNotifications(getContext(), false);
        }
        return this._n;
    }

    protected void addNotice(CommentListAdapter adapter, Map<String, Object> notice) {
        adapter.add((JSONObject) notice.get("comment"));
        List<JSONObject> replies = (List) notice.get("replies");
        if (replies != null && !replies.isEmpty()) {
            for (JSONObject reply : replies) {
                adapter.add(reply);
            }
        }
    }

    protected Comment createComment() {
        return new CommentWithReplies(this.getSelectedData());
    }
}

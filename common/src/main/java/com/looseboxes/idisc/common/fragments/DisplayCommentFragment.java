package com.looseboxes.idisc.common.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.view.View;
import android.view.View.OnClickListener;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.activities.DisplayCommentActivity;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.asynctasks.Updatereadcomments;
import com.looseboxes.idisc.common.jsonview.CommentNames;
import com.looseboxes.idisc.common.notice.CommentNotificationHandler;

import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DisplayCommentFragment extends AbstractDisplayFeedFragment implements OnClickListener {

    private List<Map<String, Object>> _n;

    public int getCommentListResourceId() {
        return R.id.commentview_comments;
    }

    public int getCommentInputResourceId() {
        return R.id.commentview_commentinput;
    }

    public JSONObject getSelectedData() {
        Map<String, Object> notice = getNotice();
        return notice == null ? null : this.getFeed(notice);
    }

    public int getContentViewId() {
        return R.layout.commentview;
    }

    public int getWebViewId() {
        return R.id.commentview_display;
    }

    public int getSendCommentViewId() {
        return R.id.commentview_sendcomment;
    }

    public int getClearCommentViewId() {
        return R.id.commentview_clearcomment;
    }

    public boolean isSupported(int buttonId) {
        return false;
    }

    @Override
    @MainThread
    @CallSuper
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.setOnClickListener(this.getSendCommentViewId());
        this.setOnClickListener(this.getClearCommentViewId());
    }

    public void setOnClickListener(@IdRes int viewId) {
        if(viewId == -1) {
            return;
        }else{
            this.findViewById(viewId).setOnClickListener(this);
        }
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

            boolean clear = this.getArguments() == null ? true : this.getArguments().getBoolean(
                    com.looseboxes.idisc.common.activities.DisplayCommentActivity.EXTRA_BOOL_CLEAR_NOTICES_ON_DESTROY,
                    true);

            if (clear) {
                Context context = this.getContext();
                com.looseboxes.idisc.common.asynctasks.FeedDownloadManager.clearCommentNotifications(context);
            }

            List<Long> commentids = this.getCommentIds();
            if(commentids != null && !commentids.isEmpty()) {
                new Updatereadcomments(this.getContext(), commentids).execute();
            }
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }finally {
            super.onDestroy();
        }
    }

    public List<Long> getCommentIds() {
        Long commentid = this.getCommentId();
        return commentid == null ? Collections.EMPTY_LIST : Arrays.asList(commentid);
    }

    public void onResume() {

        super.onResume();

        new CommentNotificationHandler(getContext()).onViewed(CommentNotificationHandler.COMMENT_NOTICE_ID);

        try {
            populateAdapter((CommentListAdapter) getCommentList().getAdapter());
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    protected void populateAdapter(CommentListAdapter adapter) {
        Map<String, Object> notice = getNotice();
        if (notice == null || notice.isEmpty()) {
            Popup.getInstance().show(getContext(), R.string.msg_nofeed, 0);
        }else {
            addNotice(adapter, notice);
        }
    }

    protected Map<String, Object> getNotice() {
        Bundle bundle = this.getArguments();
        final int pos = bundle == null || bundle.isEmpty() ? 0 :
                bundle.getInt(DisplayCommentActivity.EXTRA_INT_SELECTED_NOTICE_POSITION, 0);
        return getNotice(pos);
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
        adapter.add(this.getComment(notice));
        List<JSONObject> replies = this.getReplies(notice);
        if (replies != null && !replies.isEmpty()) {
            for (JSONObject reply : replies) {
                adapter.add(reply);
            }
        }
    }

    protected Long getCommentId() {
        return this.getCommentId(this.getNotice());
    }

    protected Long getCommentId(Map<String, Object> notice) {
        Long output;
        if(notice == null) {
            output = null;
        }else{
            JSONObject comment = this.getComment(notice);
            if(comment == null) {
                output = null;
            }else{
                output = (Long)comment.get(CommentNames.commentid);
            }
        }
        return output;
    }

    protected JSONObject getFeed(Map<String, Object> notice) {
        return (JSONObject)notice.get("feed");
    }

    protected JSONObject getComment(Map<String, Object> notice) {
        return (JSONObject)notice.get("comment");
    }

    protected List<JSONObject> getReplies(Map<String, Object> notice) {
        return (List<JSONObject>)notice.get("replies");
    }
}

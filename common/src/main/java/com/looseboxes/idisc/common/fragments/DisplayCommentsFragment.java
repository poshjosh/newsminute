package com.looseboxes.idisc.common.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.activities.DisplayCommentActivity;
import com.looseboxes.idisc.common.jsonview.DefaultJsonObject;
import com.looseboxes.idisc.common.jsonview.JsonView;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

public class DisplayCommentsFragment extends DisplayCommentFragment implements OnItemClickListener {

    public int getCommentListResourceId() {
        return R.id.commentgroupview_comments;
    }

    public int getContentViewId() {
        return R.layout.commentgroupview;
    }

    public int getWebViewId() {
        return R.id.commentgroupview_display;
    }

    public int getSendCommentViewId() {
        return -1;
    }

    public int getClearCommentViewId() {
        return -1;
    }


    public boolean isSupported(int buttonId) {
        return false;
    }

    public JSONObject getSelectedData() {
        return new DefaultJsonObject(getContext(), "Your Comments", "Click any of your comments below to view replies to that comment", "comment list description");
    }

    public List<Long> getCommentIds() {
        List<Long> commentids = new LinkedList();
        List<Map<String, Object>> notices = this.getNotices();
        if(notices != null) {
            for (Map<String, Object> notice : notices) {
                commentids.add(this.getCommentId(notice));
            }
        }
        return commentids;
    }

    protected void populateAdapter(CommentListAdapter adapter) {
        List<Map<String, Object>> notices = getNotices();
        if (notices == null || notices.isEmpty()) {
            Popup.getInstance().show(getContext(), R.string.msg_nofeed, 0);
        }
        for (Map<String, Object> notice : notices) {
            addNotice(adapter, notice);
        }
    }

    protected void addNotice(CommentListAdapter adapter, Map<String, Object> notice) {
        adapter.add((JSONObject) notice.get("comment"));
    }

    private ListView _clist;
    public ListView getCommentList() {
        if (this._clist == null) {
            this._clist = (ListView) findViewById(getCommentListResourceId());
            this._clist.setOnItemClickListener(this);
        }
        return this._clist;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        JSONObject comment = getCommentToDisplay(position);
        if (comment == null || comment.isEmpty()) {
            Popup.getInstance().show(getContext(), R.string.msg_nofeed, 0);
            return;
        }
        Intent intent = new Intent(getContext(), DisplayCommentActivity.class);
        intent.putExtra(DisplayCommentActivity.EXTRA_INT_SELECTED_NOTICE_POSITION, position);
        intent.putExtra(DisplayCommentActivity.EXTRA_BOOL_CLEAR_NOTICES_ON_DESTROY, false);
        startActivity(intent);
    }

    public JSONObject getCommentToDisplay(int position) {
        CommentListAdapter adapter = (CommentListAdapter) getCommentList().getAdapter();
        if (adapter == null) {
            Logx.getInstance().log(5, getClass(), "{0}#getFeedListAdapter() returned null", getClass().getName());
        }
        JSONObject commentJson = adapter.getItem(position);
        if (commentJson == null) {
            Logx.getInstance().log(5, getClass(), "{0}#getItem({1}) returned null", adapter.getClass().getName(), Integer.valueOf(position));
        }
        return commentJson;
    }
}

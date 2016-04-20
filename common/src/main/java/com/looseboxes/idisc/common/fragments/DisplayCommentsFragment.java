package com.looseboxes.idisc.common.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.activities.DisplayCommentActivity;
import com.looseboxes.idisc.common.jsonview.JsonView;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

public class DisplayCommentsFragment extends DisplayCommentFragment implements OnItemClickListener {
    private ListView _clist;

    public JSONObject getSelectedData() {
        return JsonView.getDefaultJsonObject(getContext(), "Your Comments", "Click any of your comments below to view replies to that comment", "comment list description");
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        View reused = findViewById(R.id.commentview_urlbar);
        if (reused != null) {
            reused.setVisibility(View.GONE);
        }
        reused = findViewById(R.id.commentview_buttons_viewgroup);
        if (reused != null) {
            reused.setVisibility(View.GONE);
        }
        reused = findViewById(R.id.commentview_commentinput);
        if (reused != null) {
            reused.setVisibility(View.GONE);
        }
    }

    protected void populateAdapter(CommentListAdapter adapter) {
        List<Map<String, Object>> notices = getNotices();
        if (notices == null || notices.isEmpty()) {
            Popup.show(getContext(), R.string.msg_nofeed, 0);
        }
        for (Map<String, Object> notice : notices) {
            addNotice(adapter, notice);
        }
    }

    protected void addNotice(CommentListAdapter adapter, Map<String, Object> notice) {
        adapter.add((JSONObject) notice.get("comment"));
    }

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
            Popup.show(getContext(), R.string.msg_nofeed, 0);
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
            Logx.log(5, getClass(), "{0}#getFeedListAdapter() returned null", getClass().getName());
        }
        JSONObject commentJson = (JSONObject) adapter.getItem(position);
        if (commentJson == null) {
            Logx.log(5, getClass(), "{0}#getItem({1}) returned null", adapter.getClass().getName(), Integer.valueOf(position));
        }
        return commentJson;
    }
}

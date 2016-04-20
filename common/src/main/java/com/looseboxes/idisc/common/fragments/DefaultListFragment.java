package com.looseboxes.idisc.common.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.EmptyViewProgressBar;
import com.looseboxes.idisc.common.util.Logx;

public class DefaultListFragment extends BaseListFragment {
    private ProgressBar _pb_accessViaGetter;
    private int listIndex;
    private ListItemClickHandler listItemClickHandler;
    private int listTop;
    private String messageToDisplayWhileLoading;
    private long selectedId;
    private int selectedPosition;

    public interface ListItemClickHandler {
        boolean isDualPaneMode();

        boolean isDualPaneVisible();

        void onListItemClick(int i, long j, boolean z);

        void onListItemClick(boolean z);
    }

    public DefaultListFragment() {
        this.selectedPosition = -1;
        this.selectedId = -1;
    }

    public int getContentView() {
        return R.layout.listfragment_list;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.listItemClickHandler = (ListItemClickHandler) activity;
            if (this.messageToDisplayWhileLoading == null) {
                this.messageToDisplayWhileLoading = activity.getString(R.string.msg_loading);
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + " must implement " + ListItemClickHandler.class.getName());
        }
    }

    public void onDetach() {
        super.onDetach();
        this.listItemClickHandler = null;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        this.listItemClickHandler.onListItemClick(position, id, true);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.listItemClickHandler.isDualPaneMode()) {
            getListView().setChoiceMode(1);
        }
        addEmptyViewProgressBar(savedInstanceState);
    }

    protected void addEmptyViewProgressBar(Bundle savedInstanceState) {
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup)this.findViewById(android.R.id.content);
        if(root != null && this.getEmptyViewProgressBar() != null) {
            root.addView(this.getEmptyViewProgressBar());
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.selectedPosition != -1) {
            outState.putInt("selectedPosition", this.selectedPosition);
        }
        if (this.selectedId != -1) {
            outState.putLong("selectedId", this.selectedId);
        }
        ListView listView = getListView();
        this.listIndex = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int i = 0;
        if (v != null) {
            i = v.getTop() - listView.getPaddingTop();
        }
        this.listTop = i;
        outState.putInt("listIndex", this.listIndex);
        outState.putInt("listTop", this.listTop);
        Logx.log(Log.DEBUG, this.getClass(), "SAVING:: List index: {0}, list top: {1}, selected pos: {2}, selected id: {3}",
                this.listIndex, this.listTop, this.selectedPosition, this.selectedId);
    }

    public void onViewStateRestored(Bundle inState) {
        super.onViewStateRestored(inState);
        if (inState != null) {
            this.selectedPosition = inState.getInt("selectedPosition", -1);
            this.selectedId = inState.getLong("selectedId", -1);
            this.listIndex = inState.getInt("listIndex", -1);
            this.listTop = inState.getInt("listTop", -1);
        }
    }

    public void onResume() {
        super.onResume();
        restoreViewStateFromRestoredInstanceState(true);
    }

    public void restoreViewStateFromRestoredInstanceState(boolean mayDisplayAdvert) {
Logx.log(Log.DEBUG, this.getClass(), "RESTORING:: List index: {0}, list top: {1}, selected pos: {2}, selected id: {3}",
        this.listIndex, this.listTop, this.selectedPosition, this.selectedId);
        if (!(this.listIndex == -1 || this.listTop == -1)) {
            getListView().setSelectionFromTop(this.listIndex, this.listTop);
        }
        if (!this.listItemClickHandler.isDualPaneMode()) {
            return;
        }
        if (this.selectedPosition != -1 || this.selectedId != -1) {
            this.listItemClickHandler.onListItemClick(this.selectedPosition, this.selectedId, mayDisplayAdvert);
        }
    }

    public ProgressBar getEmptyViewProgressBar() {
        return getEmptyViewProgressBar(true);
    }

    @TargetApi(23)
    public ProgressBar getEmptyViewProgressBar(boolean create) {
        if (this._pb_accessViaGetter == null && create) {
            Logx.log(Log.DEBUG, getClass(), "Creating empty view progress");
            this._pb_accessViaGetter = new EmptyViewProgressBar(getContext(), this.messageToDisplayWhileLoading);
            this._pb_accessViaGetter.setIndeterminate(true);
            getListView().setEmptyView(this._pb_accessViaGetter);
        }
        return this._pb_accessViaGetter;
    }

    public int getSelectedPosition() {
        return this.selectedPosition;
    }

    public long getSelectedId() {
        return this.selectedId;
    }

    public int getListIndex() {
        return this.listIndex;
    }

    public int getListTop() {
        return this.listTop;
    }

    public String getMessageToDisplayWhileLoading() {
        return this.messageToDisplayWhileLoading;
    }

    public void setMessageToDisplayWhileLoading(String messageToDisplayWhileLoading) {
        this.messageToDisplayWhileLoading = messageToDisplayWhileLoading;
    }
}

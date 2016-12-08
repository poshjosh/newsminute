package com.looseboxes.idisc.common.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bc.android.core.fragments.BaseListFragment;
import com.bc.android.core.ui.ListState;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

public abstract class BaseListFragmentImpl extends BaseListFragment {

    private View root;
    private String messageToDisplayWhileLoading;
    private ListItemClickHandler listItemClickHandler;

    public interface ListItemClickHandler {

        boolean isDualPaneMode();

        boolean isDualPaneVisible();

        void onListItemClick(int position, long id, boolean mayDisplayAdverts);
    }

    class ListStateImpl extends ListState{
        ListStateImpl(String id) {
            super(id);
        }
        @Override
        public boolean scrollToLastSavedPosition(ListView listView) {

            boolean restored = super.scrollToLastSavedPosition(listView);

            if(restored && listItemClickHandler.isDualPaneMode()) {

                final int selectedPos = this.getSelectedPosition();
                final long selectedId = this.getSelectedItemId();

                if (selectedPos != -1 || selectedId != -1) {

                    listItemClickHandler.onListItemClick(selectedPos, selectedId, true);
                }
            }

            return restored;
        }
    }

    public BaseListFragmentImpl() { }

    public abstract int getContentView();

    public void update(Bundle bundle, boolean clearPrevious) {
        NewsminuteUtil.update(this, bundle, clearPrevious);
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

        try {
            ListState listState = this.getListState();
            if(listState == null) {
                final String ID_STRING = this.listItemClickHandler.getClass().getName();
                Logx.getInstance().log(Log.DEBUG, this.getClass(), "{0} ID: {1}", ListState.class.getName(), ID_STRING);
                this.setListState(new ListStateImpl(ID_STRING));
            }
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(this.getContentView(), null, false);
        return this.root;
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.listItemClickHandler.isDualPaneMode()) {
            getListView().setChoiceMode(1);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.root = null;
    }

    public void onDetach() {
        super.onDetach();
        this.listItemClickHandler = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        this.listItemClickHandler.onListItemClick(position, id, true);
    }

    @Nullable
    public View findViewById(@IdRes int id) {
        View v = this.findView();
        return v == null ? null : v.findViewById(id);
    }

    public View findView() {
        return this.root != null ? this.root : this.getView();
    }

    public String getMessageToDisplayWhileLoading() {
        return this.messageToDisplayWhileLoading;
    }

    public void setMessageToDisplayWhileLoading(String messageToDisplayWhileLoading) {
        this.messageToDisplayWhileLoading = messageToDisplayWhileLoading;
    }
}

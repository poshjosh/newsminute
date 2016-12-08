package com.looseboxes.idisc.common.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bc.android.core.ui.ListState;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.handlers.FeedListDisplayHandler;
import com.looseboxes.idisc.common.listeners.ListOnScrollListener;
import com.looseboxes.idisc.common.listeners.VerticalScrollListener;

import org.json.simple.JSONObject;

import java.util.Set;

public class DefaultListFragment extends BaseListFragmentImpl {

    private boolean attemptedScrollToItemToDisplayOnResume;

    @Nullable
    private JsonListItemClickHandler listItemClickHandler;

    private FeedListDisplayHandler feedListDisplayHandler;

    public interface JsonListItemClickHandler extends BaseListFragmentImpl.ListItemClickHandler, VerticalScrollListener {

        JSONObject getItemToScrollToOnResume();

        Set<String> getSearchPhrases();
    }

    class ScrollListener extends ListOnScrollListener {
        ScrollListener() { }
        @Override
        public void onExceedBottom() {
            super.onExceedBottom();
            if(listItemClickHandler != null) {
                listItemClickHandler.onExceedBottom();
            }
        }
        @Override
        public void onReachedBottom() {
            super.onReachedBottom();
            if(listItemClickHandler != null) {
                listItemClickHandler.onReachedBottom();
            }
        }
        @Override
        public void onScrollStopped() {

            super.onScrollStopped();

            DefaultListFragment.this.saveListState();

            if(listItemClickHandler != null) {

                listItemClickHandler.onScrollStopped();
            }
        }
        @Override
        public void onExceedTop() {
            super.onExceedTop();
            if(listItemClickHandler != null) {
                listItemClickHandler.onExceedTop();
            }
        }
        @Override
        public void onReachedTop() {
            super.onReachedTop();
            if(listItemClickHandler != null) {
                listItemClickHandler.onReachedTop();
            }
        }
    }

    public DefaultListFragment() { }

    public int getContentView() {
        return R.layout.list;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.listItemClickHandler = (JsonListItemClickHandler) activity;
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        this.feedListDisplayHandler = this.createFeedListDisplayHandler();

        final ListState listState = this.getListState();
        if(listState != null) {
            this.feedListDisplayHandler.getJsonListAdapter().registerDataSetObserver(listState);
        }

        this.setListAdapter(this.feedListDisplayHandler.getJsonListAdapter());

    }

    protected FeedListDisplayHandler createFeedListDisplayHandler() {

        return new FeedListDisplayHandler(this.getListView(), listItemClickHandler.getSearchPhrases(), new ScrollListener());
    }

    /**
     * <p><b>Comments culled from super class</b></p>
     * Default implementation does nothing. Override to scroll to any item the user selected
     * (usually from a notification) to arrive at this user interface. This method is called
     * from method {@link #onResume() onResume()} and will be preferred over scrolling to any
     * previously saved list-state if this method returns <tt>true</tt> (which indicates that
     * the operation was successful).
     *
     * @return true if the operation was successful, false otherwise
     */
    @Override
    public boolean scrollToItemToDisplayOnResume() {

        if(this.attemptedScrollToItemToDisplayOnResume) {

            return false;

        }else {

            JSONObject item = this.listItemClickHandler == null ? null : this.listItemClickHandler.getItemToScrollToOnResume();

            if (item == null) {

                return false;

            } else {

                this.attemptedScrollToItemToDisplayOnResume = true;

                return this.scrollTo(item);
            }
        }
    }

    public boolean scrollTo(JSONObject feed) {

        boolean scrolledToLastNotice = false;
        if (feed != null) {
            int pos = this.feedListDisplayHandler.getJsonListAdapter().getPosition(feed);
            if (pos != -1) {
                scrollTo(pos);
                scrolledToLastNotice = true;
            }
        }

        return scrolledToLastNotice;
    }

    public FeedListDisplayHandler getFeedListDisplayHandler() {
        return feedListDisplayHandler;
    }
}

package com.looseboxes.idisc.common.listeners;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.bc.android.core.util.Logx;

public class ListOnScrollListener implements ListView.OnScrollListener, VerticalScrollListener {

    private boolean disabled;
    private int totalItemCount;
    private ScrollPosition scrollPosition;

    private int lastFirstVisible;
    private int lastScrollState;
    private ScrollPosition lastScrollPosition;

    public enum ScrollPosition {
        atTop,
        atBottom,
        betweenTopAndBottom,
        exceededTop,
        exceededBottom
    }

    public ListOnScrollListener() {
        this.lastFirstVisible = -1;
        this.lastScrollState = -1;
        this.lastScrollPosition = null;
    }

    public void reset() {
        this.totalItemCount = 0;
        this.scrollPosition = null;
        this.lastFirstVisible = -1;
        this.lastScrollState = -1;
        this.lastScrollPosition = null;
    }

    @Override
    public void onReachedTop() {
        log("Reached top");
    }

    @Override
    public void onExceedTop() {
        log("Exceeded top");
    }

    public void onScrollStarted() { log("Started"); }

    public void onScrollStopped() { log("Stopped"); }

    @Override
    public void onReachedBottom() {
        log("Reached bottom");
    }

    @Override
    public void onExceedBottom() {
        log("Exceeded bottom");
    }

    public ScrollPosition getScrollPosition() {
        return this.scrollPosition;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isAtTop() {
        return this.scrollPosition == ScrollPosition.atTop;
    }

    public boolean isAtBottom() {
        return this.scrollPosition == ScrollPosition.atBottom;
    }

    public boolean isExceededTop() {
        return this.scrollPosition == ScrollPosition.exceededTop;
    }

    public boolean isExceededBottom() {
        return this.scrollPosition == ScrollPosition.exceededBottom;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.totalItemCount = totalItemCount;
        doOnScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    private void doOnScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(this.disabled) {
            return;
        }
        if (firstVisibleItem == 0) {
            View firstChild = view.getChildAt(0);
            if (firstChild != null) {
                int firstChildTop = firstChild.getTop();
                int viewTop = view.getTop();
                if (firstChildTop == viewTop) {
                    if (this.scrollPosition != ScrollPosition.atTop) {
                        this.scrollPosition = ScrollPosition.atTop;
                        onReachedTop();
                    }
                } else if (firstChildTop < viewTop && this.scrollPosition != ScrollPosition.betweenTopAndBottom) {
                    this.scrollPosition = ScrollPosition.betweenTopAndBottom;
                }
            }
        } else if (firstVisibleItem > 0 && this.scrollPosition != ScrollPosition.betweenTopAndBottom) {
            this.scrollPosition = ScrollPosition.betweenTopAndBottom;
        }
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            View lastChild = view.getChildAt(view.getChildCount() - 1);
            if (lastChild != null) {
                int lastChildBottom = lastChild.getBottom();
                int viewBottom = view.getBottom();
                if (lastChildBottom == viewBottom) {
                    if (this.scrollPosition != ScrollPosition.atBottom) {
                        this.scrollPosition = ScrollPosition.atBottom;
                        onReachedBottom();
                    }
                } else if (lastChildBottom > viewBottom && this.scrollPosition != ScrollPosition.betweenTopAndBottom) {
                    this.scrollPosition = ScrollPosition.betweenTopAndBottom;
                }
            }
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {

        doOnScrollStateChanged(view, scrollState);
    }

    private void doOnScrollStateChanged(AbsListView view, int scrollState) {

        if(this.disabled) {
            return;
        }

//Logx.getInstance().log(Log.VERBOSE, this.getClass(),
//"State: "+scrollState+", pos: "+this.scrollPosition+
//", scrolling up: "+this.isScrollingUp(view)+", down: "+this.isScrollingDown(view)+
//", top: "+this.isAtTop()+", top-item: "+this.isAtTopItem(view)+
//", bottom: "+this.isAtBottom()+", bottom-item: " + this.isAtBottomItem(view));

        if(scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {

            if(lastScrollState == ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL ||
                    lastScrollState == ListView.OnScrollListener.SCROLL_STATE_FLING) {

                if(lastScrollPosition == ScrollPosition.atTop) {
                    scrollPosition = ScrollPosition.exceededTop;
                    this.onExceedTop();
                }else if(lastScrollPosition == ScrollPosition.atBottom){
                    scrollPosition = ScrollPosition.exceededBottom;
                    this.onExceedBottom();
                }
            }

            this.onScrollStopped();
        }

        this.lastFirstVisible = view.getFirstVisiblePosition();
        this.lastScrollState = scrollState;
        this.lastScrollPosition = this.scrollPosition;
    }

    private boolean isScrollingUp(AbsListView view) {
        return this.lastFirstVisible > view.getFirstVisiblePosition();
    }

    private boolean isScrollingDown(AbsListView view) {
        return this.lastFirstVisible < view.getFirstVisiblePosition();
    }

    private boolean isAtTopItem(AbsListView view) {
        final int currentFirstVisible = view.getFirstVisiblePosition();
        return currentFirstVisible == 0;
    }

    private boolean isAtBottomItem(AbsListView view) {
        final int currentLastVisible = view.getLastVisiblePosition();
        return (this.totalItemCount > 0 && currentLastVisible == this.totalItemCount - 1);
    }

    private void log(String message) {
        Logx.getInstance().log(Log.VERBOSE, getClass(), message);
    }
}

package com.looseboxes.idisc.common.listeners;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.looseboxes.idisc.common.util.Logx;

public class ListOnScrollListener implements OnScrollListener {
    private boolean disabled;
    private int lastFirstVisible;
    private ScrollPosition scrollPosition;
    private int totalItemCount;

    public enum ScrollPosition {
        atTop,
        atBottom,
        betweenTopAndBottom,
        exceededTop,
        exceededBottom
    }

    public void reset() {
        this.scrollPosition = null;
        this.lastFirstVisible = -1;
    }

    public ListOnScrollListener() {
        this.lastFirstVisible = -1;
    }

    public void onReachedTop() {
        log("Reached top");
    }

    public void onExceedTop() {
        log("Exceeded top");
    }

    public void onReachedBottom() {
        log("Reached bottom");
    }

    public void onExceedBottom() {
        log("Exceeded bottom");
    }

    public ScrollPosition getScrollPosition() {
        return this.scrollPosition;
    }

    private void log(String key) {
        Logx.log(Log.DEBUG, getClass(), key);
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
        int currentFirstVisible = view.getFirstVisiblePosition();
        int currentLastVisible = view.getLastVisiblePosition();
        if (this.lastFirstVisible < currentFirstVisible) {
            if (isExceededTop() || isExceededBottom()) {
                this.scrollPosition = null;
            }
        } else if (this.lastFirstVisible > currentFirstVisible) {
            if (isExceededTop() || isExceededBottom()) {
                this.scrollPosition = null;
            }
        } else if (currentFirstVisible == 0 || isAtTop()) {
            this.scrollPosition = ScrollPosition.exceededTop;
            onExceedTop();
        } else if ((this.totalItemCount > 0 && currentLastVisible == this.totalItemCount - 1) || isAtBottom()) {
            this.scrollPosition = ScrollPosition.exceededBottom;
            onExceedBottom();
        }
        this.lastFirstVisible = currentFirstVisible;
    }
}

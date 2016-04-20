package com.looseboxes.idisc.common.handlers;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.FeedListAdapter;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;

public class FeedDisplayHandler implements OnScrollListener {
    private final Context context;
    private boolean disabled;
    private int lastFirstVisible;
    private final ListView listView;
    private final FeedListAdapter listViewAdapter;
    private ScrollPosition scrollPosition;
    private int totalItemCount;

    public enum ScrollPosition {
        atTop,
        atBottom,
        betweenTopAndBottom,
        exceededTop,
        exceededBottom
    }

    public FeedDisplayHandler(Context context, ListView listView, FeedListAdapter listViewAdapter) {
        this.lastFirstVisible = -1;
        log(2, "In FeedDisplayHandler constructor");
        this.context = context;
        this.listViewAdapter = listViewAdapter;
        this.listView = listView;
        this.listView.setAdapter(listViewAdapter);
        this.listView.setOnScrollListener(this);
        log(2, "Set onscroll listener for list view");
    }

    public void displayLoading(boolean newer) {
        displayMessage(getMessage(newer), 1, newer);
    }

    private String getMessage(boolean newer) {
        return newer ? this.context.getString(R.string.msg_loadingnewer) : this.context.getString(R.string.msg_loadingolder);
    }

    public void displayMessage(String message, int length, boolean top) {
        try {
            Toast toast = Toast.makeText(this.context, message, length);
            int[] location = new int[2];
            this.listView.getLocationOnScreen(location);
            if (top) {
                toast.setGravity(49, location[0], location[1]);
            } else {
                toast.setGravity(81, location[0], location[1]);
            }
            toast.show();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    public Collection preDisplayFeeds(Collection feeds) {
        return feeds;
    }

    public void postDisplayFeeds(Collection feeds) {
    }

    public synchronized void displayFeeds(Collection<JSONObject> feeds) {
        Collection updated = preDisplayFeeds(feeds);
        doDisplayFeeds(updated);
        postDisplayFeeds(updated);
    }

    public boolean isEmpty() {
        return this.listViewAdapter.isEmpty();
    }

    public synchronized void clear() {
        try {
            setDisabled(true);
            reset();
            getFeedDisplayArray().clear();
            this.listViewAdapter.notifyDataSetChanged();
            setDisabled(false);
        } catch (Throwable th) {
            setDisabled(false);
        }
    }

    protected synchronized void doDisplayFeeds(Collection<JSONObject> toDisplay) {
        try {
            clear();
            setDisabled(true);
            getFeedDisplayArray().addAll(toDisplay);
            this.listViewAdapter.notifyDataSetChanged();
            setDisabled(false);
            setLastDisplayTime(System.currentTimeMillis());
            if (this.listViewAdapter.getEmphasisView() != null) {
            }
        } catch (Throwable th) {
            setDisabled(false);
        }
    }

    private void setLastDisplayTime(long lval) {
        Pref.setLong(this.context, getLastDisplayTimePreferenceKey(), lval);
    }

    public long getLastDisplayTime() {
        return Pref.getLong(this.context, getLastDisplayTimePreferenceKey(), -1);
    }

    private static String getLastDisplayTimePreferenceKey() {
        return FeedDisplayHandler.class.getName() + ".lastDisplayTime.long";
    }

    public List<JSONObject> getFeedDisplayArray() {
        return this.listViewAdapter == null ? null : this.listViewAdapter.getFeedDisplayArray();
    }

    public void reset() {
        this.scrollPosition = null;
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

    private void log(int priority, Object msg) {
        Logx.log(priority, getClass(), msg);
    }

    private void log(int priority, String msg, Object... args) {
        Logx.log(priority, getClass(), msg, args);
    }
}

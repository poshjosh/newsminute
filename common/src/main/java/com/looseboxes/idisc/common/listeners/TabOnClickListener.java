package com.looseboxes.idisc.common.listeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.BookmarksActivity;
import com.looseboxes.idisc.common.activities.FavoritesActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.SelectCategoriesActivity;
import com.looseboxes.idisc.common.activities.SelectInfoActivity;
import com.looseboxes.idisc.common.activities.SelectSourcesActivity;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;

public class TabOnClickListener implements OnClickListener {
    private Class<? extends Activity> activityClass;

    public TabOnClickListener(Class<? extends Activity> activityClass) {
        this.activityClass = activityClass;
    }

    public void onClick(View v) {
        int tabId = v.getId();
        Context context = v.getContext();
        try {
            Class targetActivityClass = getActivityClassForTabId(tabId);
            if (targetActivityClass != this.activityClass) {
                context.startActivity(new Intent(context, targetActivityClass));
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
            Popup.show(context, context.getString(getErrorMessageIdForTabId(tabId)), 1);
        }
    }

    public int getErrorMessageIdForTabId(int id) {
        if (id == R.id.tabs_all || id == R.string.msg_all) {
            return R.string.err_feedfailed;
        }
        if (id == R.id.tabs_categories || id == R.string.msg_categories) {
            return R.string.err;
        }
        if (id == R.id.tabs_sources || id == R.string.msg_sources) {
            return R.string.err;
        }
        if (id == R.id.tabs_bookmarks || id == R.string.msg_bookmarks) {
            return R.string.err_feedfailed;
        }
        if (id == R.id.tabs_favorites || id == R.string.msg_favorites) {
            return R.string.err_feedfailed;
        }
        if (id == R.id.tabs_headlines || id == R.string.msg_headlines) {
            return R.string.err_feedfailed;
        }
        if (id == R.id.tabs_info || id == R.string.msg_info) {
            return R.string.err;
        }
        return R.string.err;
    }

    public Class<? extends Activity> getActivityClassForTabId(int id) {
        if (id == R.id.tabs_all || id == R.string.msg_all) {
            return MainActivity.class;
        }
        if (id == R.id.tabs_categories || id == R.string.msg_categories) {
            return SelectCategoriesActivity.class;
        }
        if (id == R.id.tabs_sources || id == R.string.msg_sources) {
            return SelectSourcesActivity.class;
        }
        if (id == R.id.tabs_bookmarks || id == R.string.msg_bookmarks) {
            return BookmarksActivity.class;
        }
        if (id == R.id.tabs_favorites || id == R.string.msg_favorites) {
            return FavoritesActivity.class;
        }
        if (id == R.id.tabs_headlines || id == R.string.msg_headlines) {
            return HeadlinesActivity.class;
        }
        if (id == R.id.tabs_info || id == R.string.msg_info) {
            return SelectInfoActivity.class;
        }
        Logx.log(6, getClass(), "No Activity class defined for id: " + id);
        throw new IllegalArgumentException("Unexpected id: " + id);
    }

    public Class<? extends Activity> getActivityClass() {
        return this.activityClass;
    }

    public void setActivityClass(Class<? extends Activity> activityClass) {
        this.activityClass = activityClass;
    }
}

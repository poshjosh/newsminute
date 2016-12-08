package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.BookmarksActivity;
import com.looseboxes.idisc.common.activities.FavoritesActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.SelectCategoriesActivity;
import com.looseboxes.idisc.common.activities.SelectInfoActivity;
import com.looseboxes.idisc.common.activities.SelectSourcesActivity;

/**
 * Created by Josh on 8/2/2016.
 */
public class TabManager {

    public String getTitleForTabId(Context context, @IdRes int id) {
        int stringResourceId;
        if (id == R.id.tabs_all) {
            stringResourceId = R.string.msg_all;
        } else if (id == R.id.tabs_categories) {
            stringResourceId = R.string.msg_categories;
        } else if (id == R.id.tabs_sources) {
            stringResourceId = R.string.msg_sources;
        } else if (id == R.id.tabs_bookmarks) {
            stringResourceId = R.string.msg_bookmarks;
        } else if (id == R.id.tabs_favorites) {
            stringResourceId = R.string.msg_favorites;
        } else if (id == R.id.tabs_headlines) {
            stringResourceId = R.string.msg_headlines;
        } else if (id == R.id.tabs_info) {
            stringResourceId = R.string.msg_info;
        } else {
            Logx.getInstance().log(6, getClass(), "No Activity class defined for id: " + id);
            throw new IllegalArgumentException("Unexpected id: " + id);
        }
        return context.getString(stringResourceId);
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
        Logx.getInstance().log(6, getClass(), "No Activity class defined for id: " + id);
        throw new IllegalArgumentException("Unexpected id: " + id);
    }
}

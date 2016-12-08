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
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.handlers.TabManager;

public class TabOnClickListener extends TabManager implements OnClickListener {
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
            Logx.getInstance().log(getClass(), e);
            Popup.getInstance().show(context, context.getString(getErrorMessageIdForTabId(tabId)), 1);
        }
    }

    public Class<? extends Activity> getActivityClass() {
        return this.activityClass;
    }

    public void setActivityClass(Class<? extends Activity> activityClass) {
        this.activityClass = activityClass;
    }
}

package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.MenuItem;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.BookmarksActivity;
import com.looseboxes.idisc.common.activities.FavoritesActivity;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import java.util.Collection;

public class MenuHandlerPreferenceFeeds extends AbstractMenuHandler {

    /* renamed from: com.looseboxes.idisc.common.handlers.MenuHandlerPreferenceFeeds.1 */
    class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ PreferenceType val$preferenceType;

        AnonymousClass1(Activity activity, PreferenceType preferenceType) {
            this.val$activity = activity;
            this.val$preferenceType = preferenceType;
        }

        public void onClick(DialogInterface dialog, int which) {
            int size = 0;
            try {
                Popup.show(this.val$activity, "Deleting " + this.val$preferenceType, 1);
                PreferenceFeedsManager pfm = MenuHandlerPreferenceFeeds.this.getPreferenceFeedsManager();
                Collection feeds = pfm.getFeeds();
                if (feeds != null) {
                    size = feeds.size();
                }
                pfm.clear();
                MenuHandlerPreferenceFeeds.this.getActivity().finish();
                Popup.show(this.val$activity, "Deleted " + size + " " + this.val$preferenceType, 0);
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
    }

    public int getMenuResourceId() {
        return R.menu.preferencefeeds_actions;
    }

    public int getShareActionViewId() {
        return -1;
    }

    public int getLoginActionViewId() {
        return -1;
    }

    public int getSearchActionViewId() {
        return -1;
    }

    public int getAboutActionViewId() {
        return -1;
    }

    public int getSettingsActionViewId() {
        return -1;
    }

    public int getForgotpasswordActionViewId() {
        return -1;
    }

    public int getSignupActionViewId() {
        return -1;
    }

    public int getInviteActionViewId() {
        return -1;
    }

    public int getActivateActionViewId() {
        return -1;
    }

    public void onOptionsItemSelected(MenuItem item, Bundle bundle) {
        if (item.getItemId() == R.id.preferencefeeds_actions_delete) {
            PreferenceType preferenceType = getPreferenceType();
            Activity activity = getActivity();
            Popup.alert(activity, "Delete all " + preferenceType + "?", new AnonymousClass1(activity, preferenceType));
            return;
        }
        throw new IllegalArgumentException("Unexpected MenuItem: " + item.getTitle());
    }

    private PreferenceFeedsManager getPreferenceFeedsManager() {
        return App.getPreferenceFeedsManager(getActivity(), getPreferenceType());
    }

    private PreferenceType getPreferenceType() {
        if (getActivity().getClass() == BookmarksActivity.class) {
            return PreferenceType.bookmarks;
        }
        if (getActivity().getClass() == FavoritesActivity.class) {
            return PreferenceType.favorites;
        }
        throw new IllegalArgumentException("Unexpected activity " + getActivity().getClass().getName() + " for " + MenuHandler.class.getName());
    }
}

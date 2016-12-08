package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.MenuItem;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.BookmarksActivity;
import com.looseboxes.idisc.common.activities.FavoritesActivity;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedManager;

import java.util.Collection;

public class MenuHandlerPreferenceFeeds extends AbstractMenuHandler {

    class MenuHandlerPreferenceFeedsOnClickListener implements OnClickListener {

        final MenuHandlerPreferenceFeeds ref;

        MenuHandlerPreferenceFeedsOnClickListener(MenuHandlerPreferenceFeeds ref) {
            this.ref = ref;
        }

        public void onClick(DialogInterface dialog, int which) {

            try {

                final Activity activity = ref.getActivity();
                final PreferenceType preferenceType = ref.getPreferenceType();
                PreferencefeedManager pfm = new PreferencefeedManager(activity, preferenceType, true, null);

                Popup.getInstance().show(activity, "Deleting " + preferenceType, 1);

                final Collection feeds = pfm.getFeeds();
                final int sizeBeforeDelete = feeds==null?null:feeds.size();

                pfm.clear();

                final boolean ignoreSchedule = true;

                pfm.update(ignoreSchedule);

                final int deleted = sizeBeforeDelete - pfm.size();

                Popup.getInstance().show(activity, "Deleted " + deleted + " " + preferenceType, 0);

                activity.finish();

            } catch (Exception e) {

                Logx.getInstance().log(getClass(), e);
            }
        }
    }

    public MenuHandlerPreferenceFeeds() { }

    public int getMenuResourceId() {
        return R.menu.preferencefeeds_actions;
    }

    public void onOptionsItemSelected(MenuItem item, Bundle bundle) {
        if (item.getItemId() == R.id.preferencefeeds_actions_delete) {
            PreferenceType preferenceType = getPreferenceType();
            Activity activity = getActivity();
            String msg = activity.getString(R.string.msg_deleteall_s, preferenceType);
            String neutralMsg = activity.getString(R.string.msg_ok);
            Popup.getInstance().alert(activity, msg, new MenuHandlerPreferenceFeedsOnClickListener(this), neutralMsg);
            return;
        }
        throw new IllegalArgumentException("Unexpected MenuItem: " + item.getTitle());
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

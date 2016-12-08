package com.looseboxes.idisc.common.listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.View;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.asynctasks.Addhotnewsfeedid;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;
import com.looseboxes.idisc.common.preferencefeed.PreferencefeedManager;

import org.json.simple.JSONObject;

public abstract class AbstractFeedContentOptionsButtonListener extends AbstractContentOptionsButtonListener {

    public abstract JSONObject getJsonData();

    public AbstractFeedContentOptionsButtonListener(Context context) {
        super(context);
    }

    public String getUrl() {
        return getFeed().getUrl();
    }

    @Override
    public String getSubject() {
        return getFeed().getHeading(getContext().getString(R.string.err));
    }

    @Override
    public String getText() {
        return getFeed().getText(getContext().getString(R.string.err));
    }

    public void bookmark(View v) {
        updateUserPreferenceLongFromFeedid(PreferenceType.bookmarks, v);
    }

    public void favorite(View v) {

        final boolean added = updateUserPreferenceLongFromFeedid(PreferenceType.favorites, v);

        final Context context = this.getContext();

        if(added && User.getInstance().isAdmin(context)) {

            new Addhotnewsfeedid(context, getFeed().getFeedid()).execute();
        }
    }

    private boolean updateUserPreferenceLongFromFeedid(PreferenceType pref, View v) {

        final int updateImgId;

        JSONObject feed = getJsonData();

        PreferencefeedManager pfm = new PreferencefeedManager(getContext(), pref, true, null);

        final boolean contains = pfm.contains(feed);

        if (contains) {

            pfm.remove(feed);

            updateImgId = getDefaultImageId(pref);

        } else {

            pfm.add(feed);

            updateImgId = getClickedImageId(pref);
        }

        v.setBackgroundResource(updateImgId);

        final boolean ignoreSchedule = true;

        pfm.update(ignoreSchedule);

        return !contains;
    }

    public void delete(View v) {

        final Long feedid = this.getId();

        Popup.getInstance().alert(getContext(), R.string.msg_delete, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {

                    Context context = AbstractFeedContentOptionsButtonListener.this.getContext();

                    FeedDownloadManager.addDeletedFeedId(context, feedid);

                    Popup.getInstance().show(context, R.string.msg_deleted, 0);

                    Intent intent = new Intent(context, MainActivity.class);

                    intent.putExtra(MainActivity.EXTRA_BOOLEAN_CLEAR_PREVIOUS, true);

                    context.startActivity(intent);

                } catch (Exception e) {
                    Logx.getInstance().log(getClass(), e);
                }
            }
        }, R.string.msg_ok);
    }

    private int getDefaultImageId(PreferenceType pref) {
        if (pref == PreferenceType.bookmarks) {
            return R.drawable.ic_bookmark_border_black_24dp;
        }
        if (pref == PreferenceType.favorites) {
            return R.drawable.ic_favorite_border_black_24dp;
        }
        throw new UnsupportedOperationException("Unexpected " + PreferenceType.class.getName() + ": " + pref);
    }

    private int getClickedImageId(PreferenceType pref) {
        if (pref == PreferenceType.bookmarks) {
            return R.drawable.ic_bookmark_black_24dp;
        }
        if (pref == PreferenceType.favorites) {
            return R.drawable.ic_favorite_black_24dp;
        }
        throw new UnsupportedOperationException("Unexpected " + PreferenceType.class.getName() + ": " + pref);
    }

    public Long getId() {
        return getFeed().getFeedid();
    }

    private Feed f;
    private Feed getFeed() {
        if (this.f == null) {
            this.f = new Feed(getJsonData());
        }
        return this.f;
    }
}

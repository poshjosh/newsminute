package com.looseboxes.idisc.common.listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.View;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import org.json.simple.JSONObject;

public abstract class AbstractFeedContentOptionsButtonListener extends AbstractContentOptionsButtonListener {
    private Feed f;

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
        updateUserPreferenceLongFromFeedid(PreferenceType.favorites, v);
    }

    private void updateUserPreferenceLongFromFeedid(PreferenceType pref, View v) {
        int updateImgId;
        JSONObject feed = getJsonData();
        PreferenceFeedsManager feedsManager = App.getPreferenceFeedsManager(getContext(), pref);
        if (feedsManager.contains(feed)) {
            feedsManager.remove(feed);
            updateImgId = getDefaultImageId(pref);
        } else {
            feedsManager.add(feed);
            updateImgId = getClickedImageId(pref);
        }
        v.setBackgroundResource(updateImgId);
    }

    public void delete(View v) {
        Popup.alert(getContext(), "Delete?", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Context context = AbstractFeedContentOptionsButtonListener.this.getContext();
                    FeedDownloadManager.addDeletedFeedId(context, AbstractFeedContentOptionsButtonListener.this.getId());
                    Popup.show(context, R.string.msg_deleted, 0);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_BOOLEAN_CLEAR_PREVIOUS, true);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Logx.log(getClass(), e);
                }
            }
        });
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

    private Feed getFeed() {
        if (this.f == null) {
            this.f = new Feed(getJsonData());
        }
        return this.f;
    }
}

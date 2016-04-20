package com.looseboxes.idisc.common.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.asynctasks.UploadComment;
import com.looseboxes.idisc.common.jsonview.Comment;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.listeners.AbstractFeedContentOptionsButtonListener;
import com.looseboxes.idisc.common.listeners.ContentOptionsButtonListener;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.apache.harmony.awt.datatransfer.DataProvider;
import org.json.simple.JSONObject;

public abstract class AbstractDisplayFeedFragment extends BrowserFragment {

    private TextView _ci;
    private ListView _clist;
    private ContentOptionsButtonListenerImpl _cobl;
    private Feed _f;

    private class ContentOptionsButtonListenerImpl extends AbstractFeedContentOptionsButtonListener {
        private ContentOptionsButtonListenerImpl() {
            super(AbstractDisplayFeedFragment.this.getContext());
        }
        @Override
        public void onClick(View v) {
            Logx.log(Log.DEBUG, this.getClass(), "onClick(View)");
            this.updateButtonState(v, true);
            super.onClick(v);
        }

        @TargetApi(14)
        private void updateButtonState(View v, boolean selected) {
            Logx.log(Log.DEBUG, this.getClass(), "updateButtonState(View, boolean)");
            v.setActivated(selected);
            if (App.isAcceptableVersion(this.getContext(), 14)) {
                v.setHovered(selected);
            }
            v.setPressed(selected);
            v.setSelected(selected);
        }

        public void browseToSource(View v) {
            String url = getUrl();
            try {
                if (url == null) {
                    Popup.show(this.getContext(), R.string.msg_nolink, Toast.LENGTH_SHORT);
                } else {
                    // First display loading message before the actual loading begins
                    WebView webView = getWebView();
                    webView.loadDataWithBaseURL(null, getString(R.string.msg_loading), DataProvider.TYPE_HTML, "utf-8", null);
                    AbstractDisplayFeedFragment.this.loadData(webView, url);
                }
            } catch (Exception e) {
                Popup.show(this.getContext(), url == null ? "Error accessing URL of feed" : "Error browsing: " + url, Toast.LENGTH_SHORT);
                Logx.debug(getClass(), e);
            }
        }
        public JSONObject getJsonData() {
            return AbstractDisplayFeedFragment.this.getSelectedData();
        }
    }

    public abstract int getCommentInputResourceId();

    public abstract int getCommentListResourceId();

    public abstract JSONObject getSelectedData();

    public String getLinkToDisplay() {
        Feed feed = getSelectedFeed();
        if (feed != null) {
            return feed.getUrl();
        }
        return null;
    }

    public String getShareText() {
        Feed selectedFeed = getSelectedFeed();
        PropertiesManager pm = App.getPropertiesManager(getContext());
        String shareContent = selectedFeed.getHeading(null, pm.getInt(PropertyName.textLengthMedium));
        if (shareContent != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(shareContent).append("\n\nView more at\n\n");
            builder.append(pm.getString(PropertyName.appServiceUrl));
            builder.append("/feed/").append(selectedFeed.getFilename());
            shareContent = builder.toString();
        }
        Logx.debug(getClass(), "Share content: {0}", shareContent);
        return shareContent;
    }

    public String getContentToDisplay() {
        Feed selectedFeed = getSelectedFeed();
        if (selectedFeed == null) {
            return null;
        }
        Logx.log(Log.VERBOSE, getClass(), "Feed text:\n{0}", selectedFeed.getText(null));
        return selectedFeed.getText(null);
    }

    @TargetApi(19) // actually pertains to the super method
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        Popup.promptContentoptionsUsage(this.getContext());
    }


    public void onResume() {
        super.onResume();
        String content = getContentToDisplay();
        if (content == null || content.isEmpty()) {
            Popup.show(getContext(), R.string.msg_nofeed, 1);
        } else if (getSelectedFeed() == null) {
            Popup.show(getContext(), R.string.msg_nofeed, 1);
        } else {
            getCommentList().setAdapter(createCommentListAdapter());
        }
    }

    protected CommentListAdapter createCommentListAdapter() {
        return new CommentListAdapter(getContext(), createComment());
    }

    protected Comment createComment() {
        return new Comment();
    }

    protected void onClearCommentButtonClicked(View v) {
        if(v.getId() != R.id.feedview_clearcomment) {
            throw new IllegalArgumentException();
        }
        getCommentInput().setText(null);
    }

    protected void onSendCommentButtonClicked(View v) {
        if(v.getId() != R.id.feedview_sendcomment) {
            throw new IllegalArgumentException();
        }
        try {
            CharSequence cs = getCommentInput().getText();
            if (cs == null || cs.length() == 0) {
                Popup.show(getContext(), R.string.msg_nocommententered, 0);
                return;
            }
            Feed selectedFeed = getSelectedFeed();
            if (selectedFeed == null) {
                Popup.show(getContext(), R.string.err, 0);
                return;
            }
            UploadComment uploadTask = new UploadComment(getContext(), selectedFeed.getFeedid(), null, cs.toString());
            Popup.show(getContext(), R.string.msg_postingcomment, 0);
            uploadTask.execute();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    public ContentOptionsButtonListener getContentOptionsButtonListener() {
        if (this._cobl == null) {
            this._cobl = new ContentOptionsButtonListenerImpl();
        }
        return this._cobl;
    }

    public ListView getCommentList() {
        if (this._clist == null) {
            this._clist = (ListView) findViewById(getCommentListResourceId());
        }
        return this._clist;
    }

    public TextView getCommentInput() {
        if (this._ci == null) {
            this._ci = (TextView) findViewById(getCommentInputResourceId());
        }
        return this._ci;
    }

    public Feed getSelectedFeed() {
        if (this._f == null) {
            JSONObject feedJson = getSelectedData();
            if (feedJson != null) {
                this._f = new Feed(feedJson);
            }
        }
        return this._f;
    }
}

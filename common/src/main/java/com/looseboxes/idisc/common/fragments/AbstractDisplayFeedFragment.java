package com.looseboxes.idisc.common.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.CommentListAdapter;
import com.looseboxes.idisc.common.asynctasks.UploadComment;
import com.looseboxes.idisc.common.jsonview.Comment;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.listeners.ContentOptionsButtonListener;
import com.looseboxes.idisc.common.listeners.ContentOptionsButtonListenerImpl;
import com.looseboxes.idisc.common.notice.PromptContentOptionsUsage;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

public abstract class AbstractDisplayFeedFragment extends BrowserFragment
        implements ContentOptionsButtonListenerImpl.ListenerDataHandler {

    private TextView _ci;
    private ListView _clist;
    private ContentOptionsButtonListener _cobl;
    private Feed _f;

    public abstract int getCommentInputResourceId();

    public abstract int getCommentListResourceId();

    @Override
    public abstract JSONObject getSelectedData();

    public boolean acceptContentForDisplay(String content) {
        final boolean output;
        final String link = this.getLinkToDisplay();
        if(this.acceptLinkForDisplay(link) && NewsminuteUtil.isPreferredNetworkConnectedOrConnecting(this.getContext())) {
            CommentListAdapter cla = ((CommentListAdapter) this.getCommentList().getAdapter());
            PropertiesManager pm = App.getPropertiesManager(this.getContext());
            final int len_short = pm.getInt(PropertyName.textLengthShort);
            final int maxLen = cla == null ? len_short : cla.getHeadingLength();
            output = content != null && content.length() > maxLen;
        }else{
            output = super.acceptContentForDisplay(content);
        }
        return output;
    }

    @Override
    public String getLinkToDisplay() {
        Feed feed = getSelectedFeed();
        if (feed != null) {
            return feed.getUrl();
        }
        return null;
    }

    @Override
    public String getShareText() {
        Feed selectedFeed = getSelectedFeed();
        PropertiesManager pm = App.getPropertiesManager(getContext());
        String shareContent = selectedFeed.getHeading(null, pm.getInt(PropertyName.textLengthMedium));
        if (shareContent != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(shareContent).append("\n\nView more at\n\n");
            builder.append(pm.getString(PropertyName.appServiceUrl));
            builder.append("/feed/");
            selectedFeed.appendFilename(builder);
            shareContent = builder.toString();
        }
        Logx.getInstance().debug(getClass(), "Share content: {0}", shareContent);
        return shareContent;
    }

    @Override
    public String getContentToDisplay() {
        Feed selectedFeed = getSelectedFeed();
        String output;
        if (selectedFeed == null) {
            output = null;
        }else{
            output = selectedFeed.getText(null);
        }
        Logx.getInstance().log(Log.VERBOSE, getClass(), "Feed text: {0}", output);
        return output;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        new PromptContentOptionsUsage(0.5f, 1, 0).attemptShow(this.getContext());
    }

    public void onResume() {

        super.onResume();

        final String content = getContentToDisplay();

        if (content == null || content.isEmpty()) {
            Popup.getInstance().show(getContext(), R.string.msg_nofeed, 1);
        } else if (getSelectedFeed() == null) {
            Popup.getInstance().show(getContext(), R.string.msg_nofeed, 1);
        } else {
            if(getCommentList() != null) {
                getCommentList().setAdapter(createCommentListAdapter());
            }
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
                Popup.getInstance().show(getContext(), R.string.msg_nocommententered, 0);
                return;
            }
            Feed selectedFeed = getSelectedFeed();
            if (selectedFeed == null) {
                Popup.getInstance().show(getContext(), R.string.err, 0);
                return;
            }
            UploadComment uploadTask = new UploadComment(getContext(), selectedFeed.getFeedid(), null, cs.toString());
            Popup.getInstance().show(getContext(), R.string.msg_postingcomment, 0);
            uploadTask.execute();
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    @Override
    public ContentOptionsButtonListener getContentOptionsButtonListener() {
        Logx.getInstance().log(Log.DEBUG, this.getClass(), "getContentOptionsButtonListener");
        if (this._cobl == null) {
            this._cobl = new ContentOptionsButtonListenerImpl(this.getWebView(), this);
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

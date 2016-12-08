package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

import org.json.simple.JSONArray;

public abstract class CommentDownloadTask extends AbstractReadTask<JSONArray> {

    public CommentDownloadTask(Context context, Object feedid, int offset, int limit) {

        super(context, context.getString(R.string.err_loadingcomments));

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        if (offset > 0) {
            this.addOutputParameter("offset", Integer.toString(offset));
        }
        if (limit > 0) {
            this.addOutputParameter("limit", Integer.toString(limit));
        }
        this.addOutputParameter(FeedhitNames.feedid, feedid.toString());

        setNoUI(true);
    }

    public String getOutputKey() {
        return FileIO.getCommentskey();
    }

    @Override
    protected boolean isNetworkAvailable() {
        return NewsminuteUtil.isPreferredNetworkConnectedOrConnecting(this.getContext());
    }

    @Override
    protected String getNetworkUnavailableMessage() {
        return NewsminuteUtil.getNetworkUnavailableMessage(this.getContext());
    }
}

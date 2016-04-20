package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.CommentNames;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.util.Logx;
import java.util.HashMap;
import java.util.Map;

public class UploadComment extends DefaultReadTask<Object> {
    private final Context context;

    public UploadComment(Context context, Object feedid, String commentSubject, String commentText) {
        Map<String, String> params = new HashMap(24, 0.75f);
        if (!(commentSubject == null || commentSubject.isEmpty())) {
            params.put(CommentNames.commentSubject, commentSubject);
        }
        params.put(CommentNames.commentText, commentText);
        params.put(FeedhitNames.feedid, feedid.toString());
        User.getInstance().addParameters(context, params);
        setOutputParameters(params);
        this.context = context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_uploadingcomments);
    }

    public void onSuccess(Object download) {
        Logx.debug(getClass(), "Comment upload successful. Server response: {0}", download);
        if (!isNoUI()) {
            displayMessage(getContext().getString(R.string.msg_commentuploadsuccessful), 1);
        }
    }

    public Context getContext() {
        return this.context;
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "newcomment";
    }
}

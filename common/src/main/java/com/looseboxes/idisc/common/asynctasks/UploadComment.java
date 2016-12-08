package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.CommentNames;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.bc.android.core.util.Logx;
import java.util.HashMap;
import java.util.Map;

public class UploadComment extends AbstractReadTask<Object> {

    public UploadComment(Context context, Object feedid, String commentSubject, String commentText) {

        super(context, context.getString(R.string.err_uploadingcomments));

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        if (!(commentSubject == null || commentSubject.isEmpty())) {
            this.addOutputParameter(CommentNames.commentSubject, commentSubject);
        }
        this.addOutputParameter(CommentNames.commentText, commentText);
        this.addOutputParameter(FeedhitNames.feedid, feedid.toString());
    }

    public void onSuccess(Object download) {
        Logx.getInstance().debug(getClass(), "Comment upload successful. Server response: {0}", download);
        if (!isNoUI()) {
            displayMessage(getContext().getString(R.string.msg_commentuploadsuccessful), 1);
        }
    }

    public String getOutputKey() {
        return "newcomment";
    }
}

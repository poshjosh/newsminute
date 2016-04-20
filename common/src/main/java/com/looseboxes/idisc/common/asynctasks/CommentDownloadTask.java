package com.looseboxes.idisc.common.asynctasks;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;

public abstract class CommentDownloadTask extends DefaultReadTask<JSONArray> {
    public CommentDownloadTask(Object feedid, int offset, int limit) {
        Map<String, String> params = new HashMap(20, 0.75f);
        if (offset > 0) {
            params.put("offset", Integer.toString(offset));
        }
        if (limit > 0) {
            params.put("limit", Integer.toString(limit));
        }
        params.put(FeedhitNames.feedid, feedid.toString());
        User.getInstance().addParameters(getContext(), params);
        setOutputParameters(params);
        setNoUI(true);
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_loadingcomments);
    }

    public boolean isRemote() {
        return true;
    }

    public String getOutputKey() {
        return FileIO.getCommentskey();
    }

    public String getLocalFilename() {
        return FileIO.getCommentsFilename();
    }
}

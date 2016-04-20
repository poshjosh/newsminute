package com.looseboxes.idisc.common.asynctasks;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;

public abstract class GetUserFeedPreferences extends DefaultReadTask<JSONArray> {
    public GetUserFeedPreferences() {
        Map<String, String> params = new HashMap(20, 0.75f);
        User.getInstance().addParameters(getContext(), params);
        setOutputParameters(params);
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_loading_userpreferences);
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }
}

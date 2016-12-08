package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;

import org.json.simple.JSONArray;

public abstract class GetUserFeedPreferences extends AbstractReadTask<JSONArray> {
    public GetUserFeedPreferences(Context context) {
        super(context, context.getString(R.string.err_loading_userpreferences));
        this.addOutputParameters(User.getInstance().getOutputParameters(context));
    }
}

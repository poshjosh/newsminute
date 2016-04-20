package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.HashMap;
import java.util.Map;

public abstract class FeedDownloadTask extends AbstractFeedReadTask {
    private final Context context;

    public FeedDownloadTask(Context context) {
        this(context, 0);
    }

    public FeedDownloadTask(Context context, long downloadFeedsAfter) {
        init(context, downloadFeedsAfter, App.getPropertiesManager(context).getInt(PropertyName.feedDownloadLimit));
        this.context = context;
    }

    public FeedDownloadTask(Context context, long downloadFeedsAfter, int limit) {
        init(context, downloadFeedsAfter, limit);
        this.context = context;
    }

    private void init(Context context, long downloadFeedsAfter, int limit) {
        Map<String, String> params = new HashMap(20, 0.75f);
        if (downloadFeedsAfter > 0) {
            params.put("after", Long.toString(downloadFeedsAfter));
        }
        params.put("limit", Integer.toString(limit));
        int added = User.getInstance().addParameters(context, params);
        setOutputParameters(params);
    }

    public Context getContext() {
        return this.context;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_loadingfeeds);
    }

    public boolean isRemote() {
        return true;
    }
}

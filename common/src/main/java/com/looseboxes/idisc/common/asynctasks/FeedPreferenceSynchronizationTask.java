package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.util.CachedSet;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;

public abstract class FeedPreferenceSynchronizationTask extends DefaultReadTask<JSONObject> {
    private final Context context;
    private final PreferenceType preferenceType;

    protected abstract void processParsed(List<JSONObject> list, List<JSONObject> list2, List<Long> list3);

    public FeedPreferenceSynchronizationTask(Context context, CachedSet<Long> addfeedids, CachedSet<Long> removefeedids, Set<Long> feedids, PreferenceType type) {
        if ((addfeedids == null || addfeedids.isEmpty()) && ((removefeedids == null || removefeedids.isEmpty()) && (feedids == null || feedids.isEmpty()))) {
            throw new NullPointerException();
        }
        this.context = context;
        this.preferenceType = type;
        Map<String, String> params = new HashMap(24, 0.75f);
        JsonFormat fmt = new JsonFormat();
        if (!(addfeedids == null || addfeedids.isEmpty())) {
            params.put("com.looseboxes.idisc.common." + getOutputKey() + ".feedids_to_add", fmt.toJSONString(addfeedids));
        }
        if (!(removefeedids == null || removefeedids.isEmpty())) {
            params.put("com.looseboxes.idisc.common." + getOutputKey() + ".feedids_to_remove", fmt.toJSONString(removefeedids));
        }
        if (!(feedids == null || feedids.isEmpty())) {
            params.put("com.looseboxes.idisc.common." + getOutputKey() + ".feedids", fmt.toJSONString(feedids));
        }
        params.put("limit", Integer.toString(App.getPropertiesManager(context).getInt(PropertyName.feedDownloadLimit)));
        int added = User.getInstance().addParameters(context, params);
        setOutputParameters(params);
        setNoUI(!App.isVisible());
    }

    public void onSuccess(JSONObject download) {
        if (download == null || download.isEmpty()) {
            processParsed(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        } else {
            processParsed((List) download.get("com.looseboxes.idisc.common." + getOutputKey() + ".addedfeeds"), (List) download.get("com.looseboxes.idisc.common." + getOutputKey() + ".removedfeeds"), (List) download.get("com.looseboxes.idisc.common." + getOutputKey() + ".feedids"));
        }
    }

    public PreferenceType getPreferenceType() {
        return this.preferenceType;
    }

    public String getOutputKey() {
        return FileIO.getSyncFeedidskey(getPreferenceType());
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException();
    }

    public Context getContext() {
        return this.context;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_syncing, new Object[]{getPreferenceType().name()});
    }

    public boolean isRemote() {
        return true;
    }
}

package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;
import com.looseboxes.idisc.common.util.Pref;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONObject;

public abstract class PreferencefeedSyncTask extends AbstractReadTask<JSONObject> {

    private Preferencefeeds preferencefeeds;

    protected abstract void processParsed(List<JSONObject> list, List<JSONObject> list2, List<Long> list3);

    public PreferencefeedSyncTask(Context context, PreferenceType type) {
        this(new Preferencefeeds(context, type));
    }

    public PreferencefeedSyncTask(Preferencefeeds preferencefeeds) {

        super(preferencefeeds.getContext(),
                preferencefeeds.getContext().getString(R.string.err_syncing, new Object[]{preferencefeeds.getPreferenceType().name()}));

        if(preferencefeeds.isVirgin()) {
            throw new IllegalArgumentException();
        }
        this.preferencefeeds = preferencefeeds;

        JsonFormat fmt = new JsonFormat();
        final String outputKey = this.getOutputKey();

        final Set<Long> addfeedids = preferencefeeds.getAddedFeedids();
        if (!(addfeedids == null || addfeedids.isEmpty())) {
            this.addOutputParameter("com.looseboxes.idisc.common." + outputKey + ".feedids_to_add", fmt.toJSONString(addfeedids));
        }

        final Set<Long> removefeedids = preferencefeeds.getDeletedFeedids();
        if (!(removefeedids == null || removefeedids.isEmpty())) {
            this.addOutputParameter("com.looseboxes.idisc.common." + outputKey + ".feedids_to_remove", fmt.toJSONString(removefeedids));
        }

        final Set<Long> feedids = preferencefeeds.getFeedIds();
        if (!(feedids == null || feedids.isEmpty())) {
            this.addOutputParameter("com.looseboxes.idisc.common." + outputKey + ".feedids", fmt.toJSONString(feedids));
        }

        Context context = preferencefeeds.getContext();

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter("limit", Integer.toString(Pref.getFeedDownloadLimit(context)));

        setNoUI(!App.isVisible());
    }

    @Override
    protected void after(String downloaded) {
        super.after(downloaded);
        if(preferencefeeds != null) {
            preferencefeeds.destroy();
            preferencefeeds = null;
        }
    }

    public void onSuccess(JSONObject download) {
        if (download == null || download.isEmpty()) {
            processParsed(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        } else {
            processParsed((List) download.get("com.looseboxes.idisc.common." + getOutputKey() + ".addedfeeds"), (List) download.get("com.looseboxes.idisc.common." + getOutputKey() + ".removedfeeds"), (List) download.get("com.looseboxes.idisc.common." + getOutputKey() + ".feedids"));
        }
    }

    public PreferenceType getPreferenceType() {
        return this.preferencefeeds.getPreferenceType();
    }

    public String getOutputKey() {
        return FileIO.getSyncFeedidskey(getPreferenceType());
    }

    public Preferencefeeds getPreferencefeeds() {
        return preferencefeeds;
    }
}

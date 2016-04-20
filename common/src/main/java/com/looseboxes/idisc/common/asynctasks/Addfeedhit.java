package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.looseboxes.idisc.common.util.Logx;
import java.util.HashMap;
import java.util.Map;

public class Addfeedhit extends DefaultReadTask<Object> {
    private final Context context;
    private Long feedid;
    private long hittime;

    public Addfeedhit(Context context) {
        Map<String, String> params = new HashMap(20, 0.75f);
        int added = User.getInstance().addParameters(context, params);
        setOutputParameters(params);
        this.context = context;
        setNoUI(!User.getInstance().isAdmin(context));
    }

    public void reset() {
        super.reset();
        this.feedid = null;
        this.hittime = 0;
    }

    public void onSuccess(Object download) {
        FeedhitManager.getHitcounts(this.context, true).put(this.feedid, Integer.valueOf(download.toString()));
        Logx.log(Log.DEBUG, getClass(), "Successfully updated hitcount");
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "addfeedhit";
    }

    public Context getContext() {
        return this.context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_updatinghitcount);
    }

    public Long getFeedid() {
        return this.feedid;
    }

    public void setFeedid(Long feedid) {
        if (feedid.longValue() < 1) {
            throw new IllegalArgumentException("Feedid < 1");
        }
        this.feedid = feedid;
        getOutputParameters().put(FeedhitNames.feedid, feedid.toString());
    }

    public long getHittime() {
        return this.hittime;
    }

    public void setHittime(long hittime) {
        if (hittime < 1) {
            throw new IllegalArgumentException("hittime: " + hittime);
        }
        this.hittime = hittime;
        getOutputParameters().put(FeedhitNames.hittime, Long.toString(hittime));
    }
}

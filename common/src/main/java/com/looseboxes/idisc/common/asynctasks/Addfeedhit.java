package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.bc.android.core.util.Logx;

public abstract class Addfeedhit extends AbstractReadTask<Object> {

    public Addfeedhit(Context context, Long feedid, long hittime) {

        super(context, context.getString(R.string.err_updatinghitcount));

        if (feedid.longValue() < 1) {
            throw new IllegalArgumentException("Feedid < 1");
        }
        if (hittime < 1) {
            throw new IllegalArgumentException("hittime < 1");
        }

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter(FeedhitNames.feedid, Long.toString(feedid));

        this.addOutputParameter(FeedhitNames.hittime, Long.toString(hittime));

        setNoUI(!User.getInstance().isAdmin(context));
    }

    public String getOutputKey() {
        return "addfeedhit";
    }
}

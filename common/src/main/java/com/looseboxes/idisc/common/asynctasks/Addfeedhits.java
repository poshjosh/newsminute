package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.util.SparseIntArrayForLongsLimitedSize;
import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.bc.android.core.util.Logx;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

public abstract class Addfeedhits extends AbstractReadTask<Object> {

    public Addfeedhits(Context context, SparseIntArrayForLongsLimitedSize feedIdToHittime) {

        super(context, context.getString(R.string.err_updatinghitcount));

        if (feedIdToHittime == null || feedIdToHittime.size() == 0) {
            throw new NullPointerException();
        }

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        final int size = feedIdToHittime.size();

        List<String> hits = new LinkedList<>();
        for(int i=0; i<size; i++) {
            int key = feedIdToHittime.keyAt(i);
            long val = feedIdToHittime.longValueAt(i);
            hits.add(this.toOutputFormat(key, val));
        }

        this.addOutputParameter("hits", new JsonFormat(false).toJSONString(hits));

        setNoUI(!User.getInstance().isAdmin(context));
    }

    public static String toOutputFormat(long feedid, long hittime) {
        return Long.toString(feedid) + ',' + hittime;
    }

    public String getOutputKey() {
        return "addfeedhits";
    }
}

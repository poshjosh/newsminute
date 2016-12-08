package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.util.Util;
import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;

import java.util.List;

/**
 * Created by Josh on 8/6/2016.
 */
public class Updatereadcomments extends AbstractReadTask<Object> {

    private final Context context;

    public Updatereadcomments(Context context, List<Long> commentids) {

        super(context, context.getString(R.string.err_unexpected));

        Util.requireNonNull(commentids);

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter("commentids", new JsonFormat().toJSONString(commentids));
        this.context = context;
        setNoUI(!User.getInstance().isAdmin(context));
    }

    @Override
    public void onSuccess(Object o) { }

    public String getOutputKey() {
        return "updatereadcomments";
    }
}

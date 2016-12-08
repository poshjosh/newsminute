package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.support.annotation.MainThread;
import android.widget.Toast;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;

/**
 * Created by Josh on 11/20/2016.
 */
public class Addhotnewsfeedid extends AbstractReadTask<Object> {

    private final Long feedid;

    public Addhotnewsfeedid(Context context, Long feedid) {

        super(context, context.getString(R.string.err));

        if (feedid.longValue() < 1) {
            throw new IllegalArgumentException("Feedid < 1");
        }

        this.feedid = feedid;

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        this.addOutputParameter(FeedhitNames.feedid, Long.toString(feedid));

        setNoUI(!User.getInstance().isAdmin(context));
    }

    @Override
    @MainThread
    public void onSuccess(Object o) {
        final Context ctx = this.getContext();
        final String msg = ctx.getString(R.string.msg_add_s_tohotnewsfeedids, Long.toString(this.feedid)) + ':' + ' ' + o;
        Logx.getInstance().debug(this.getClass(), msg);
        if(!this.isNoUI()) {
            Popup.getInstance().show(ctx, msg, Toast.LENGTH_LONG);
        }
    }

    public String getOutputKey() {
        return "addhotnewsfeedid";
    }
}
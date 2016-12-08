package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.io.IOWrapper;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;

public class DownloadToLocalCache<T> extends AbstractReadTask<T> {

    private final Context context;
    private final IOWrapper<T> ioWrapper;
    private final String outputkey;

    public DownloadToLocalCache(Context context, String outputkey, IOWrapper<T> io) {
        super(context, context.getString(R.string.err_loading, new Object[]{outputkey}));
        this.context = context;
        this.outputkey = outputkey;
        this.ioWrapper = io;
    }

    public void onSuccess(T download) {
        Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Download:\n{0}", download);
        this.ioWrapper.setTarget(download);
    }

    public String getOutputKey() {
        return this.outputkey;
    }
}

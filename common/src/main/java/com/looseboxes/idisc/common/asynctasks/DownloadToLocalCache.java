package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.IOWrapper;

public class DownloadToLocalCache<T> extends DefaultReadTask<T> {
    private final Context context;
    private final IOWrapper<T> ioWrapper;
    private final String outputkey;

    public DownloadToLocalCache(Context context, String outputkey, IOWrapper<T> io) {
        this.context = context;
        this.outputkey = outputkey;
        this.ioWrapper = io;
    }

    public void onSuccess(T download) {
        this.ioWrapper.setTarget(download);
    }

    public String getOutputKey() {
        return this.outputkey;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_loading, new Object[]{getOutputKey()});
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Context getContext() {
        return this.context;
    }
}

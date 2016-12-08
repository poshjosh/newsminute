package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.looseboxes.idisc.common.util.Pref;

import org.json.simple.JSONObject;

import java.util.List;

public abstract class FeedDownloadTask extends AbstractReadTask<List<JSONObject>> {

    private static boolean loggedMemory;

    public FeedDownloadTask(Context context) {
        this(context, 0);
    }

    public FeedDownloadTask(Context context, long downloadFeedsAfter) {
        this(context, downloadFeedsAfter, Pref.getFeedDownloadLimit(context));
        setNoUI(!App.isVisible());
    }

    public FeedDownloadTask(Context context, long downloadFeedsAfter, int limit) {

        super(context, context.getString(R.string.err_loadingfeeds));

        this.addOutputParameters(User.getInstance().getOutputParameters(context));

        if (downloadFeedsAfter > 0) {
            this.addOutputParameter("after", Long.toString(downloadFeedsAfter));
        }
        this.addOutputParameter("limit", Integer.toString(limit));
    }

    @Override
    protected boolean isNetworkAvailable() {
        return NewsminuteUtil.isPreferredNetworkConnectedOrConnecting(this.getContext());
    }

    @Override
    protected String getNetworkUnavailableMessage() {
        return NewsminuteUtil.getNetworkUnavailableMessage(this.getContext());
    }

    @Override
    protected void before() {
        super.before();
        if(!loggedMemory) {
            Logx.getInstance().log(Log.INFO, this.getClass(), "BEFORE DOWNLOADING FEEDS. Total memory: {0}, free memory: {1}",
                    Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
        }
    }

    @Override
    protected void after(String downloaded) {
        super.after(downloaded);
        if(!loggedMemory) {
            loggedMemory = true;
            Logx.getInstance().log(Log.INFO, this.getClass(), "AFTER DOWNLOADING FEEDS. Total memory: {0}, free memory: {1}",
                    Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
        }
    }

    public String getOutputKey() {
        return FileIO.getFeedskey();
    }
}

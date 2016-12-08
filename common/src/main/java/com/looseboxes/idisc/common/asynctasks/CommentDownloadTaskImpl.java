package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;

/**
 * Created by Josh on 9/12/2016.
 */
public class CommentDownloadTaskImpl extends CommentDownloadTask {

    public interface ListFace {
        boolean isFragmentAdded();
        ArrayAdapter<JSONObject> getListAdapter();
        TextView getProgressText();
    }

    private int displayed;

    private final ListFace listFace;

    public CommentDownloadTaskImpl(Context context, Object feedid, int offset, int limit, ListFace listFace) {
        super(context, feedid, offset, limit);
        this.listFace = Util.requireNonNull(listFace);
    }

    public void onSuccess(JSONArray download) {
        try {
            Logx.getInstance().log(Log.DEBUG, getClass(), "Processing update");
            ArrayAdapter<JSONObject> adapter = listFace.getListAdapter();
            Iterator iter = download.iterator();
            while (iter.hasNext()) {
                adapter.add((JSONObject)iter.next());
                this.displayed++;
            }
            Logx.getInstance().log(Log.DEBUG, getClass(), "Displayed comments: " + this.displayed);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }

    @Override
    @CallSuper
    @MainThread
    protected void before() {
        super.before();
        try {
            if (this.listFace.getProgressText() != null && this.listFace.isFragmentAdded()) {
                final int percent = this.getProgressPercent();
                this.listFace.getProgressText().setText(this.getContext().getString(R.string.msg_loadingcomments) + ": " + percent + "%");
            }
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
    }

    @Override
    @CallSuper
    @MainThread
    protected void after(String downloaded) {
        super.after(downloaded);
        try {
            if(listFace.getProgressText() != null) {
                if (this.displayed < 1) {
                    listFace.getProgressText().setText(R.string.msg_befirsttocomment);
                } else {
                    listFace.getProgressText().setText(R.string.msg_comments);
                }
            }
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
    }
}

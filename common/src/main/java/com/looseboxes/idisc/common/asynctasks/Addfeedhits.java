package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.looseboxes.idisc.common.util.Logx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.JSONParser;

public class Addfeedhits extends DefaultReadTask<Object> {
    private final Context context;
    private List<String> hits;

    public Addfeedhits(Context context) {
        Map<String, String> params = new HashMap(20, 0.75f);
        int added = User.getInstance().addParameters(context, params);
        setOutputParameters(params);
        this.context = context;
        setNoUI(!User.getInstance().isAdmin(context));
    }

    public void reset() {
        super.reset();
        this.hits = null;
    }

    public void onSuccess(Object download) {
        try {
            if (this.hits != null) {
                this.hits.clear();
            }
            FeedhitManager.getHitcounts(this.context, true).putAll((Map) new JSONParser().parse(download.toString()));
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "addfeedhits";
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

    public List<String> getHits() {
        return this.hits;
    }

    public void setHits(List<String> hits) {
        if (hits == null || hits.isEmpty()) {
            throw new NullPointerException();
        }
        getOutputParameters().put("hits", new JsonFormat().toJSONString(hits));
        this.hits = hits;
    }
}

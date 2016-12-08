package com.looseboxes.idisc.common.jsonview;

import android.content.Context;

import com.looseboxes.idisc.common.R;

import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Josh on 8/2/2016.
 */
public class Notice extends JsonView {

    public Notice() { }

    public Notice(JSONObject source) {
        super(source);
    }

    public String getSummary(Context context) {
        return getSummary(context, getReplies().size(), getReplierScreenName(0));
    }

    private String getSummary(Context context, int repliesCount, String replierScreenname) {
        if (repliesCount == 1) {
            return context.getString(R.string.msg_s_replied, new Object[]{replierScreenname});
        }
        return context.getString(R.string.msg_s_and_d_othersreplied, new Object[]{replierScreenname, Integer.valueOf(repliesCount - 1)});
    }

    public String getReplierScreenName(int index) {
        JSONObject reply = this.getReply(index);
        JSONObject installation = (JSONObject)reply.get(InstallationNames.installationid);
        String replierScreenName = (String)installation.get(InstallationNames.screenname);
        return replierScreenName;
    }

    public JSONObject getReply(int index) {
        JSONObject reply = (JSONObject)this.getReplies().get(index);
        return reply;
    }

    public List getReplies() {
        JSONObject json = this.getJsonData();
        return json == null ? Collections.EMPTY_LIST : (List)json.get("replies");
    }
}

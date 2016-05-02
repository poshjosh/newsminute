package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;

public class Addextractedemails extends DefaultReadTask<Integer> {
    public static final String PREF_NAME;
    private final Context context;

    static {
        PREF_NAME = Addextractedemails.class.getName() + ".lastemailsextractiontime.long";
    }

    public Addextractedemails(Context context, JSONObject extractedEmails) {
        if (Logx.isLoggable(2)) {
            JsonFormat jsonFormat = new JsonFormat();
            jsonFormat.setIndent(" ");
            jsonFormat.setTidyOutput(true);
            StringBuilder out = new StringBuilder();
            jsonFormat.appendJSONString((Map) extractedEmails, out);
            Logx.log(Log.VERBOSE, getClass(), "Extracted emails json:\n{0}", out, Integer.valueOf(1));
        }
        Map<String, String> params = new HashMap(24, 0.75f);
        params.put("extractedemails", extractedEmails.toJSONString());
        int added = User.getInstance().addParameters(context, params);
        setOutputParameters(params);
        this.context = context;
        setNoUI(!User.getInstance().isAdmin(context));
    }

    public Context getContext() {
        return this.context;
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return "Error adding extracted emails";
    }

    public void onSuccess(Integer numberOfEmailsUpdated) {
        Logx.debug(getClass(), "Number of emails updated: {0}", numberOfEmailsUpdated);
        if (numberOfEmailsUpdated.intValue() > 0) {
            Pref.setLong(this.context, PREF_NAME, System.currentTimeMillis());
        }
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getOutputKey() {
        return "addextractedemails";
    }
}

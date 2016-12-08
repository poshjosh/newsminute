package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.User;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.util.Pref;

import java.util.Map;
import org.json.simple.JSONObject;

public class Addextractedemails extends AbstractReadTask<Integer> {

    public static final String PREF_NAME = Addextractedemails.class.getName() + ".lastemailsextractiontime.long";

    private final Context context;

    public Addextractedemails(Context context, JSONObject extractedEmails) {
        super(context, "");
        if (Logx.getInstance().isLoggable(2)) {
            JsonFormat jsonFormat = new JsonFormat(true);
            StringBuilder out = new StringBuilder();
            jsonFormat.appendJSONString((Map) extractedEmails, out);
            Logx.getInstance().log(Log.VERBOSE, getClass(), "Extracted emails json:\n{0}", out, Integer.valueOf(1));
        }
        this.addOutputParameter("extractedemails", extractedEmails.toJSONString());
        this.addOutputParameters(User.getInstance().getOutputParameters(context));
        this.context = context;
        setNoUI(!User.getInstance().isAdmin(context));
    }

    public void onSuccess(Integer numberOfEmailsUpdated) {
        Logx.getInstance().debug(getClass(), "Number of emails updated: {0}", numberOfEmailsUpdated);
        if (numberOfEmailsUpdated.intValue() > 0) {
            Pref.setLong(this.context, PREF_NAME, System.currentTimeMillis());
        }
    }

    public String getOutputKey() {
        return "addextractedemails";
    }
}

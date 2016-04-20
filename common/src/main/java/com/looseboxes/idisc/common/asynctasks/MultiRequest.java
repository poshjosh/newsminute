package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MultiRequest extends DefaultReadTask<Map> {
    private final String FEEDS_KEY;
    private final String PROPERTIES_KEY;
    private final Context context;
    private final Map<Object, DefaultReadTask> tasks;

    /* renamed from: com.looseboxes.idisc.common.asynctasks.MultiRequest.2 */
    class AnonymousClass2 extends DefaultReadTask<JSONObject> {
        final /* synthetic */ Context val$context;
        final /* synthetic */ String val$outputKey;

        AnonymousClass2(String str, Context context) {
            this.val$outputKey = str;
            this.val$context = context;
        }

        public void onSuccess(JSONObject download) {
        }

        public String getOutputKey() {
            return this.val$outputKey;
        }

        public String getLocalFilename() {
            throw new UnsupportedOperationException("Not Supported");
        }

        public Context getContext() {
            return this.val$context;
        }

        public String getErrorMessage() {
            return getContext().getString(R.string.err_loading, new Object[]{getClass().getSimpleName()});
        }
    }

    public MultiRequest(Context context) {
        this.FEEDS_KEY = "feeds";
        this.PROPERTIES_KEY = "properties";
        this.context = context;
        AliasType[] aliasAliasTypes = AliasType.values();
        this.tasks = new HashMap(aliasAliasTypes.length + 2, 1.0f);
        this.tasks.put("feeds", new FeedDownloadManager(context));
        this.tasks.put("properties", createDefaultReadTask(context, FileIO.getAppPropertiesKey()));
        for (AliasType aliasAliasType : aliasAliasTypes) {
            this.tasks.put(aliasAliasType, createDefaultReadTask(context, FileIO.getAliasesKey(aliasAliasType)));
        }
        Map<String, String> parameters = new HashMap();
        for (DefaultReadTask task : this.tasks.values()) {
            Map<String, String> taskParams = task.getOutputParameters();
            if (taskParams != null) {
                parameters.putAll(taskParams);
            }
        }
        if (!parameters.isEmpty()) {
            setOutputParameters(parameters);
        }
    }

    public void onSuccess(Map download) {
        if (download != null && !download.isEmpty()) {
            for (Entry<Object, DefaultReadTask> entry : this.tasks.entrySet()) {
                try {
                    Object taskId = entry.getKey();
                    DefaultReadTask task = entry.getValue();
                    processParsed(download, taskId, task);
                    Logx.log(Log.DEBUG, getClass(), "SUCCESS: {0}", taskId);
                } catch (Exception e) {
                    Logx.log(getClass(), e);
                }
            }
        }
    }

    public void processParsed(Map download, Object taskId, DefaultReadTask task) {
        Object taskResult = download.get(task.getOutputKey());
        if ("feeds".equals(taskId)) {
            ((FeedDownloadManager) task).onSuccess((List) taskResult);
        } else if ("properties".equals(taskId)) {
            PropertiesManager propsMgr = new PropertiesManager(getContext(), 0);
            propsMgr.setNoUI(true);
            propsMgr.setLastDownloadTime(System.currentTimeMillis());
            propsMgr.setTarget((JSONObject) taskResult);
        } else {
            AliasesManager aliases = App.getAliasesManager(getContext(), 0, (AliasType) taskId);
            aliases.setLastDownloadTime(System.currentTimeMillis());
            aliases.setTarget((JSONObject) taskResult);
        }
    }

    public String getOutputKey() {
        return "getmultipleresults";
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not Supported");
    }

    public Context getContext() {
        return this.context;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_loading, new Object[]{getClass().getSimpleName()});
    }

    private DefaultReadTask<JSONObject> createDefaultReadTask(Context context, String outputKey) {
        DefaultReadTask<JSONObject> task = new AnonymousClass2(outputKey, context);
        task.setNoUI(!User.getInstance().isAdmin(context));
        return task;
    }
}

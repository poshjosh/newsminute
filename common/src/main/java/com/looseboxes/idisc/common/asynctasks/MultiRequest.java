package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.asynctasks.ReadJsonObjectWithSingleEntry;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MultiRequest extends AbstractReadTask<Map> {

    private final String FEEDS_KEY;
    private final String PROPERTIES_KEY;
    private final Context context;
    private final Map<Object, ReadJsonObjectWithSingleEntry> tasks;

    class MultiRequestTask extends AbstractReadTask<JSONObject> {

        final Context val$context;
        final String val$outputKey;

        MultiRequestTask(String str, Context context) {
            super(context, context.getString(R.string.err_loading, new Object[]{MultiRequestTask.class.getSimpleName()}));
            this.val$outputKey = str;
            this.val$context = context;
        }

        public void onSuccess(JSONObject download) { }

        public String getOutputKey() {
            return this.val$outputKey;
        }
    }

    public MultiRequest(Context context) {
        super(context, context.getString(R.string.err_loading, new Object[]{MultiRequest.class.getSimpleName()}));
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
        for (ReadJsonObjectWithSingleEntry task : this.tasks.values()) {
            Map<String, String> taskParams = task.getOutputParameters();
            if(taskParams != null && !taskParams.isEmpty()) {
                this.addOutputParameters(taskParams);
            }
        }
    }

    public void onSuccess(Map download) {
        if (download != null && !download.isEmpty()) {
            for (Entry<Object, ReadJsonObjectWithSingleEntry> entry : this.tasks.entrySet()) {
                try {
                    Object taskId = entry.getKey();
                    ReadJsonObjectWithSingleEntry task = entry.getValue();
                    processParsed(download, taskId, task);
                    Logx.getInstance().log(Log.DEBUG, getClass(), "SUCCESS: {0}", taskId);
                } catch (Exception e) {
                    Logx.getInstance().log(getClass(), e);
                }
            }
        }
    }

    public void processParsed(Map download, Object taskId, ReadJsonObjectWithSingleEntry task) {
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

    private ReadJsonObjectWithSingleEntry<JSONObject> createDefaultReadTask(Context context, String outputKey) {
        ReadJsonObjectWithSingleEntry<JSONObject> task = new MultiRequestTask(outputKey, context);
        task.setNoUI(!User.getInstance().isAdmin(context));
        return task;
    }
}

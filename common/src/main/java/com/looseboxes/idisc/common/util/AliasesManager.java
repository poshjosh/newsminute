package com.looseboxes.idisc.common.util;

import android.content.Context;
import com.looseboxes.idisc.common.asynctasks.DownloadToLocalCache;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.io.IOWrapper;
import com.looseboxes.idisc.common.io.JsonObjectIO;
import org.json.simple.JSONObject;

public class AliasesManager extends StaticResourceManager<JSONObject> {

    private boolean noUI;

    public enum AliasType {
        Categories,
        Content
    }

    private final AliasType aliasType;

    private final String filename;

    public AliasesManager(Context context, long updateIntervalMillis, AliasType aliasType) {
        super(context, updateIntervalMillis, FileIO.getLastDownloadTimePreferenceName(AliasesManager.class, aliasType));
        if(aliasType == null) {
            throw new NullPointerException();
        }
        this.aliasType = aliasType;
        this.filename = FileIO.getAliasesFilename(getAliasType());
    }

    public final AliasType getAliasType() {
        return aliasType;
    }

    public String getFilename() {
        return filename;
    }

    protected IOWrapper<JSONObject> createIOWrapper(Context context) {
        return new JsonObjectIO(context, getFilename());
    }

    public void update(IOWrapper<JSONObject> ioWrapper) {
        setLastDownloadTime(System.currentTimeMillis());
        DownloadToLocalCache<JSONObject> downloader = new DownloadToLocalCache(getContext(), getOutputkey(), ioWrapper);
        downloader.setNoUI(this.noUI);
        downloader.execute();
    }

    public String[] getAliases(String content) {
        JSONObject aliases = (JSONObject) getTarget();
        return aliases == null ? null : (String[]) aliases.get(content);
    }

    public String getOutputkey() {
        return FileIO.getAliasesKey(getAliasType());
    }

    public boolean isNoUI() {
        return this.noUI;
    }

    public void setNoUI(boolean noUI) {
        this.noUI = noUI;
    }
}

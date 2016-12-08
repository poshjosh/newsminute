package com.looseboxes.idisc.common.util;

import android.content.Context;

import com.bc.android.core.io.IOWrapper;
import com.bc.android.core.io.JsonObjectIO;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.io.FileIO;

import org.json.simple.JSONObject;

public class AliasesManager extends StaticResourceManager<JSONObject> {

    public enum AliasType {
        Categories,
        Content
    }

    private AliasType aliasType;

    public AliasesManager(Context context, long updateIntervalMillis, AliasType aliasType) {
        super(context, updateIntervalMillis, false,
                "com.looseboxes.idisc.common.AliasesManager." + aliasType + ".lastDownloadTime.long",
                FileIO.getAliasesKey(aliasType));
        Util.requireNonNull(context);
        Util.requireNonNull(aliasType);
        this.aliasType = aliasType;
    }

    public void destroy() {
        super.destroy();
        this.aliasType = null;
    }

    public final AliasType getAliasType() {
        return aliasType;
    }

    protected IOWrapper<JSONObject> createIOWrapper(Context context) {
        Util.requireNonNull(aliasType);
        return new JsonObjectIO(context, "com.looseboxes.idisc.common.AliasesManager." + aliasType + ".json");
    }

    public String[] getAliases(String content) {
        JSONObject aliases = getTarget();
        return aliases == null ? null : (String[]) aliases.get(content);
    }
}

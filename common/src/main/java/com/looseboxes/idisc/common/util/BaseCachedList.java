package com.looseboxes.idisc.common.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class BaseCachedList<E> extends ArrayList<E> implements CloseableList<E> {
    private final Context context;
    private final String filename;

    public BaseCachedList(int capacity, Context context, String filename) {
        super(capacity);
        this.context = context;
        this.filename = filename;
        load(context, filename);
    }

    public BaseCachedList(Context context, String filename) {
        this.context = context;
        this.filename = filename;
        load(context, filename);
    }

    public BaseCachedList(Collection<? extends E> collection, Context context, String filename) {
        super(collection);
        this.context = context;
        this.filename = filename;
        load(context, filename);
    }

    public void close() {
        save();
    }

    public void save() {
        Pref.setString(this.context, this.filename, JSONValue.toJSONString(this));
    }

    public void load(Context context, String filename) {
        String cachedStr = Pref.getString(context, filename, null);
        if (cachedStr != null && !cachedStr.isEmpty()) {
            List cached;
            try {
                cached = (List) new JSONParser().parse(cachedStr);
            } catch (Exception e) {
                cached = null;
                Logx.log(getClass(), e);
            }
            if (cached != null && !cached.isEmpty()) {
                addAll(0, cached);
            }
        }
    }
}

package com.looseboxes.idisc.common.util;

import android.content.Context;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class CachedMap<K, V> extends LinkedHashMap<K, V> implements CloseableMap<K, V> {
    private final float bucket;
    private final Context context;
    private final String filename;
    private final int limit;

    public CachedMap(Context context, String filename) {
        this.context = context;
        this.filename = filename;
        this.limit = 500;
        this.bucket = 0.2f;
        load(context, filename);
    }

    public CachedMap(Context context, String filename, int limit) {
        this.context = context;
        this.filename = filename;
        this.limit = limit;
        this.bucket = 0.2f;
        load(context, filename);
    }

    public CachedMap(int capacity, Context context, String filename, int limit) {
        super(capacity);
        this.context = context;
        this.filename = filename;
        this.limit = limit;
        this.bucket = 0.2f;
        load(context, filename);
    }

    public CachedMap(int capacity, float loadFactor, Context context, String filename, int limit) {
        super(capacity, loadFactor);
        this.context = context;
        this.filename = filename;
        this.limit = limit;
        this.bucket = 0.2f;
        load(context, filename);
    }

    public CachedMap(Map<? extends K, ? extends V> map, Context context, String filename, int limit) {
        super(map);
        this.context = context;
        this.filename = filename;
        this.limit = limit;
        this.bucket = 0.2f;
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
            Map cached;
            try {
                cached = (Map) new JSONParser().parse(cachedStr);
            } catch (Exception e) {
                cached = null;
                Logx.log(getClass(), e);
            }
            if (cached != null && !cached.isEmpty()) {
                putAll(cached);
            }
        }
    }

    private void truncate() {
        if(size() <= limit) {
            return;
        }
        int toRemove = (int)(limit * bucket);
        if(toRemove < 1) {
            toRemove = 1;
        }
//@todo work on this
        Set<K> ks = this.keySet();
        Iterator iter = ks.iterator();
        for(int i=0; i<toRemove & iter.hasNext(); i++) {
            Object next = iter.next();
            iter.remove();
        }
    }
}

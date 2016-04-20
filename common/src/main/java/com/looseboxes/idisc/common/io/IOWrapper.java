package com.looseboxes.idisc.common.io;

import android.content.Context;

import org.json.simple.JSONObject;

public class IOWrapper<K> extends FileIO {
    private K cached;
    private Context context;
    private String filename;
    private boolean useCache;

    public IOWrapper() {
        this.useCache = true;
    }

    public IOWrapper(Context context, String filename) {
        setContext(context);
        setFilename(filename);
        this.useCache = true;
    }

    public IOWrapper(Context context, String filename, K target) {
        setContext(context);
        setFilename(filename);
        setTarget(target);
        this.useCache = true;
    }

    public static IOWrapper getObjectInstance() {
        return new IOWrapper();
    }

    public static <K> IOWrapper<K> getObjectInstance(Class<K> cls) {
        return new IOWrapper();
    }

    public static IOWrapper<JSONObject> getJsonInstance() {
        return new JsonObjectIO();
    }

    public static IOWrapper<String> getTextInstance() {
        return new TextIO();
    }

    public synchronized K getTarget() {
        K output;
        if (getFilename() == null || getContext() == null) {
            throw new NullPointerException();
        } else if (!this.useCache) {
            this.cached = null;
            output = load();
        } else if (this.cached == null) {
            output = load();
            this.cached = output;
        } else {
            output = this.cached;
        }
        return output;
    }

    public synchronized void setTarget(K target) {
        if (getFilename() == null || getContext() == null) {
            throw new NullPointerException();
        }
        if (this.useCache) {
            this.cached = target;
        } else {
            this.cached = null;
        }
        if (target == null) {
            getContext().deleteFile(getFilename());
        } else {
            save(target);
        }
    }

    public synchronized K load() {
        return (K)loadObject(getContext(), getFilename(), true);
    }

    public synchronized void save(K toSave) {
        saveObject(getContext(), getFilename(), toSave);
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isUseCache() {
        return this.useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
}

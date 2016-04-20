package com.looseboxes.idisc.common.util;

import android.content.Context;

import java.util.Collection;

public class CachedList<E> extends BaseCachedList<E> {
    private final float bucket;
    private final int limit;

    public CachedList(Context context, String filename) {
        super(context, filename);
        this.limit = 500;
        this.bucket = 0.2f;
    }

    public CachedList(int capacity, Context context, String filename, int limit) {
        super(capacity, context, filename);
        this.limit = limit;
        this.bucket = 0.2f;
        if (limit <= 0) {
            throw new IllegalArgumentException("limit <= 0");
        }
    }

    public CachedList(Context context, String filename, int limit) {
        super(context, filename);
        this.limit = limit;
        this.bucket = 0.2f;
        if (limit <= 0) {
            throw new IllegalArgumentException("limit <= 0");
        }
    }

    public CachedList(Collection<? extends E> collection, Context context, String filename, int limit) {
        super((Collection) collection, context, filename);
        this.limit = limit;
        this.bucket = 0.2f;
        if (limit <= 0) {
            throw new IllegalArgumentException("limit <= 0");
        }
    }

    public CachedList(Collection<? extends E> collection, Context context, String filename, int limit, float bucket) {
        super((Collection) collection, context, filename);
        this.limit = limit;
        this.bucket = bucket;
        if (limit <= 0) {
            throw new IllegalArgumentException("limit <= 0");
        } else if (bucket <= 0.0f) {
            throw new IllegalArgumentException("bucket <= 0");
        } else if (bucket > 1.0f) {
            throw new IllegalArgumentException("bucket > 1");
        }
    }

    public boolean add(E object) {
        boolean output = super.add(object);
        if (output) {
            truncate();
        }
        return output;
    }

    public void add(int index, E object) {
        super.add(index, object);
        truncate();
    }

    public boolean addAll(Collection<? extends E> collection) {
        boolean output = super.addAll(collection);
        if (output) {
            truncate();
        }
        return output;
    }

    public boolean addAll(int index, Collection<? extends E> collection) {
        boolean output = super.addAll(index, collection);
        if (output) {
            truncate();
        }
        return output;
    }

    private void truncate() {
        if (size() > this.limit) {
            int toRemove = (int) (((float) this.limit) * this.bucket);
            if (toRemove < 1) {
                toRemove = 1;
            }
            removeRange(0, toRemove);
        }
    }
}

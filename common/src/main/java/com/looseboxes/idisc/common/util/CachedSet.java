package com.looseboxes.idisc.common.util;

import android.content.Context;
import java.util.AbstractSet;
import java.util.Iterator;

public class CachedSet<E> extends AbstractSet<E> implements CloseableSet<E> {
    private CachedList backingList;

    public CachedSet(Context context, String filename) {
        this.backingList = new CachedList(context, filename);
    }

    public CachedSet(Context context, String filename, int limit) {
        this.backingList = new CachedList(context, filename, limit);
    }

    public void clear() {
        this.backingList.clear();
    }

    public void close() {
        this.backingList.close();
    }

    public boolean add(E object) {
        if (contains(object)) {
            return false;
        }
        return this.backingList.add(object);
    }

    public Iterator iterator() {
        return this.backingList.iterator();
    }

    public int size() {
        return this.backingList.size();
    }
}

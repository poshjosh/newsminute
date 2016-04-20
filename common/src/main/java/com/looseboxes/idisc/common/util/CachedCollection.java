package com.looseboxes.idisc.common.util;

import android.content.Context;
import java.util.AbstractCollection;
import java.util.Iterator;

public class CachedCollection<E> extends AbstractCollection<E> {
    private CachedList backingList;

    public CachedCollection(Context context, String filename) {
        this.backingList = new CachedList(context, filename);
    }

    public CachedCollection(Context context, String filename, int limit) {
        this.backingList = new CachedList(context, filename, limit);
    }

    public boolean add(E object) {
        return this.backingList.add(object);
    }

    public Iterator iterator() {
        return this.backingList.iterator();
    }

    public int size() {
        return this.backingList.size();
    }
}

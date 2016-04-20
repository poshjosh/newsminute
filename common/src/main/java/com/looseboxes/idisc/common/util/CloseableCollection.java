package com.looseboxes.idisc.common.util;

import java.io.Closeable;
import java.util.Collection;

public interface CloseableCollection<E> extends Closeable, Collection<E> {
    void close();
}

package com.looseboxes.idisc.common.util;

import java.util.Set;

public interface CloseableSet<E> extends CloseableCollection<E>, Set<E> {
    void close();
}

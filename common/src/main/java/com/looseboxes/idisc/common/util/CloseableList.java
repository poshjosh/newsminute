package com.looseboxes.idisc.common.util;

import java.util.List;

public interface CloseableList<E> extends CloseableCollection<E>, List<E> {
    void close();
}

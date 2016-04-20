package com.looseboxes.idisc.common.util;

import java.io.Closeable;
import java.util.Map;

public interface CloseableMap<K, V> extends Closeable, Map<K, V> {
    void close();
}

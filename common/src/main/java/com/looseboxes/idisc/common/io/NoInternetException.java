package com.looseboxes.idisc.common.io;

import java.io.IOException;

public class NoInternetException extends IOException {
    public NoInternetException(String msg) {
        super(msg);
    }
}

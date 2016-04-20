package com.looseboxes.idisc.common.io;

import android.content.Context;

public class TextIO extends IOWrapper<String> {

    public TextIO() {
    }

    public TextIO(Context context, String filename) {
        super(context, filename);
    }

    public TextIO(Context context, String filename, String target) {
        super(context, filename, target);
    }

    public String load() {
        return load(getContext(), getFilename(), true);
    }

    public void save(String toSave) {
        save(getContext(), toSave, getFilename());
    }
}

package com.looseboxes.idisc.common.asynctasks;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.io.FileIO;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class AbstractFeedReadTask extends DefaultReadTask<List<JSONObject>> {
    public AbstractFeedReadTask() {
        setNoUI(!App.isVisible());
    }

    public String getOutputKey() {
        return FileIO.getFeedskey();
    }

    public String getLocalFilename() {
        return FileIO.getFeedsFilename();
    }
}

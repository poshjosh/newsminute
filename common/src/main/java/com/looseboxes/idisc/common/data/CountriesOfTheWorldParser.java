package com.looseboxes.idisc.common.data;

import android.content.Context;
import com.looseboxes.idisc.common.asynctasks.DefaultReadTask.ResponseParser;
import com.looseboxes.idisc.common.jsonview.JsonView;
import java.util.List;
import org.json.simple.JSONObject;

public class CountriesOfTheWorldParser implements ResponseParser<String, List<JSONObject>> {
    private Context context;

    public CountriesOfTheWorldParser(Context context) {
        this.context = context;
    }

    public List<JSONObject> parse(String source) {
        return JsonView.getDummyFeeds(this.context);
    }
}

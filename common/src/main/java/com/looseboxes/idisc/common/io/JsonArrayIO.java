package com.looseboxes.idisc.common.io;

import android.content.Context;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonArrayIO extends IOWrapper<JSONArray> {
    private static final transient boolean DEBUG = false;

    public JsonArrayIO(Context context, String filename) {
        super(context, filename);
    }

    public JsonArrayIO(Context context, String filename, JSONArray target) {
        super(context, filename, target);
    }

    public Object getKey() {
        return "array";
    }

    public JSONArray load() {
        String jsonStr = load(getContext(), getFilename(), true);
        if (jsonStr == null) {
            return null;
        }
        try {
            return (JSONArray) ((JSONObject) new JSONParser().parse(jsonStr)).get(getKey());
        } catch (ParseException e) {
            return null;
        } catch (ClassCastException e2) {
            return null;
        }
    }

    public void save(JSONArray toSave) {
        JSONObject json = new JSONObject();
        json.put(getKey(), toSave);
        save(getContext(), json.toJSONString(), getFilename());
    }
}

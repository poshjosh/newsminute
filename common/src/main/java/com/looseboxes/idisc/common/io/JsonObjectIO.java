package com.looseboxes.idisc.common.io;

import android.content.Context;
import com.looseboxes.idisc.common.util.Logx;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonObjectIO extends IOWrapper<JSONObject> {

    public JsonObjectIO() { }

    public JsonObjectIO(Context context, String filename) {
        super(context, filename);
    }

    public JsonObjectIO(Context context, String filename, JSONObject target) {
        super(context, filename, target);
    }

    public JSONObject load() {
        String jsonStr = load(getContext(), getFilename(), true);
        if (jsonStr == null) {
            return null;
        }
        try {
            return (JSONObject) new JSONParser().parse(jsonStr);
        } catch (ParseException e) {
            Logx.log(getClass(), e);
            return null;
        } catch (ClassCastException e2) {
            Logx.log(getClass(), e2);
            return null;
        }
    }

    public void save(JSONObject toSave) {
        save(getContext(), toSave.toJSONString(), getFilename());
    }
}

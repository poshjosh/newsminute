package com.looseboxes.idisc.common.io;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonListIO<E> extends IOWrapper<List<E>> implements ContainerFactory {
    private static final transient boolean DEBUG = false;
    private int listSize;

    public JsonListIO(Context context, String filename) {
        super(context, filename);
    }

    public JsonListIO(Context context, String filename, JSONArray target) {
        super(context, filename, target);
    }

    public Object getKey() {
        return "list";
    }

    public List<E> load() {
        String jsonStr = load(getContext(), getFilename(), true);
        if (jsonStr == null) {
            return null;
        }
        try {
            return (List) ((JSONObject) new JSONParser().parse(jsonStr, (ContainerFactory) this)).get(getKey());
        } catch (ParseException e) {
            return null;
        } catch (ClassCastException e2) {
            return null;
        }
    }

    public void save(List<E> toSave) {
        JSONObject json = new JSONObject();
        json.put(getKey(), toSave);
        save(getContext(), json.toJSONString(), getFilename());
    }

    public Map createObjectContainer() {
        return new JSONObject();
    }

    public List<E> creatArrayContainer() {
        return this.listSize > 0 ? new ArrayList(this.listSize) : new ArrayList();
    }

    public int getListSize() {
        return this.listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }
}

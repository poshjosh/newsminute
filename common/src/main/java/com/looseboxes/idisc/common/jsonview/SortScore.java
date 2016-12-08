package com.looseboxes.idisc.common.jsonview;

import org.json.simple.JSONObject;

/**
 * Created by Josh on 10/8/2016.
 */
public interface SortScore {

    long computeScoreOfSorting(JSONObject json);

    long computeTime(JSONObject json, long defaultValue);
}

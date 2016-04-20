package com.looseboxes.idisc.common.jsonview;

import java.util.Comparator;
import java.util.Date;
import org.json.simple.JSONObject;

public class JsonObjectSorter implements Comparator<JSONObject> {
    private final String dateColumn;
    private boolean invertSort;
    private final JsonView jsonView;

    public JsonObjectSorter(String dateColumn, JsonView view) {
        this.dateColumn = dateColumn;
        this.jsonView = view;
    }

    public int compare(JSONObject a, JSONObject b) {
        int compareDates;
        synchronized (this.jsonView) {
            this.jsonView.setJsonData(a);
            Date date_a = this.jsonView.getDate(this.dateColumn);
            this.jsonView.setJsonData(b);
            compareDates = compareDates(date_a, this.jsonView.getDate(this.dateColumn));
        }
        return compareDates;
    }

    protected int compareInts(int hit_a, int hit_b) {
        int i = -1;
        if (!this.invertSort) {
            if (hit_a >= hit_b) {
                i = hit_a == hit_b ? 0 : 1;
            }
            return i;
        } else if (hit_b < hit_a) {
            return -1;
        } else {
            return hit_b == hit_a ? 0 : 1;
        }
    }

    protected int compareDates(Date date_a, Date date_b) {
        int i = -1;
        if (date_a == null && date_b == null) {
            return 0;
        }
        if (date_a == null) {
            if (this.invertSort) {
                return 1;
            }
            return -1;
        } else if (date_b != null) {
            return this.invertSort ? date_b.compareTo(date_a) : date_a.compareTo(date_b);
        } else {
            if (!this.invertSort) {
                i = 1;
            }
            return i;
        }
    }

    public String getDateColumn() {
        return this.dateColumn;
    }

    public JsonView getJsonView() {
        return this.jsonView;
    }

    public boolean isInvertSort() {
        return this.invertSort;
    }

    public void setInvertSort(boolean invertSort) {
        this.invertSort = invertSort;
    }
}

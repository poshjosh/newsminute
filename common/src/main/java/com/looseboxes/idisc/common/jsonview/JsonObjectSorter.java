package com.looseboxes.idisc.common.jsonview;

import org.json.simple.JSONObject;

import java.util.Comparator;
import java.util.Date;

public class JsonObjectSorter implements Comparator<JSONObject> {

    private final String dateColumn;
    private final JsonView jsonView;

    public JsonObjectSorter(String dateColumn, JsonView view) {
        this.dateColumn = dateColumn;
        this.jsonView = view;
    }

    public int compare(JSONObject a, JSONObject b) {
        int compareDates;
        synchronized (this.jsonView) {
            this.jsonView.setJsonData(a);
            Date date_a = this.jsonView.getDate(this.dateColumn, null);
            this.jsonView.setJsonData(b);
            Date date_b = this.jsonView.getDate(this.dateColumn, null);
            compareDates = compareDates(date_a, date_b);
        }
        return compareDates;
    }

    /**
     * <p>Comment from Boolean.compare(boolean, boolean)</p>
     * Compares two {@code boolean} values.
     * @return 0 if lhs = rhs, less than 0 if lhs &lt; rhs, and greater than 0 if lhs &gt; rhs.
     *         (Where true &gt; false.)
     * @since 1.7
     */
    protected int compareBooleans(boolean lhs, boolean rhs) {
        return lhs == rhs ? 0 : lhs ? 1 : -1;
    }

    protected int compareInts(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    /**
     * <p>Comment from Long.compare(long, long)</p>
     * Compares two {@code long} values.
     * @return 0 if lhs = rhs, less than 0 if lhs &lt; rhs, and greater than 0 if lhs &gt; rhs.
     * @since 1.7
     */
    protected int compareLongs(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    protected int compareDates(Date lhs, Date rhs) {
        if (lhs == null && rhs == null) {
            return 0;
        }
        if (lhs == null) {
            return -1;
        } else if (rhs == null) {
            return 1;
        } else {
            return lhs.compareTo(rhs);
        }
    }

    public final String getDateColumn() {
        return this.dateColumn;
    }

    public final JsonView getJsonView() {
        return this.jsonView;
    }
}

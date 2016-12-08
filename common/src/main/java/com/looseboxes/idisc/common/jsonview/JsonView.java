package com.looseboxes.idisc.common.jsonview;

import android.util.Log;

import com.bc.android.core.util.Logx;

import org.json.simple.JSONObject;

import java.util.Date;
import java.util.List;

public class JsonView {

    private transient DateParser dateParser;

    private boolean displayDateAsTimeElapsed;

    private JSONObject source;

    private final Date NOW = new Date();

    public JsonView() { }

    public JsonView(JSONObject source) {
        this.source = source;
    }

    public Object get(String name, Object defaultValue) {
        Object output;
        if(source == null) {
            output = defaultValue;
        }else{
            Object value = source.get(name);
            output = value == null ? defaultValue : value;
        }
        return output;
    }

    public int getEarliestIndex(List<JSONObject> download, String dateColumn) {
        return getIndex(download, dateColumn, true);
    }

    public int getLatestIndex(List<JSONObject> download, String dateColumn) {
        return getIndex(download, dateColumn, false);
    }

    public Date getEarliestDate(List<JSONObject> download, String dateColumn, Date defaultValue) {
        Date output = getDate(download, dateColumn, true, defaultValue);
        return output;
    }

    public Date getLatestDate(List<JSONObject> download, String dateColumn, Date defaultValue) {
        Date output = getDate(download, dateColumn, false, defaultValue);
        return output;
    }

    private Date getDate(List<JSONObject> download, String dateColumn, boolean earliest, Date defaultValue) {
        int i = getIndex(download, dateColumn, earliest);
        Date output;
        if (i == -1) {
            output = null;
        }else {
            try {
                setJsonData(download.get(i));
                output = getDate(dateColumn, defaultValue);
            } finally {
                setJsonData(this.source);
            }
        }
        final int priority = output == null ? Log.DEBUG : Log.VERBOSE;
        Logx.getInstance().log(priority, this.getClass(), "{0} = {1} date in {2} values",
                output, earliest?"earliest":"latest", download==null?null:download.size());
        return output == null ? defaultValue : output;
    }

    private int getIndex(List<JSONObject> download, String dateColumn, boolean earliest) {
        int output = -1;
        Date target = null;
        for (int i = 0; i < download.size(); i++) {
            JSONObject o = download.get(i);
            if (o != null) {
                setJsonData(o);
                Date curr = getDate(dateColumn, null);
                if (curr != null) {
                    if (target != null) {
                        if (earliest ? curr.before(target) : curr.after(target)) {
                            target = curr;
                            output = i;
                        }
                    } else {
                        target = curr;
                        output = i;
                    }
                }
            }
        }
        return output;
    }

    public String getHeading(String defaultValue, int maxLength) {
        String text = getHeading(defaultValue);
        if (text == null || maxLength == -1 || text.length() <= maxLength) {
            return text;
        }
        if (maxLength > 3) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text.substring(0, maxLength);
    }

    public String getHeading(String defaultValue) {
        return defaultValue;
    }

    public String getText(String defaultValue, int maxLength) {
        String text = getText(defaultValue);
        if (text == null || maxLength == -1 || text.length() <= maxLength) {
            return text;
        }
        if (maxLength > 3) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text.substring(0, maxLength);
    }

    public String getText(String defaultValue) {
        return defaultValue;
    }

    public Date getDate(String dateColumn, Date outputIfNone) {
        String dateStr = (String) getJsonData().get(dateColumn);
        if(dateStr == null || dateStr.isEmpty()) {
            return outputIfNone;
        }else {
            Date date = parse(dateStr, outputIfNone);
            return date;
        }
    }

    private Date parse(String dateStr, Date outputIfNone) {
        if (dateStr == null || dateStr.isEmpty()) {
            return outputIfNone;
        }
        if (this.dateParser == null) {
            this.dateParser = new DateParser();
        }
        Date output = this.dateParser.parse(dateStr);
        if(output != null && output.after(NOW)) {
            output = outputIfNone;
        }
        return output;
    }

    public String getDateDisplay(String dateStr) {
        String output = null;
        if (isDisplayDateAsTimeElapsed()) {
            Date date = parse(dateStr, null);
            if (date != null) {
                output = getTimeElapsed(date);
            }
        }
        if (output != null && !output.isEmpty()) {
            return output;
        }
        String[] parts = dateStr.split("\\s");
        if (parts == null || parts.length <= 3) {
            return dateStr;
        }
        String time;
        int n = parts[3].lastIndexOf(58);
        if (n > 2) {
            time = parts[3].substring(0, n);
        } else {
            time = parts[3];
        }
        return new StringBuilder(parts[2]).append(' ').append(parts[1]).append(',').append(' ').append(time).toString();
    }

    public String getTimeElapsed(Date date) {
        long secs = (System.currentTimeMillis() - date.getTime()) / 1000;
        if(secs < 0) {
            return "";
        }
        if (secs < 60) {
            return secs + "s";
        }
        long min = secs / 60;
        if (min < 60) {
            return min + "m";
        }
        long hrs = min / 60;
        if (hrs < 24) {
            return hrs + "h";
        }
        return (hrs / 24) + "d";
    }

    public boolean isDisplayDateAsTimeElapsed() {
        return this.displayDateAsTimeElapsed;
    }

    public void setDisplayDateAsTimeElapsed(boolean displayDateAsTimeElapsed) {
        this.displayDateAsTimeElapsed = displayDateAsTimeElapsed;
    }

    public JSONObject getJsonData() {
        return this.source;
    }

    public void setJsonData(JSONObject source) {
        this.source = source;
    }
}

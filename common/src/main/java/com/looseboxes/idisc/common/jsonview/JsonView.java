package com.looseboxes.idisc.common.jsonview;

import android.content.Context;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.Raw;
import com.looseboxes.idisc.common.util.Logx;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

public class JsonView {
    private Date date;
    private transient SimpleDateFormat dateFormat;
    private boolean displayDateAsTimeElapsed;
    private JSONObject source;

    public JsonView() {
    }

    public JsonView(JSONObject source) {
        this.source = source;
    }

    public int getEarliestIndex(List<JSONObject> download, String dateColumn) {
        return getIndex(download, dateColumn, true);
    }

    public int getLatestIndex(List<JSONObject> download, String dateColumn) {
        return getIndex(download, dateColumn, false);
    }

    public long getEarliestTime(List<JSONObject> download, String dateColumn) {
        Date output = getDate(download, dateColumn, true);
        return output == null ? -1 : output.getTime();
    }

    public long getLatestTime(List<JSONObject> download, String dateColumn) {
        Date output = getDate(download, dateColumn, false);
        return output == null ? -1 : output.getTime();
    }

    private Date getDate(List<JSONObject> download, String dateColumn, boolean earliest) {
        int i = getIndex(download, dateColumn, earliest);
        if (i == -1) {
            return null;
        }
        try {
            setJsonData((JSONObject) download.get(i));
            Date date = getDate(dateColumn);
            return date;
        } finally {
            setJsonData(this.source);
        }
    }

    private int getIndex(List<JSONObject> download, String dateColumn, boolean earliest) {
        int output = -1;
        Date target = null;
        for (int i = 0; i < download.size(); i++) {
            JSONObject o = (JSONObject) download.get(i);
            if (o != null) {
                setJsonData(o);
                Date curr = getDate(dateColumn);
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

    public Date getDate(String dateColumn) {
        return _parse((String) getJsonData().get(dateColumn));
    }

    private Date _parse(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        if (this.dateFormat == null) {
            this.dateFormat = createDefaultDateFormat();
        }
        this.date = parse(this.dateFormat, dateStr);
        return this.date;
    }

    public static Date parse(String dateStr) {
        return parse(createDefaultDateFormat(), dateStr);
    }

    public static SimpleDateFormat createDefaultDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyLocalizedPattern("EEE MMM dd HH:mm:ss z yyyy");
        dateFormat.setTimeZone(App.getUserSelectedTimeZone());
        return dateFormat;
    }

    public static Date parse(SimpleDateFormat dateFormat, String dateStr) {
        Date date = null;
        if (!(dateStr == null || dateStr.isEmpty())) {
            try {
                date = dateFormat.parse(dateStr);
            } catch (Exception e) {
                Logx.log(5, JsonView.class, "Error parsing date: {0} with pattern: 'EEE MMM dd HH:mm:ss z yyyy'", dateStr);
                Logx.log(JsonView.class, e);
            }
        }
        return date;
    }

    public String getDateDisplay(String dateStr) {
        String output = null;
        if (isDisplayDateAsTimeElapsed()) {
            this.date = _parse(dateStr);
            if (this.date != null) {
                output = getTimeElapsed(this.date);
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

    public static List<JSONObject> getDummyFeeds(Context context) {
        return getDummyFeeds(context, context.getString(R.string.app_label) + "-local-default-feed-list");
    }

    public static List<JSONObject> getDummyFeeds(Context context, Object categories) {
        List<JSONObject> output = new ArrayList();
        Map map = Raw.get(context);
        for (Object title : map.keySet()) {
            try {
                output.add(getDefaultJsonObject(context, title, map.get(title), categories));
            } catch (Exception e) {
                Logx.log(JsonView.class, e);
            }
        }
        return output;
    }

    public static JSONObject getDefaultJsonObject(Context context, Object title, Object content, Object categories) {
        JSONObject obj = new JSONObject();
        String cat = categories.toString();
        Date date = new Date();
        String appLabel = App.getLabel(context);
        obj.put(FeedNames.author, appLabel);
        obj.put(FeedNames.categories, cat);
        obj.put(FeedNames.content, content.toString());
        obj.put(FeedNames.datecreated, date.toString());
        obj.put(FeedNames.description, title);
        obj.put(FeedNames.feeddate, date.toString());
        obj.put(FeedhitNames.feedid, Long.valueOf(0));
        obj.put(FeedNames.keywords, cat);
        Map siteData = new HashMap();
        siteData.put(FeedNames.siteid, Integer.valueOf(0));
        siteData.put("site", appLabel);
        obj.put(FeedNames.siteid, siteData);
        obj.put(FeedNames.title, title.toString());
        obj.put("hitcount", Integer.valueOf(0));
        return obj;
    }

    public static void appendDefaultJsonString(Context context, StringBuilder appendTo, Object title, Object content, Object categories) {
        appendTo.append('{');
        Date date = new Date();
        String x = ",\n\t\"";
        String y = "\": \"";
        appendTo.append("\n\t\"").append(FeedNames.datecreated).append(y).append(date).append('\"');
        appendTo.append(x).append(FeedNames.datecreated).append(y).append(date).append('\"');
        String appName = App.getLabel(context);
        StringBuilder append = appendTo.append(x).append(FeedNames.author).append(y);
        if (appName == null) {
            appName = "";
        }
        append.append(appName).append('\"');
        appendTo.append(x).append(FeedNames.title).append(y).append(title).append('\"');
        appendTo.append(x).append(FeedNames.description).append(y).append(title).append('\"');
        appendTo.append(x).append(FeedNames.siteid).append(y).append('0').append('\"');
        appendTo.append(x).append("hitcount").append(y).append(0).append('\"');
        appendTo.append(x).append(FeedNames.content).append("\": ").append(content).append('\"');
        appendTo.append(x).append(FeedhitNames.feedid).append("\": ").append(0);
        appendTo.append(x).append(FeedNames.categories).append(y).append(categories).append('\"');
        appendTo.append('}');
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
        this.date = null;
    }
}

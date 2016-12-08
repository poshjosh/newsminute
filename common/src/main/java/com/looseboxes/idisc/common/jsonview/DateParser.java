package com.looseboxes.idisc.common.jsonview;

import com.bc.android.core.util.Logx;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Josh on 10/12/2016.
 */
public class DateParser implements Serializable {

    private final String datePattern;
    private final SimpleDateFormat dateFormat;

    public DateParser() {
        this.datePattern = "EEE MMM dd HH:mm:ss z yyyy";
        this.dateFormat = new SimpleDateFormat(datePattern);
        this.dateFormat.setTimeZone(TimeZone.getDefault());
    }

    public Date parse(String dateStr) {
        Date date = null;
        if (!(dateStr == null || dateStr.isEmpty())) {
            try {
                date = dateFormat.parse(dateStr);
            } catch (Exception e) {
                Logx.getInstance().log(5, this.getClass(), "Error parsing date: {0} with pattern: '"+datePattern+"'", dateStr);
                Logx.getInstance().log(this.getClass(), e);
            }
        }
        return date;
    }

//    public final Date getNOW() {
//        return NOW;
//    }

//    public final SimpleDateFormat getDateFormat() {
//        return dateFormat;
//    }

    public final String getDatePattern() {
        return datePattern;
    }
}

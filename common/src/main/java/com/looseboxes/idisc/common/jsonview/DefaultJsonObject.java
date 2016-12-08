package com.looseboxes.idisc.common.jsonview;

import android.content.Context;

import com.looseboxes.idisc.common.App;

import org.json.simple.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh on 8/15/2016.
 */
public class DefaultJsonObject extends JSONObject {

    public DefaultJsonObject(Context context, Object title, Object content, Object categories) {

        this(context, Long.valueOf(0L), title, content, categories);
    }

    public DefaultJsonObject(Context context, Long feedid, Object title, Object content, Object categories) {
        String cat = categories.toString();
        Date date = new Date();
        String appLabel = App.getLabel(context);
        this.put(FeedNames.author, appLabel);
        this.put(FeedNames.categories, cat);
        this.put(FeedNames.content, content.toString());
        this.put(FeedNames.datecreated, date.toString());
        this.put(FeedNames.description, title);
        this.put(FeedNames.feeddate, date.toString());
        this.put(FeedhitNames.feedid, feedid);
        this.put(FeedNames.keywords, cat);

        Map siteData = new HashMap();
        siteData.put(SiteNames.siteid, Integer.valueOf(28));
        siteData.put(SiteNames.site, App.getLabel(context));
        siteData.put(SiteNames.iconurl, "");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.APRIL, 26, 18, 14, 59);
        siteData.put("datecreated", calendar.getTime());
        calendar.set(2016, Calendar.OCTOBER, 6, 3, 0, 28);
        siteData.put("timemodified", calendar.getTime());

        Map countryData = new HashMap();
        countryData.put(CountryNames.countryid, 566);
        countryData.put(CountryNames.iso2code, "NG");
        countryData.put(CountryNames.iso3code, "NGA");
        countryData.put(CountryNames.country, "Nigeria");
        siteData.put("countryid", countryData);

        Map timeZoneData = new HashMap();
        timeZoneData.put("timezoneid", 229);
        timeZoneData.put("abbreviation", "WCAST");
        timeZoneData.put("timezonename", "Africa/Lagos");
        siteData.put("timezoneid", timeZoneData);

        this.put(FeedNames.siteid, siteData);

        this.put(FeedNames.title, title.toString());
        this.put("hitcount", Integer.valueOf(0));
    }
}

package com.looseboxes.idisc.common.notice;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.util.Geo;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.jsonview.CountryNames;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Josh on 11/19/2016.
 */
public class FeedNotificationFilter implements FeedFilter {

    private final Context context;
    private final String countryCode;
    private final Date dateLowerlimit;
    private final FeedNotificationHandler feedNotificationHandler;


    private final Logx logger = Logx.getInstance();

    public FeedNotificationFilter(Context context) {
        this(context, new Geo().getCountryCode(context, null));
    }

    public FeedNotificationFilter(Context context, String countryCode) {
        this.context = context;
        this.countryCode = countryCode;
        final long youngestAge = getYoungestAge(-1L);
        this.dateLowerlimit = youngestAge == -1L ? null : new Date(youngestAge);
        logger.debug(this.getClass(), "Country code: {0}, date lower limit: {1}", this.countryCode, this.dateLowerlimit);
        this.feedNotificationHandler = new FeedNotificationHandler(context);
    }

    @Override
    public boolean accept(Feed feed) {

        boolean accept = !feedNotificationHandler.isFeedViewedFromNotice(feed.getFeedid());

        if(accept) {

            final String key;
            switch(countryCode.length()) {
                case 2: key = CountryNames.iso2code; break;
                case 3: key = CountryNames.iso3code; break;
                default: throw new IllegalArgumentException();
            }

            final String feedCountryCode = (String)(feed.getCountry() == null ? null : feed.getCountry().get(key));

            accept = this.countryCode == null || feedCountryCode == null || feedCountryCode.equalsIgnoreCase(countryCode);

            if(!accept) {
                logger.log(Log.VERBOSE, this.getClass(), "Feed countrycode: {0}, user countrycode: {1}", feedCountryCode, countryCode);
            }
        }

        if(accept) {

            final Date feeddate = feed.getDate(FeedNames.feeddate, null);

            accept = this.dateLowerlimit == null || feeddate == null ? false : feeddate.after(dateLowerlimit);

            if(!accept) {
                logger.log(Log.VERBOSE, this.getClass(), "Feeddate: {0}, date lower limit: {1}", feeddate, this.dateLowerlimit);
            }
        }

        return accept;
    }

    private long getYoungestAge(long defaultValue) {
        try {
            return System.currentTimeMillis() - TimeUnit.HOURS.toMillis(App.getPropertiesManager(context).getLong(PropertiesManager.PropertyName.notificationMaxFeedAgeHours));
        } catch (Exception e) {
            logger.log(getClass(), e);
            return defaultValue;
        }
    }
}

package com.looseboxes.idisc.common.jsonview;

import android.content.Context;

import com.bc.android.core.util.Geo;
import com.looseboxes.idisc.common.feedfilters.FeedFilterImpl;

import org.json.simple.JSONObject;

import java.util.Set;

public class FeedComparator extends FeedFilterImpl {

    private final String countryCode;

    public FeedComparator(Context context, long defaultTime, boolean acceptedByDefault) {
        this(context, defaultTime, acceptedByDefault, new Geo().getCountryCode(context, null));
    }

    public FeedComparator(Context context, long defaultTime, boolean acceptedByDefault, String countryCode) {
        super(context, defaultTime, acceptedByDefault);
        this.countryCode = this.validateCountryCode(countryCode);
    }

    public FeedComparator(
            Context context, Set<String> preferredCategories, Set<String> preferredSources,
            long defaultTime, boolean acceptedByDefault) {
        this(context, preferredCategories, preferredSources, defaultTime, acceptedByDefault,
                new Geo().getCountryCode(context, null));
    }

    public FeedComparator(
            Context context, Set<String> preferredCategories, Set<String> preferredSources,
            long defaultTime, boolean acceptedByDefault, String countryCode) {
        super(context, preferredCategories, preferredSources, defaultTime, acceptedByDefault);
        this.countryCode = this.validateCountryCode(countryCode);
    }

    private String validateCountryCode(String countryCode) {
        if(countryCode != null && (countryCode.length() != 2 && countryCode.length() != 3)) {
            throw new IllegalArgumentException("Country-code length must be 2 or 3 chars only");
        }
        return countryCode;
    }

    @Override
    public int compare(JSONObject a, JSONObject b) {

        final boolean countryAok = this.isOfAcceptableCountry(a, false);
        final boolean countryBok = this.isOfAcceptableCountry(b, false);

        final int byCountry = this.compareBooleans(countryAok, countryBok);

        final int byAll;

        if(byCountry != 0) {

            byAll = byCountry;

        }else {

            final long t1 = computeScoreOfSorting(a);

            final long t2 = computeScoreOfSorting(b);

            byAll = this.compareLongs(t1, t2);
        }

        return byAll;
    }

    public boolean isOfAcceptableCountry(JSONObject a, boolean defaultValue) {

        Feed feed = (Feed) getJsonView();

        feed.setJsonData(a);

        boolean output;
        if(this.countryCode == null) {

            output = defaultValue;

        }else{

            final int countryCodeLength = this.countryCode.length();

            final String feedCountryCode;
            switch (countryCodeLength) {
                case 2:
                    feedCountryCode = (String)feed.getCountry().get(CountryNames.iso2code); break;
                case 3:
                    feedCountryCode = (String)feed.getCountry().get(CountryNames.iso3code); break;
                default: feedCountryCode = null;
            }

            output = (this.isAcceptNullFeedCountryCodes() && feedCountryCode == null) ? true : this.countryCode.equalsIgnoreCase(feedCountryCode);
        }

        return output;
    }

    public boolean isAcceptNullFeedCountryCodes() {
        return true;
    }
}

package com.looseboxes.idisc.common.util;

import android.content.Context;
import com.looseboxes.idisc.common.asynctasks.DownloadToLocalCache;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.io.IOWrapper;
import com.looseboxes.idisc.common.io.JsonObjectIO;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;

public class PropertiesManager extends StaticResourceManager<JSONObject> {
    public static final int DOWNLOAD_INTERVAL_HOURS = 6;
    // Work on this being editable like other properties
    // That would involve logging howmuch free subscription/trial each user has
    //
    public static final int FREE_SUBSCRIPTION_MONTHS = 12;
    private boolean noUI;

    public enum PropertyName {
        developerEmails(Collections.singletonList("posh.bc@gmail.com")),
        developerPasswords( Collections.singletonList("1kjvdul-")),
        minimumMatchCountForAliases(2),
        textComparisonMaxLength(100),
        textComparisonTolerance(0.10000000149011612d),
        feedLoadServiceDelay(500),
        connectTimeoutMillis(30000),
        readTimeoutMillis(60000),
        feedDownloadLimit(200),
        feedDownloadBufferSize(1000),
        logFileCount(20),
        logFileLimit(50000),
        addedValueLimitMinutes(2880),
        addedValuePerHitMinutes(15),
        textLengthXShort(50),
        textLengthShort(100),
        textLengthMedium(150),
        textLengthLong(300),
        textLengthXLong(1000),
        advertDisplayPeriod(5000),
        advertCategoryName("newsminute-advert"),

        // To be added to live server properties file
//
// These are decimals not numbers, hence the floating point
        advertFrequencyLocalAds(0.2d),
        advertFrequencyBannerAds(0.6d),
        advertFrequencyInterstitialAds(0.2d),
        syncIntervalHours(6),
        sources(PropertiesManager.getDefaultSources()),
        categories(PropertiesManager.getDefaultCategories()),
        appServiceUrl("http://www.looseboxes.com/idisc"),
        appBasicVersionUrl("https://play.google.com/store/apps/details?id=com.looseboxes.idisc.newsminutelite"),
        appFreeVersionUrl("https://play.google.com/store/apps/details?id=com.looseboxes.idisc.newsminutefree"),
        appPremiumVersionUrl("https://play.google.com/store/apps/details?id=com.looseboxes.idisc.newsminuteplus"),
        appInvitesUrl("http://www.looseboxes.com/idisc/incrementinvites"),
        // These are decimals not numbers, hence the floating point
// Do not change these as they are already fixed in google play
        price1MonthNaira(150.0d),
        price3MonthsNaira(300.0d),
        price6MonthsNaira(500.0d),
        price12MonthsNaira(800.0d),
        defaultAutoSearchText("breaking, b r e a k i n g, b-r-e-a-k-i-n-g, just-in, just in"),
        appInvitesCustomImageUrl("http://www.looseboxes.com/idisc/images/appicon.png"),
        countriesOfTheWorldDataEndpoint("http://ec2-54-201-183-195.us-west-2.compute.amazonaws.com/cow/cow.csv"),
        citiesOfTheWorldDataEndpoint("opengeocode.org/download/worldcities.zip"),
        languagesOfTheWorldDataEndpoint("http://opengeocode.org/download/iso639lang.txt"),
        governmentwebsitesOfTheWorldDataEndpoint("opengeocode.org/download/CCurls.txt"),
        notificationMaxFeedAgeHours(12),
        headlinePattern("b.??r.??e.??a.??k.??i.??n.??g|just.??in"),
        freesubscriptionMonths(12);
        
        private final double defaultDecimal;
        private final long defaultNumber;
        private final Object defaultValue;

        private PropertyName(long defaultValue) {
            this.defaultNumber = defaultValue;
            this.defaultDecimal = -1.0d;
            this.defaultValue = null;
        }

        private PropertyName(double defaultValue) {
            this.defaultNumber = -1;
            this.defaultDecimal = defaultValue;
            this.defaultValue = null;
        }

        private PropertyName(Object defaultValue) {
            this.defaultNumber = -1;
            this.defaultDecimal = -1.0d;
            this.defaultValue = defaultValue;
        }

        public long getDefaultNumber() {
            return this.defaultNumber;
        }

        public Double getDefaultDecimal() {
            return Double.valueOf(this.defaultDecimal);
        }

        public Object getDefaultValue() {
            return this.defaultValue;
        }
    }

    public PropertiesManager(Context context, long updateIntervalMillis) {
        super(context, updateIntervalMillis, PropertiesManager.class.getName()+".lastDownloadTime.long");
    }

    public void update(IOWrapper<JSONObject> ioWrapper) {
        setLastDownloadTime(System.currentTimeMillis());
        DownloadToLocalCache<JSONObject> downloader = new DownloadToLocalCache(getContext(), FileIO.getAppPropertiesKey(), ioWrapper);
        downloader.setNoUI(this.noUI);
        downloader.execute();
    }

    protected IOWrapper<JSONObject> createIOWrapper(Context context) {
        return new JsonObjectIO(context, FileIO.getAppPropertiesFilename());
    }

    public int getInt(PropertyName propertyName) {
        return (int) getLong(propertyName);
    }

    public long getLong(PropertyName propertyName) {
        Long value = (Long) getProperty(Long.class, propertyName);
        return value == null ? propertyName.defaultNumber : value.longValue();
    }

    public float getFloat(PropertyName propertyName) {
        return (float) getDouble(propertyName);
    }

    public double getDouble(PropertyName propertyName) {
        Double value = (Double) getProperty(Double.class, propertyName);
        return value == null ? propertyName.defaultDecimal : value.doubleValue();
    }

    public String getString(PropertyName propertyName) {
        String value = (String) getProperty(String.class, propertyName);
        return value == null ? (String) propertyName.defaultValue : value;
    }

    public List getList(PropertyName propertyName) {
        List output;
        List value = (List) getProperty(List.class, propertyName);
        if (value == null) {
            output = (List) propertyName.defaultValue;
        } else {
            output = value;
        }
        if (output == null) {
            return null;
        }
        return output.isEmpty() ? Collections.EMPTY_LIST : new LinkedList(output);
    }

    public Set getSet(PropertyName propertyName) {
        Set output;
        Set value = (Set) getProperty(Set.class, propertyName);
        if (value == null) {
            output = (Set) propertyName.defaultValue;
        } else {
            output = value;
        }
        if (output == null) {
            return null;
        }
        return output.isEmpty() ? Collections.EMPTY_SET : new LinkedHashSet(output);
    }

    public Map getMap(PropertyName propertyName) {
        Map output;
        Map value = (Map) getProperty(Map.class, propertyName);
        if (value == null) {
            output = (Map) propertyName.defaultValue;
        } else {
            output = value;
        }
        if (output == null) {
            return null;
        }
        return output.isEmpty() ? Collections.EMPTY_MAP : new LinkedHashMap(output);
    }

    public <K> K getProperty(Class<K> cls, PropertyName propertyName) {
        return getProperties() == null ? null : (K)getProperties().get(propertyName);
    }

    public JSONObject getProperties() {
        return (JSONObject) getTarget();
    }

    private static Set<String> getDefaultCategories() {
        return Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(new String[]{"Business", "Entertainment", "Headlines", "International", "National", "Politics", "Religion", "Sports", "Technology"})));
    }

    private static JSONObject getDefaultSources() {
        JSONObject sources = new JSONObject();
        sources.put("1", "Aljazeera");
        sources.put("2", "twitter");
        sources.put("3", "Osundefender");
        sources.put("4", "Vanguard Nigeria");
        sources.put("5", "Reuters");
        sources.put("6", "Sahara Reporters");
        sources.put("7", "Punch Nigeria");
        sources.put("8", "vanguardngr");
        sources.put("9", "punchng");
        sources.put("10", "tribune");
        sources.put("11", "thenationonlineng");
        sources.put("12", "thisday");
        sources.put("13", "Premium Times");
        sources.put("14", "ngrguardiannews");
        sources.put("15", "saharareporters");
        sources.put("16", "leadership.ng");
        sources.put("17", "Channels TV");
        sources.put("18", "naij");
        sources.put("19", "bellanaija");
        sources.put("20", "lindaikeji.blogspot");
        sources.put("21", "channelstv_headlines");
        sources.put("22", "aitonline_news");
        sources.put("23", "dailytrust");
        return sources;
    }

    public boolean isNoUI() {
        return this.noUI;
    }

    public void setNoUI(boolean noUI) {
        this.noUI = noUI;
    }
}

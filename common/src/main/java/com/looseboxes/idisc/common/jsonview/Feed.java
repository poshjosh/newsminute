package com.looseboxes.idisc.common.jsonview;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.util.Logx;
import com.bc.util.StringComparator;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Feed must have FeedID. Numeral Zero (0) is the default FeedID for any locally
 * created feeds. Also, a Feed must have SiteID. String Zero (0) is the default
 * SiteID for any locally created feeds.
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class Feed extends JsonView {

    public Feed() { }

    public Feed(JSONObject source) {
        super(source);
    }

    private static int _$mmca = -1;
    public static int getDefaultMinimumMatchCountAliases(Context context) {
        if (_$mmca == -1) {
            _$mmca = App.getPropertiesManager(context).getInt(PropertyName.minimumMatchCountForAliases);
        }
        return _$mmca;
    }

    public List<JSONObject> getFeeds(List<JSONObject> feeds, FeedFilter feedFilter) {

        int iterated = 0;

        List<JSONObject> output = null;

        for (JSONObject json : feeds) {

            setJsonData(json);

            if(feedFilter == null || feedFilter.accept(this)) {
                if (output == null) {
                    output = new ArrayList(feeds.size() - iterated);
                }
                output.add(json);
            }

            ++iterated;
        }

        return output;
    }

    public List<JSONObject> getFeedsAfter(List<JSONObject> feeds, Date date) {
        return getFeedsFromDateReference(feeds, date, true);
    }

    public List<JSONObject> getFeedsBefore(List<JSONObject> feeds, Date date) {
        return getFeedsFromDateReference(feeds, date, false);
    }

    private List<JSONObject> getFeedsFromDateReference(List<JSONObject> feeds, Date date, boolean after) {
        List<JSONObject> output = null;
        for (JSONObject json : feeds) {
            setJsonData(json);
            Date feeddate = getDate(FeedNames.feeddate, null);
            if (feeddate != null && ((after && feeddate.after(date)) || (!after && feeddate.before(date)))) {
                if (output == null) {
                    output = new ArrayList(feeds.size());
                }
                output.add(json);
            }
        }
        return output;
    }

    public JSONObject getMostViewed(Context context, List<JSONObject> feeds) {
        return this.getMostViewed(context, feeds, null);
    }

    public JSONObject getMostViewed(Context context, List<JSONObject> feeds, FeedFilter filter) {
        int mostViewedHitcount = -1;
        JSONObject mostViewed = null;
        for (JSONObject json : feeds) {
            setJsonData(json);
            if(filter == null || filter.accept(this)) {
                int hitcount = getHitcount(context);
                if (hitcount > mostViewedHitcount) {
                    mostViewedHitcount = hitcount;
                    mostViewed = json;
                }
            }
        }
        return mostViewed;
    }

    public void appendFilename(StringBuilder appendTo) {
        appendTo.append(getFeedid()).append('_');
        String heading = getHeading("page", 100);
        boolean mayAppendDash = false;
        for (int i = 0; i < heading.length(); i++) {
            char ch = heading.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                appendTo.append(ch);
                mayAppendDash = true;
            } else if (mayAppendDash) {
                appendTo.append('-');
                mayAppendDash = false;
            }
        }
        appendTo.append(".jsp");
    }

    public int getEarliestIndex(List<JSONObject> download) {
        return getEarliestIndex(download, FeedNames.feeddate);
    }

    public int getLatestIndex(List<JSONObject> download) {
        return getLatestIndex(download, FeedNames.feeddate);
    }

    public Date getEarliestDate(List<JSONObject> download, Date defaultValue) {
        return getEarliestDate(download, FeedNames.feeddate, defaultValue);
    }

    public Date getLatestDate(List<JSONObject> download, Date defaultValue) {
        return getLatestDate(download, FeedNames.feeddate, defaultValue);
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public boolean contains(Context context, Collection<JSONObject> feeds, JSONObject feedJson) {
        PropertiesManager appProps = App.getPropertiesManager(context);
        return contains(feeds, feedJson, (float) appProps.getInt(PropertyName.textComparisonTolerance), appProps.getInt(PropertyName.textComparisonMaxLength));
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public boolean contains(Collection<JSONObject> feeds, JSONObject toFind, float textComparisonTolerance, int textComparisonMaxLength) {
        return indexOf(feeds, toFind, textComparisonTolerance, textComparisonMaxLength) != -1;
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public int indexOf(Context context, Collection<JSONObject> feeds, JSONObject toFind) {
        PropertiesManager appProps = App.getPropertiesManager(context);
        return indexOf(feeds, toFind, (float) appProps.getInt(PropertyName.textComparisonTolerance), appProps.getInt(PropertyName.textComparisonMaxLength));
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public int indexOf(Collection<JSONObject> feeds, JSONObject toFind, float textComparisonTolerance, int textComparisonMaxLength) {
        StringComparator sc = new StringComparator();
        int pos = -1;
        for (JSONObject feedJson : feeds) {
            ++pos;
            Object id1 = feedJson.get(FeedNames.feedid);
            Object id2 = toFind.get(FeedNames.feedid);
            if(id1 != null && id1.equals(id2)) {
                return pos;
            }
            if (matches(feedJson, toFind, sc, textComparisonTolerance, textComparisonMaxLength)) {
                return pos;
            }
        }
        return -1;
    }

    public boolean matches(JSONObject feed_0, JSONObject feed_1) {
        return matches(feed_0, feed_1, null, 0.0f, Integer.MAX_VALUE);
    }

    public boolean matches(JSONObject feed_0, JSONObject feed_1, StringComparator sc, float tolerance, int maxTextLen) {
        if (feed_0 == null || feed_1 == null) {
            throw new NullPointerException();
        }
        boolean matches = false;
        final JSONObject json = this.getJsonData();
        try {
            setJsonData(feed_1);
            long siteid_1 = getSiteid();
            Object text_1 = getText(null, maxTextLen);
            setJsonData(feed_0);
            if (getSiteid() != siteid_1) {
                return false;
            }
            Object text_0 = getText(null, maxTextLen);
            if (sc == null || tolerance <= 0.0f) {
                return text_0.equals(text_1);
            }
            matches = sc.compare(text_0, text_1, tolerance);
            if(matches) {
                Logx.getInstance().debug(this.getClass(), "Matches: {0}\nlhs: {1}\nrhs: {2}", matches, feed_0, feed_1);
            }
        }finally{
            this.setJsonData(json);
        }
        return matches;
    }

    /**
     * Mirrors {@link java.util.ArrayList#contains(java.lang.Object)}. However
     * uses a custom equality which only checks entries of selected keys.
     * Particularly feedid is not considered as it is unique even for the same
     * feed title and content.
     */
    public boolean contains_old(List feeds, Object feed_1) {
        if (feed_1 != null) {
            JSONObject json_1 = (JSONObject)feed_1;
            String [] keys;
            if(json_1.get(FeedNames.categories).toString().equalsIgnoreCase("statuses")) {
                keys = new String[]{FeedNames.siteid, FeedNames.title, FeedNames.url};
            }else{
                // When we used this for tweets, most tweets were considered duplicates.
                keys = new String[]{FeedNames.siteid, FeedNames.title};
            }

            for (Object feed_0:feeds) {
                // Custom equality which only checks entries of selected keys.
                if (NewsminuteUtil.equals((Map) feed_0, feed_1, keys)) {
                    return true;
                }
            }
        } else {
            for (Object e:feeds) {
                if (e == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public int removeEmptyFeeds(List feeds) {
        Iterator iter = feeds.iterator();
        int i = 0;
        while (iter.hasNext()) {
            JSONObject json = (JSONObject) iter.next();
            if (json == null) {
                iter.remove();
                i++;
            } else {
                setJsonData(json);
                String text = getText(null);
                if (text == null || text.trim().isEmpty()) {
                    iter.remove();
                    i++;
                }
            }
        }
        return i;
    }

    public int getHitcount(Context context) {
        Long feedid = getFeedid();
        Object oval = getJsonData().get("hitcount");
        String sval = oval == null ? null : oval.toString();
        int b = sval == null ? 0 : Integer.parseInt(sval);
        return b;
    }

    public Long getFeedid() {
        return (Long) getJsonData().get(FeedNames.feedid);
    }

    public String getImageUrl() {
        return (String) getJsonData().get(FeedNames.imageurl);
    }

    public String getLocalUrl() {
        return App.getUrlScheme()+"://displayfeed?feedid="+this.getFeedid();
    }

    public String getUrl() {
        return (String) getJsonData().get(FeedNames.url);
    }

    public String getKeywords() {
        return (String) getJsonData().get(FeedNames.keywords);
    }

    public String getTitle() {
        return (String) getJsonData().get(FeedNames.title);
    }

    public String getDescription() {
        return (String) getJsonData().get(FeedNames.description);
    }

    public String getCategories() {
        return (String) getJsonData().get(FeedNames.categories);
    }

    public String getContent() {
        return (String) getJsonData().get(FeedNames.content);
    }

    public String getAuthor() {
        return (String) getJsonData().get(FeedNames.author);
    }

    public String getSiteIconurl() {

        Map siteData = this.getSite();

        return (String)siteData.get("iconurl");
    }

    public long getSiteid() {

        Map siteData = this.getSite();

        Long lval = (Long)siteData.get(FeedNames.siteid);

        return lval == null ? 28 : lval.longValue();
    }

    public Map getCountry() {
        Map site = this.getSite();
        if(site == null) {
            return Collections.EMPTY_MAP;
        }else {
            Map country = (Map)site.get(CountryNames.countryid);
            return country == null ? Collections.EMPTY_MAP : country;
        }
    }

    public Map getSite() {
        return (Map)this.get(SiteNames.siteid, Collections.EMPTY_MAP);
    }

    /**
     * Use {@link #getSiteid()}
     * @deprecated
     * @see #getSiteid()
     */
    @Deprecated
    public long getSiteid_v1_0_1(String sitedata) {
// Format:        com.idisc.pu.entities.SiteNames[ siteid=4 ]
// We want to extract the 4
        StringBuilder idbuff = new StringBuilder();
        boolean started = false;
        for (int i = 0; i < sitedata.length(); i++) {
            char ch = sitedata.charAt(i);
            if (Character.isDigit(ch)) {
                started = true;
                idbuff.append(ch);
            } else if (started) {
                break;
            }
        }
        return Long.parseLong(idbuff.toString());
    }

    public String getSourceName() {
        return (String)this.getSite().get(SiteNames.site);
    }

    /**
     * Use {@link #getSourceName()}
     * @param context
     * @return The name of the source of the contents if this feed
     * @deprecated
     * @see #getSourceName()
     */
    @Deprecated
    public String getSourceName_v1_0_1(Context context) {
        Map sources = App.getPropertiesManager(context).getMap(PropertyName.sources);
        Set keys = sources.keySet();
        String mSiteid = Long.toString(getSiteid());
        for (Object siteid : keys) {
            if (mSiteid.equals(siteid.toString())) {
                return sources.get(siteid).toString();
            }
        }
        return null;
    }

    public String getHeading(String defaultValue) {
        String title = getTitle();
        if (title != null) {
            title = title.trim();
            if (!title.isEmpty()) {
                return title.replaceAll("\\s{2,}", " ");
            }
        }
        String content = getContent();
        if (content != null) {
            content = content.trim();
            if (!content.isEmpty()) {
                return content;
            }
        }
        return defaultValue;
    }

    public String getText(String defaultValue) {
        String output = null;
        String content = getContent();
        if (content != null) {
            content = content.trim();
            if (!content.isEmpty()) {
                output = content;
            }
        }
        if(output == null) {
            String description = this.getDescription();
            if(description != null) {
                description = description.trim();
                if(!description.isEmpty()) {
                    output = description;
                }
            }
        }
        if(output == null) {
            String title = getTitle();
            if (title != null) {
                title = title.trim();
                if (!title.isEmpty()) {
                    output = title.replaceAll("\\s{2,}", " ");
                }
            }
        }

        final String imageUrl = this.getImageUrl();

        if(imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals(this.getSiteIconurl())) {
            final String imageNode = "<img src=\"" + imageUrl + "\"/><br/>";
            if(output != null) {
                if(!output.contains("<img") && !output.contains("<IMG")) {
                    output = imageNode + output;
                }
            }else{
                output = imageNode;
            }
        }

        Logx.getInstance().log(Log.VERBOSE, getClass(), "Text:\n{0}", output);

        return output == null ? defaultValue : output;
    }

    public String getInfo(Context context, String defaultValue, int maxLength) {

        String author;
        String dateStr = (String) getJsonData().get(FeedNames.feeddate);
        String views;
        final int hitcount = this.getHitcount(context);
        if(hitcount < 1) {
            views = "";
        }else if(hitcount == 1) {
            views = hitcount + " view";
        }else { // hitcount > 1
            views = hitcount + " views";
        }

        if(dateStr != null && !dateStr.isEmpty()) {
            dateStr = this.getDateDisplay(dateStr);
        }

        int len_date = dateStr == null ? 0 : dateStr.length();

        final String BY = " by ";

        int otherLength = (len_date + BY.length() + views.length());

        if(otherLength > maxLength) {
            author = "";
        }else{
            author = (String) getJsonData().get(FeedNames.author);
            if(author != null && !author.isEmpty()) {
                author = author.trim().replaceAll("\\s{2,}", " ");
            }
            int len_author = author == null ? 0 : author.length();
            int required = maxLength - otherLength;
            if(len_author > required) {

                final String trailing = "...";
                int tlen = trailing.length();

                if(author != null && required > tlen) {
                    author = author.substring(0, required - tlen) + trailing;
                }else{
                    author = "";
                }
            }
        }

        StringBuilder builder = new StringBuilder(maxLength + 10);
        if(dateStr != null && !dateStr.isEmpty()) {
            builder.append(dateStr);
        }

        // Replace  'This post bla bla bla etc was written by Jane Doe' with 'by Jane Doe'
        if(author != null && !author.isEmpty()) {
            String sLower = author.toLowerCase();
            int x = sLower.indexOf(BY.trim().toLowerCase());
            if(x != -1) {
                author = author.substring(x + BY.length());
            }
            builder.append(BY).append(author);
        }

        if(views != null && !views.isEmpty()) {
            builder.append(' ').append(' ').append(' ').append(' ').append(views);
        }

        return builder.toString();
    }

    public boolean matches(Context context, String category) {
        return matches(context, category, getDefaultMinimumMatchCountAliases(context));
    }

    public boolean matches(Context context, String category, int minMatchCountAliases) {
        String[] feedCatsArr = null;
        String feedTitle = getHeading(null);
        if (feedTitle != null && matchesContent(context, category, feedTitle, minMatchCountAliases)) {
            return true;
        }
        String feedCats = getCategories();
        if (!(feedCats == null || feedCats.isEmpty())) {
            feedCatsArr = feedCats.split(",");
        }
        if (feedCatsArr == null || feedCatsArr.length == 0) {
            return false;
        }
        for (String feedCat : feedCatsArr) {
            if (matchesCategory(context, category, feedCat, minMatchCountAliases)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCategory(Context context, String categoryToMatch, String contentToCheckIfMatches, int minimumMatchCountForAliases) {
        return matches(context, categoryToMatch, contentToCheckIfMatches, AliasType.Categories, minimumMatchCountForAliases);
    }

    private boolean matchesContent(Context context, String categoryToMatch, String contentToCheckIfMatches, int minimumMatchCountForAliases) {
        return matches(context, categoryToMatch, contentToCheckIfMatches, AliasType.Content, minimumMatchCountForAliases);
    }

    private boolean matches(Context context, String preferedCat, String text, AliasType aliasAliasType, int minimumMatchCountForAliases) {
        if (text == null) {
            return false;
        }
        if (matches(preferedCat, text)) {
            return true;
        }
        String[] aliases = App.getAliasesManager(context, aliasAliasType).getAliases(preferedCat);
        if (aliases == null || aliases.length <= 0) {
            return false;
        }
        if (minimumMatchCountForAliases <= 0) {
            throw new AssertionError("minimumMatchCountForAliases <= 0");
        }
        int matchCount = 0;
        for (String alias : aliases) {
            if (matches(alias, text)) {
                matchCount++;
            }
            if (matchCount == minimumMatchCountForAliases) {
                return true;
            }
        }
        return false;
    }

    protected boolean matches(String s0, String s1) {
        s0 = s0.toLowerCase();
        s1 = s1.toLowerCase();
        return s0.contains(s1) || s1.contains(s0);
    }
}

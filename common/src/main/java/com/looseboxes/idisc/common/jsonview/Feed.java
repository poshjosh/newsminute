package com.looseboxes.idisc.common.jsonview;

import android.content.Context;
import android.util.Log;

import com.bc.util.StringComparator;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.FeedhitManager;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;

/**
 * A Feed must have FeedID. Numeral Zero (0) is the default FeedID for any locally
 * created feeds. Also, a Feed must have SiteID. String Zero (0) is the default
 * SiteID for any locally created feeds.
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class Feed extends JsonView {
    public static final long DEFAULT_FEED_ID = 0;
    private static int _mmca;
    private StringBuilder _b_accessViaGetter;
    private String filename;

    public Feed() { }

    public Feed(JSONObject source) {
        super(source);
    }

    public List<JSONObject> getFeedsAfter(List<JSONObject> feeds, Date date) {
        return getFeedsFromDateReference(feeds, date, true);
    }

    public List<JSONObject> getFeedsBefore(List<JSONObject> feeds, Date date) {
        return getFeedsFromDateReference(feeds, date, false);
    }

    private List<JSONObject> getFeedsFromDateReference(List<JSONObject> feeds, Date date, boolean after) {
        List<JSONObject> output = null;
        for (JSONObject o : feeds) {
            setJsonData(o);
            Date feeddate = getDate(FeedNames.feeddate);
            if (feeddate != null && ((after && feeddate.after(date)) || (!after && feeddate.before(date)))) {
                if (output == null) {
                    output = new ArrayList(feeds.size());
                }
                output.add(o);
            }
        }
        return output;
    }

    public JSONObject getMostViewed(Context context, List download, boolean currentUserInclusive) {
        int mostViewedHitcount = -1;
        JSONObject mostViewed = null;
        for (Object obj : download) {
            JSONObject json = (JSONObject)obj;
            setJsonData(json);
            int hitcount = getHitcount(context, currentUserInclusive);
            if (hitcount > mostViewedHitcount) {
                mostViewedHitcount = hitcount;
                mostViewed = json;
            }
        }
        return mostViewed;
    }

    public String getFilename() {
        if (this.filename == null) {
            StringBuilder builder = getReusedStringBuilder();
            builder.append(getFeedid()).append('_');
            String heading = getHeading("page", 100);
            boolean mayAppendDash = false;
            for (int i = 0; i < heading.length(); i++) {
                char ch = heading.charAt(i);
                if (Character.isLetterOrDigit(ch)) {
                    builder.append(ch);
                    mayAppendDash = true;
                } else if (mayAppendDash) {
                    builder.append('-');
                    mayAppendDash = false;
                }
            }
            this.filename = builder.append(".jsp").toString();
        }
        return this.filename;
    }

    public int getEarliestIndex(List<JSONObject> download) {
        return getEarliestIndex(download, FeedNames.feeddate);
    }

    public int getLatestIndex(List<JSONObject> download) {
        return getLatestIndex(download, FeedNames.feeddate);
    }

    public long getEarliestTime(List<JSONObject> download) {
        return getEarliestTime(download, FeedNames.feeddate);
    }

    public long getLatestTime(List<JSONObject> download) {
        return getLatestTime(download, FeedNames.feeddate);
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public boolean contains(Context context, Collection<JSONObject> feeds, JSONObject feed_1) {
        PropertiesManager appProps = App.getPropertiesManager(context);
        return contains(feeds, feed_1, (float) appProps.getInt(PropertyName.textComparisonTolerance), appProps.getInt(PropertyName.textComparisonMaxLength));
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public boolean contains(Collection<JSONObject> feeds, JSONObject feed_1, float textComparisonTolerance, int textComparisonMaxLength) {
        return indexOf(feeds, feed_1, textComparisonTolerance, textComparisonMaxLength) != -1;
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public int indexOf(Context context, Collection<JSONObject> feeds, JSONObject feed_1) {
        PropertiesManager appProps = App.getPropertiesManager(context);
        return indexOf(feeds, feed_1, (float) appProps.getInt(PropertyName.textComparisonTolerance), appProps.getInt(PropertyName.textComparisonMaxLength));
    }

    /**
     * This method does not use the equals method of the objects involved
     */
    public int indexOf(Collection<JSONObject> feeds, JSONObject feed_1, float textComparisonTolerance, int textComparisonMaxLength) {
        StringComparator sc = new StringComparator();
        int pos = -1;
        for (JSONObject feed_0 : feeds) {
            pos++;
            if (matches(feed_0, feed_1, sc, textComparisonTolerance, textComparisonMaxLength)) {
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
        return sc.compare(text_0, text_1, tolerance);
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
                if (Util.equals((Map)feed_0, feed_1, keys)) {
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
        return getHitcount(context, true);
    }

    public int getHitcount(Context context, boolean currentUserInclusive) {
        int c;
        Long feedid = getFeedid();
        int a = FeedhitManager.getHitcount(context, feedid);
        Object oval = getJsonData().get("hitcount");
        String sval = oval == null ? null : oval.toString();
        int b = sval == null ? 0 : Integer.parseInt(sval);
        if (a > b) {
            c = a;
        } else {
            c = b;
        }
        if (currentUserInclusive || !User.getInstance().isReadFeed(context, feedid)) {
            return c;
        }
        return c - 1;
    }

    public Long getFeedid() {
        return (Long) getJsonData().get(FeedhitNames.feedid);
    }

    public String getImageUrl() {
        return (String) getJsonData().get(FeedNames.imageurl);
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

    public long getSiteid() {
        Object data = getJsonData().get(FeedNames.siteid);
        if (!(data instanceof Map)) {
            return getSiteid_v1_0_1(data.toString());
        }
        Long l = (Long) ((Map) data).get(FeedNames.siteid);
        return l == null ? 0 : l.longValue();
    }

    public long getSiteid_v1_0_1(String sitedata) {
// Format:        com.idisc.pu.entities.Site[ siteid=4 ]
// We want to extract the 4
        StringBuilder idbuff = getReusedStringBuilder();
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

    public String getSourceName(Context context) {
        Object data = getJsonData().get(FeedNames.siteid);
        if (data instanceof Map) {
            return (String) ((Map) data).get("site");
        }
        return getSourceName_v1_0_1(context);
    }

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
        String output;
        String content = getContent();
        if (content != null) {
            content = content.trim();
            if (!content.isEmpty()) {
                output = content;
                Logx.log(Log.VERBOSE, getClass(), "Text:\n{0}", output);
                return output;
            }
        }
        String title = getTitle();
        if (title != null) {
            title = title.trim();
            if (!title.isEmpty()) {
                output = title.replaceAll("\\s{2,}", " ");
                Logx.log(Log.VERBOSE, getClass(), "Text:\n{0}", output);
                return output;
            }
        }
        output = defaultValue;
        Logx.log(Log.VERBOSE, getClass(), "Text:\n{0}", output);
        return output;
    }

    public String getInfo(Context context, String defaultValue, int maxLength) {

        String author;
        String dateStr = (String) getJsonData().get(FeedNames.feeddate);
        String views;
        final int hitcount = this.getHitcount(context, true);
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

        StringBuilder builder = this.getReusedStringBuilder();
        if(dateStr != null && !dateStr.isEmpty()) {
            builder.append(dateStr);
        }

        // Replace  'This post bla bla bla etc was written by Jane Doe' with 'by Jane Doe'
        if(author != null && !author.isEmpty()) {
            String sLower = author.toLowerCase();
            int x = sLower.indexOf(BY.toLowerCase());
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

    static {
        _mmca = -1;
    }

    public static int getDefaultMinimumMatchCountAliases(Context context) {
        if (_mmca == -1) {
            _mmca = App.getPropertiesManager(context).getInt(PropertyName.minimumMatchCountForAliases);
        }
        return _mmca;
    }

    private StringBuilder getReusedStringBuilder() {
        if (this._b_accessViaGetter == null) {
            this._b_accessViaGetter = new StringBuilder(20);
        }
        this._b_accessViaGetter.setLength(0);
        return this._b_accessViaGetter;
    }
}

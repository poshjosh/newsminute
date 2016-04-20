package com.looseboxes.idisc.common.jsonview;

import android.content.Context;
import android.text.Html;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.Util;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedSearcher extends Feed {
    private int outputSize;

    /* renamed from: com.looseboxes.idisc.common.jsonview.FeedSearcher.2 */
    class AnonymousClass2 implements Iterator<String> {
        StringBuilder builder;
        int offset;
        final /* synthetic */ int val$len;
        final /* synthetic */ String[] val$parts;
        final /* synthetic */ String val$toFind;

        AnonymousClass2(int i, String str, String[] strArr) {
            this.val$len = i;
            this.val$toFind = str;
            this.val$parts = strArr;
            this.builder = new StringBuilder();
        }

        public boolean hasNext() {
            return this.offset < this.val$len;
        }

        public String next() {
            try {
                this.builder.setLength(0);
                this.builder.append("\\b");
                if (this.offset == 0) {
                    this.builder.append(this.val$toFind);
                } else if (this.offset == 1) {
                    for (int i = 0; i < this.val$parts.length - 1; i++) {
                        this.builder.append(this.val$parts[i]);
                        if (i < this.val$parts.length - 2) {
                            this.builder.append("\\s");
                        }
                    }
                } else if (this.offset == 2) {
                    for (int i = 1; i < this.val$parts.length; i++) {
                        this.builder.append(this.val$parts[i]);
                        if (i < this.val$parts.length - 1) {
                            this.builder.append("\\s");
                        }
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
                this.builder.append("\\b");
                String stringBuilder = this.builder.toString();
                return stringBuilder;
            } finally {
                this.offset++;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    public interface FeedSearchResult {
        Long getFeedId();

        JSONObject getSearchedFeed();

        String getText();
    }

    /* renamed from: com.looseboxes.idisc.common.jsonview.FeedSearcher.1 */
    class AnonymousClass1 implements FeedSearchResult {
        final /* synthetic */ Long val$feedid;
        final /* synthetic */ String val$result;
        final /* synthetic */ JSONObject val$searchedFeed;

        AnonymousClass1(Long l, String str, JSONObject jSONObject) {
            this.val$feedid = l;
            this.val$result = str;
            this.val$searchedFeed = jSONObject;
        }

        public Long getFeedId() {
            return this.val$feedid;
        }

        public String getText() {
            return this.val$result;
        }

        public JSONObject getSearchedFeed() {
            return this.val$searchedFeed;
        }
    }

    public FeedSearcher(Context context) {
        this.outputSize = App.getPropertiesManager(context).getInt(PropertyName.textLengthMedium);
    }

    public FeedSearcher(int outputSize) {
        this.outputSize = outputSize;
    }

    public FeedSearchResult[] getMostCommon(Map<String, FeedSearchResult[]> results) {
        FeedSearchResult[] output = null;
        for (String s : results.keySet()) {
            FeedSearchResult[] arr = (FeedSearchResult[]) results.get(s);
            if (arr.length > -1) {
                output = arr;
            }
        }
        return output;
    }

    public Map<String, FeedSearchResult[]> searchIn(Context context, Collection target) {
        return searchIn(context, target, this.outputSize);
    }

    public Map<String, FeedSearchResult[]> searchIn(Context context, Collection target, int displayLen) {
        this.outputSize = displayLen;
        String autosearchText = Pref.getAutoSearchText(context);
        if (autosearchText == null || autosearchText.isEmpty()) {
            return null;
        }
        String[] toFind = autosearchText.split(",");
        if (toFind.length != 0) {
            return searchFor(new HashSet(Arrays.asList(toFind)), target);
        }
        return null;
    }

    public Map<String, FeedSearchResult[]> searchFor(Set<String> textToFindSet, Collection target) {
        Map<String, FeedSearchResult[]> resultBuffer = new HashMap(textToFindSet.size());
        for (String toFind : textToFindSet) {
            FeedSearchResult[] results = searchFor(toFind, target);
            if (!(results == null || results.length == 0)) {
                resultBuffer.put(toFind, results);
            }
        }
        return resultBuffer;
    }

    public FeedSearchResult[] searchFor(String textToFind, Collection target) {
        List<FeedSearchResult> results = searchFor(Pattern.compile(getPattern(textToFind), Pattern.CASE_INSENSITIVE), target);
        return results == null ? null : (FeedSearchResult[]) results.toArray(new FeedSearchResult[results.size()]);
    }

    public List<FeedSearchResult> searchFor(Pattern pattern, Collection target) {
        List<FeedSearchResult> results = new ArrayList();
        for (Object oval : target) {
            JSONObject json = (JSONObject)oval;
            setJsonData(json);
            String found = match(getTitle(), pattern);
            if (found != null) {
                results.add(getSearchResult(getFeedid(), found, json));
            } else {
                found = match(getPlainText(getContent()), pattern);
                if (found != null) {
                    results.add(getSearchResult(getFeedid(), found, json));
                }
            }
        }
        return results;
    }

    private String getPlainText(String s) {
        return (s == null || s.isEmpty()) ? s : Html.fromHtml(s).toString();
    }

    public FeedSearchResult getSearchResult(Long feedid, String result, JSONObject searchedFeed) {
        return new AnonymousClass1(feedid, result, searchedFeed);
    }

    private String match(String toSearch, Pattern pattern) {
        if (toSearch == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(toSearch);
        if (!matcher.find()) {
            return null;
        }
        if (this.outputSize >= toSearch.length()) {
            return toSearch;
        }
        return Util.truncateSides(toSearch, matcher.group(), toSearch.length() - this.outputSize, true);
    }

    protected String getPattern(String toFind) {
        return toFind;
    }

    private Iterator<String> getPatterns(String toFind) {
        int len = 1;
        String SP = "\\s";
        String[] parts = toFind.split("\\s");
        if (parts.length != 1) {
            len = 3;
        }
        return new AnonymousClass2(len, toFind, parts);
    }

    public int getOutputSize() {
        return this.outputSize;
    }

    public void setOutputSize(int outputSize) {
        this.outputSize = outputSize;
    }
}

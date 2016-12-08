package com.looseboxes.idisc.common.search;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.util.Pref;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by poshjosh on 5/3/2016.
 */
public abstract class FeedSearcher<R> extends Feed {

    class SearchTokens implements Iterator<String> {
        StringBuilder builder;
        int offset;
        final int val$len;
        final String[] val$parts;
        final String val$toFind;

        SearchTokens(int i, String str, String[] strArr) {
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
                    this.builder.append(Pattern.quote(this.val$toFind));
                } else if (this.offset == 1) {
                    for (int i = 0; i < this.val$parts.length - 1; i++) {
                        this.builder.append(Pattern.quote(this.val$parts[i]));
                        if (i < this.val$parts.length - 2) {
                            this.builder.append("\\s");
                        }
                    }
                } else if (this.offset == 2) {
                    for (int i = 1; i < this.val$parts.length; i++) {
                        this.builder.append(Pattern.quote(this.val$parts[i]));
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

    protected FeedSearcher() { }

    public abstract R getResult(String searched, JSONObject data, MatchResult found);

    protected String getTextToSearch(JSONObject json, int index, String outputIfNone, String outputAtEnd) {
        this.setJsonData(json);
        String output;
        switch(index) {
            case 0: output = this.getSourceName(); break;
            case 1: output = this.getTitle(); break;
            case 2: output = this.getPlainText(this.getContent()); break;
            case 3: output = outputAtEnd; break;
            default: throw new UnsupportedOperationException();
        }
        return output != outputAtEnd && output == null ? outputIfNone : output;
    }

    public List<R> getMostCommon(Map<String, List<R>> results) {
        List<R> output = null;
        for (String s : results.keySet()) {
            List<R> list = results.get(s);
            if (output == null) {
                output = list;
            }else{
                if(list.size() > output.size()) {
                    output = list;
                }
            }
        }
        return output;
    }

    public Map<String, List<R>> searchForUserPreferenceWordsToBeNotifiedOf(
            Context context, Collection<JSONObject> target) {
        return searchForUserPreferenceWordsToBeNotifiedOf(context, target, 0, -1);
    }

    public Map<String, List<R>> searchForUserPreferenceWordsToBeNotifiedOf(
            Context context, Collection<JSONObject> target, int limit) {
        return searchForUserPreferenceWordsToBeNotifiedOf(context, target, 0, limit);
    }

    public Map<String, List<R>> searchForUserPreferenceWordsToBeNotifiedOf(
            Context context, Collection<JSONObject> target, int offset, int limit) {
        final String autosearchText = Pref.getAutoSearchText(context);
        if (autosearchText == null || autosearchText.isEmpty()) {
            return null;
        }
        String[] toFind = autosearchText.split(",");
        if (toFind.length != 0) {
            return searchFor(new HashSet(Arrays.asList(toFind)), target, offset, limit);
        }
        return null;
    }

    public Map<String, List<R>> searchForLine(String heading, Collection<JSONObject> target, int offset, int limit) {
        String [] parts = heading.split("\\s");
        if(parts != null && parts.length > 0) {
            Set<String> set = new HashSet<>(parts.length);
            for (String part : parts) {
                if(part.length() > 3) {
                    set.add(part);
                }
            }
            if(!set.isEmpty()) {
                return this.searchFor(set, target, offset, limit);
            }
        }
        return Collections.EMPTY_MAP;
    }

    public Map<String, List<R>> searchFor(Set<String> textToFindSet, Collection<JSONObject> target) {

        return this.searchFor(textToFindSet, target, 0, -1);
    }

    public Map<String, List<R>> searchFor(Set<String> textToFindSet, Collection<JSONObject> target, int offset, int limit) {
        Map<String, List<R>> resultBuffer = new HashMap(textToFindSet.size());
        for (String toFind : textToFindSet) {
            List<R> results = searchFor(toFind, target, offset, limit);
            if (results != null && !results.isEmpty()) {
                resultBuffer.put(toFind, results);
            }
        }
        return resultBuffer;
    }

    public List<R> searchFor(String textToFind, Collection<JSONObject> target) {

        return this.searchFor(textToFind, target, 0, -1);
    }

    public List<R> searchFor(String textToFind, Collection<JSONObject> target, int offset, int limit) {

        List<R> resultsBuffer = limit < 1 ? new ArrayList() : new ArrayList(limit);

        this.searchFor(textToFind, target, offset, limit, resultsBuffer);

        return resultsBuffer;
    }

    public void searchFor(String textToFind, Collection<JSONObject> target, List<R> resultsBuffer) {

        this.searchFor(textToFind, target, 0, -1, resultsBuffer);
    }

    public void searchFor(String textToFind, Collection<JSONObject> target, int offset, int limit,
                          List<R> resultsBuffer) {
        searchFor(
                Pattern.compile(getPattern(textToFind), Pattern.CASE_INSENSITIVE), target, offset, limit, resultsBuffer);
    }

    public List<R> searchFor(Pattern pattern, Collection<JSONObject> target) {

        return this.searchFor(pattern, target, 0, -1);
    }

    public List<R> searchFor(Pattern pattern, Collection<JSONObject> target, int offset, int limit) {

        List<R> resultsBuffer = limit < 1 ? new ArrayList() : new ArrayList(limit);

        this.searchFor(pattern, target, offset, limit, resultsBuffer);

        return resultsBuffer;
    }

    public void searchFor(Pattern pattern, Collection<JSONObject> target, List<R> resultsBuffer) {

        this.searchFor(pattern, target, 0, -1, resultsBuffer);
    }

    public void searchFor(Pattern pattern, Collection<JSONObject> target, int offset, int limit,
                          List<R> resultsBuffer) {

        if(offset < 0) {
            offset = 0;
        }

        int mOffset = 0;

        final Logx logger = Logx.getInstance();
        final Class cls = this.getClass();
        final String regex = pattern.pattern();

        final String NONE = null;
        final String END = "__________";
        for (JSONObject json : target) {

            int textToSearchIndex = -1;

            do{
                ++textToSearchIndex;

                final String searchedText = this.getTextToSearch(json, textToSearchIndex, NONE, END);

                if(searchedText == END) {
                    break;
                }else if(searchedText == NONE) {
                    continue;
                }

                MatchResult found = this.match(searchedText, pattern);

                if(found != null) {

                    if(mOffset >= offset && (limit < 1 || resultsBuffer.size() < limit)) {

                        final R result = getResult(searchedText, json, found);

                        logger.log(Log.VERBOSE, cls, "{0} matches {1} in {2}", regex, found==null?null:found.group(), searchedText);

                        resultsBuffer.add(result);
                    }

                    ++mOffset;
                }
                if(limit > 0 && resultsBuffer.size() >= limit) {
                    break;
                }
            }while(true);
        }
    }

    private String getPlainText(String s) {
        return (s == null || s.isEmpty()) ? s : Html.fromHtml(s).toString();
    }

    private MatchResult match(String toSearch, Pattern pattern) {
        MatchResult output;
        if (toSearch == null) {
            output = null;
        }else {
            Matcher matcher = pattern.matcher(toSearch);
            output = matcher.find() ? matcher.toMatchResult() : null;
        }
        return output;
    }

    protected String getPattern(String toFind) {
        return Pattern.quote(toFind);
    }

    private Iterator<String> getPatterns(String toFind) {
        int len = 1;
        String SP = "\\s";
        String[] parts = toFind.split("\\s");
        if (parts.length != 1) {
            len = 3;
        }
        return new SearchTokens(len, toFind, parts);
    }
}


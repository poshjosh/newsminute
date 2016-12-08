package com.looseboxes.idisc.common.search;

import java.util.regex.MatchResult;

/**
 * Created by Josh on 11/19/2016.
 */
public class SingleMatchResult implements MatchResult {

    private final int index;
    private final String group;

    public SingleMatchResult(String group) {
        this(0, group);
    }

    public SingleMatchResult(int index, String group) {
        this.index = index;
        this.group = group;
    }

    @Override
    public int end() {
        return index + group.length();
    }

    @Override
    public int end(int group) {
        switch (group) {
            case 0: return end();
            default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public String group(int group) {
        switch (group) {
            case 0: return group();
            default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int groupCount() {
        return 1;
    }

    @Override
    public int start() {
        return index;
    }

    @Override
    public int start(int group) {
        switch (group) {
            case 0: return start();
            default: throw new IndexOutOfBoundsException();
        }
    }
}

package com.looseboxes.idisc.common.feedfilters;

import java.util.Collection;

public interface FeedFilterWithAcceptables extends FeedFilter {

    Collection<String> getAcceptables();
}

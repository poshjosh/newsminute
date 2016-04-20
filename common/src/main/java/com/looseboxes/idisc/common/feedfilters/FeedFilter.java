package com.looseboxes.idisc.common.feedfilters;

import com.looseboxes.idisc.common.jsonview.Feed;

public interface FeedFilter {
    boolean accept(Feed feed);
}

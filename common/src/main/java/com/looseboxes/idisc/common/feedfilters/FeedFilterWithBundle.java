package com.looseboxes.idisc.common.feedfilters;

import android.os.Bundle;

public interface FeedFilterWithBundle extends FeedFilter {
    Bundle getBundle();

    void setBundle(Bundle bundle);
}

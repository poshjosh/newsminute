package com.looseboxes.idisc.common.feedfilters;

import android.content.Context;
import android.os.Bundle;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.List;
import java.util.regex.Pattern;

public class FilterByHeadlines extends FilterByCategory {
    private Pattern _hp;

    public FilterByHeadlines(Context context) {
        super(context);
    }

    public FilterByHeadlines(Context context, Bundle bundle) {
        super(context, bundle);
    }

    public FilterByHeadlines(Context context, List<String> categories) {
        super(context, (List) categories);
    }

    public boolean accept(Feed feed) {
        boolean matches;
        String title = feed.getTitle();
        if (title != null) {
            matches = getHeadlinePattern().matcher(title).find();
        } else {
            matches = false;
        }
        if (matches) {
            return matches;
        }
        return super.accept(feed);
    }

    public Pattern getHeadlinePattern() {
        if (this._hp == null) {
            this._hp = Pattern.compile(App.getPropertiesManager(getContext()).getString(PropertyName.headlinePattern), Pattern.CASE_INSENSITIVE);
        }
        return this._hp;
    }
}

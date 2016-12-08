package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;

public class BookmarksActivity extends PreferenceFeedsActivity {

    public BookmarksActivity() {
        super(PreferenceType.bookmarks, R.id.tabs_bookmarks);
    }
}

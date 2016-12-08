package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;

public class FavoritesActivity extends PreferenceFeedsActivity {

    public FavoritesActivity() {
        super(PreferenceType.favorites, R.id.tabs_favorites);
    }
}

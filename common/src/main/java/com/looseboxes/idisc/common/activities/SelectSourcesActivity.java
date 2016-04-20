package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.view.View;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SelectSourcesActivity extends AbstractSelectOptions {
    private Set _c_accessViaGetter;

    public String getPreferenceKeyForStoredUserSelection() {
        return getClass().getName() + ".UserSelection.preferencekey";
    }

    public void onSelection(View view, Set<String> selection) {
        if (selection == null || selection.isEmpty()) {
            finish();
            return;
        }
        Intent intent = new Intent(this, SourcesActivity.class);
        intent.putStringArrayListExtra(SourcesActivity.EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES, new ArrayList(selection));
        startActivity(intent);
    }

    public Set getOptions() {
        if (this._c_accessViaGetter == null) {
            this._c_accessViaGetter = Pref.getAvailableSources(this, new HashSet(App.getPropertiesManager(this).getMap(PropertyName.sources).values()));
        }
        return this._c_accessViaGetter;
    }
}

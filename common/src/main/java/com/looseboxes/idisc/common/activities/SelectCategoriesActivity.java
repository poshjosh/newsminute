package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.view.View;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.ArrayList;
import java.util.Set;

public class SelectCategoriesActivity extends AbstractSelectOptions {
    private Set _c_accessViaGetter;

    public String getPreferenceKeyForStoredUserSelection() {
        return getClass().getName() + ".UserSelection.preferencekey";
    }

    public void onSelection(View view, Set<String> selection) {
        if (selection == null || selection.isEmpty()) {
            finish();
            return;
        }
        Intent intent = new Intent(this, CategoriesActivity.class);
        intent.putStringArrayListExtra(CategoriesActivity.EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES, new ArrayList(selection));
        startActivity(intent);
    }

    public Set getOptions() {
        if (this._c_accessViaGetter == null) {
            this._c_accessViaGetter = Pref.getAvailableCategories(this, App.getPropertiesManager(this).getSet(PropertyName.categories));
        }
        return this._c_accessViaGetter;
    }
}

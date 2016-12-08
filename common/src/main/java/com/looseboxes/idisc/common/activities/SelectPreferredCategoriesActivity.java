package com.looseboxes.idisc.common.activities;

import android.view.View;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.util.Set;

/**
 * Created by Josh on 11/21/2016.
 */
public class SelectPreferredCategoriesActivity extends SelectCategoriesActivity {

    public SelectPreferredCategoriesActivity() { }

    @Override
    public Set getOptions() {
        return App.getPropertiesManager(this).getSet(PropertiesManager.PropertyName.categories);
    }

    public void onSelection(View view, Set<String> selection) {
        Pref.setPreferredCategories(this, selection);
        this.finish();
    }
}

package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CategoriesActivity extends AbstractCategoriesActivity {

    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES = CategoriesActivity.class.getName() + ".SelectedCategories.extrastringset";

    public int getTabId() {
        return R.id.tabs_categories;
    }

    public Set<String> getSelected() {
        ArrayList<String> output = getIntent().getStringArrayListExtra(EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES);
        return output == null ? Collections.EMPTY_SET : new HashSet(output);
    }
}

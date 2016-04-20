package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.R;
import java.util.HashSet;
import java.util.Set;

public class CategoriesActivity extends AbstractCategoriesActivity {
    public static final String EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES;

    static {
        EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES = CategoriesActivity.class.getName() + ".SelectedCategories.extrastringset";
    }

    public int getTabId() {
        return R.id.tabs_categories;
    }

    public Set<String> getCategories() {
        return new HashSet(getIntent().getStringArrayListExtra(EXTRA_STRING_ARRAYLIST_SELECTED_CATEGORIES));
    }
}

package com.looseboxes.idisc.common.ui;

import android.content.Context;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.util.Set;

/**
 * Created by Josh on 9/12/2016.
 */
public class CategoryPreferences extends MultiSelectListPreferenceImpl {

    public CategoryPreferences(Context context) {

        super(context,
                R.string.pref_categories_id,
                R.string.pref_categories_title,
                R.string.pref_categories_summary,
                R.string.pref_categories_dialogtitle,
                getValues(context));
    }

    private static CharSequence [] getValues(Context context) {
        Set<String> categories = App.getPropertiesManager(context).getSet(PropertiesManager.PropertyName.categories);
        return categories.toArray(new CharSequence[categories.size()]);
    }
}

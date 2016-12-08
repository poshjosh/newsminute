package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import java.util.ArrayList;
import java.util.Set;

public class SelectCategoriesActivity extends AbstractSelectOptions {

    public SelectCategoriesActivity() { }

    @Override
    protected void doCreate(Bundle icicle) {

        super.doCreate(icicle);

        TextView titleView = (TextView)findViewById(R.id.selectcategories_title);

        titleView.setText(R.string.msg_selectcategories);
    }

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
        return Pref.getAvailableCategories(this, App.getPropertiesManager(this).getSet(PropertyName.categories));
    }
}

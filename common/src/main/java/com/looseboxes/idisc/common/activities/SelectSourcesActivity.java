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
import java.util.HashSet;
import java.util.Set;

public class SelectSourcesActivity extends AbstractSelectOptions {

    public SelectSourcesActivity() { }

    @Override
    protected void doCreate(Bundle icicle) {

        super.doCreate(icicle);

        TextView titleView = (TextView)findViewById(R.id.selectcategories_title);

        titleView.setText(R.string.msg_selectsources);
    }

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
        final Set options = Pref.getAvailableSources(this, null);
        return options == null ? new HashSet(App.getPropertiesManager(this).getMap(PropertyName.sources).values()) : options;
    }
}

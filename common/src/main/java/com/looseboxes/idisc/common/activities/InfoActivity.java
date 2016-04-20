package com.looseboxes.idisc.common.activities;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.data.InfoProvider;
import com.looseboxes.idisc.common.data.InfoProvider.Category;
import java.util.List;
import org.json.simple.JSONObject;

public class InfoActivity extends DefaultFeedListActivity {
    public static final String EXTRA_STRING_SELECTED_CATEGORY;

    static {
        EXTRA_STRING_SELECTED_CATEGORY = InfoActivity.class.getName() + ".SelectedCategory.extrastring";
    }

    public int getTabId() {
        return R.id.tabs_info;
    }

    public List<JSONObject> getFeedsToDisplay() {
        Category category = Category.getCategory(getIntent().getStringExtra(EXTRA_STRING_SELECTED_CATEGORY));
        if (category != null) {
            return new InfoProvider().getInfo(this, category);
        }
        return null;
    }
}

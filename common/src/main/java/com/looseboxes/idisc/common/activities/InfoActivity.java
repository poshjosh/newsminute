package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.looseboxes.idisc.common.R;

public class InfoActivity extends DefaultFeedListActivity {

    public static final String EXTRA_STRING_SELECTED_CATEGORY = InfoActivity.class.getName() + ".SelectedCategory.extrastring";

    public int getTabId() {
        return R.id.tabs_info;
    }

    @Override
    @CallSuper
    protected void doCreate(Bundle savedInstanceState) {

        super.doCreate(savedInstanceState);

//        Category category = Category.getCategory(getIntent().getStringExtra(EXTRA_STRING_SELECTED_CATEGORY));
//        List<JSONObject> info;
//        if (category != null) {
//            info = new InfoProvider().getInfo(this, category);
//            throw new UnsupportedOperationException("Not yet implemented");
//        }else{
//            info = Collections.EMPTY_LIST;
//        }

//        this.displayFeeds(info, false);
    }
}

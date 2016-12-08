package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.looseboxes.idisc.common.jsonview.DefaultJsonObject;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public class SelectInfoActivity extends DefaultFeedListActivity {

    public int getTabId() {
        return -1;
    }

    @CallSuper
    @Override
    protected void doCreate(Bundle savedInstanceState) {

        super.doCreate(savedInstanceState);

//        Category[] categories = Category.values();

//        List<JSONObject> toDisplay = new ArrayList(categories.length);

//        for (Category category : categories) {
//            toDisplay.add(new DefaultJsonObject(this, category.toString(), "View info about " + category, category));
//        }

//        this.displayFeeds(toDisplay, false);
    }

    public void onListItemClick(int position, long id, boolean mayDisplayAdvert) {
        Object oval = this.getFeedDetailsDisplayHandler().getItem(position, id).get(FeedNames.categories);
        if (oval != null) {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra(InfoActivity.EXTRA_STRING_SELECTED_CATEGORY, oval.toString());
            startActivity(intent);
            return;
        }
        finish();
    }
}

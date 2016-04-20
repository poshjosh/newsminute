package com.looseboxes.idisc.common.activities;

import android.content.Intent;
import com.looseboxes.idisc.common.data.InfoProvider.Category;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.jsonview.JsonView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;

public class SelectInfoActivity extends DefaultFeedListActivity {
    public int getTabId() {
        return -1;
    }

    public Collection<JSONObject> getFeedsToDisplay() {
        Category[] categories = Category.values();
        List<JSONObject> list = new ArrayList(categories.length);
        for (Category category : categories) {
            list.add(JsonView.getDefaultJsonObject(this, category.toString(), "View info about " + category, category));
        }
        return list;
    }

    public void onListItemClick(int position, long id, boolean mayDisplayAdvert) {
        Object oval = ((JSONObject) getFeedListAdapter().getItem(position)).get(FeedNames.categories);
        if (oval != null) {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra(InfoActivity.EXTRA_STRING_SELECTED_CATEGORY, oval.toString());
            startActivity(intent);
            return;
        }
        finish();
    }
}

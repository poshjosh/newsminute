package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

import com.bc.android.core.util.Logx;
import com.idisc.shared.feedid.FeedidsService;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.activities.DisplayFeedActivity;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.util.FeedhitManager;

import org.json.simple.JSONObject;
/**
 * Created by poshjosh on 5/3/2016.
 */
public class FeedDetailsDisplayHandler {

    private boolean finishCurrentActivityBeforeDisplay;
    private Activity context;
    private ListAdapter listAdapter;

    private FeedhitManager feedhitManager;

    public FeedDetailsDisplayHandler(
            Activity context, ListAdapter adapter) {

        this(context, adapter, false);
    }
    public FeedDetailsDisplayHandler(
            Activity context, ListAdapter adapter, boolean finishCurrentActivityBeforeDisplay) {
        this.context = context;
        this.listAdapter = adapter;
        this.finishCurrentActivityBeforeDisplay = finishCurrentActivityBeforeDisplay;
        this.feedhitManager = new FeedhitManager(context);
    }

    public void destroy() {
        this.feedhitManager.destroy();
    }

    public boolean displayFeed(int position, long id) {

        JSONObject feedJson = this.getItem(position, id);

        if (feedJson == null) {
            return false;
        }

        Long feedId = (Long) feedJson.get(FeedhitNames.feedid);

        DefaultApplication app = (DefaultApplication)this.context.getApplication();

        if (feedId != null) {

            this.feedhitManager.addReadFeed(feedId);

            final FeedidsService feedidsService = app.getSharedContext().getFeedidsService();

            final int ival = feedId.intValue();

            if(feedidsService.isReservedFeedid(ival)) {
                if(feedidsService.isNoticeFeedid(ival)) {
                    this.feedhitManager.addReadNotice(feedId);
                }
            }else{
                this.feedhitManager.addFeedhit(feedId);
            }
        }

        displayFeed(feedJson);

        return true;
    }
    public void displayFeed(JSONObject feedJson) {
        displayFeed(feedJson.toJSONString());
    }

    public void displayFeed(String feedJson) {
        Intent intent = new Intent(context, DisplayFeedActivity.class);
        intent.putExtra(DisplayFeedActivity.EXTRA_STRING_SELECTED_FEED_JSON, feedJson);
        displayContent(intent);
    }

    public void displayFeed(Bundle bundle) {
        if (bundle == null) {
            throw new NullPointerException();
        }
        Intent intent = new Intent(context, DisplayFeedActivity.class);
        intent.putExtras(bundle);
        displayContent(intent);
    }

    public void displayContent(Intent intent) {
        if(this.finishCurrentActivityBeforeDisplay) {
            context.finish();
        }
        context.startActivity(intent);
    }

    public JSONObject getItem(int position, long id) {
        Logx.getInstance().log(Log.DEBUG, this.getClass(), "Position: {0}, id: {1}", position, id);
        JSONObject output = null;
        try {
            if (listAdapter.isEmpty()) {
                return null;
            }
            if (id >= 0) {
                for (int pos = 0; pos < listAdapter.getCount(); pos++) {
                    if (listAdapter.getItemId(pos) == id) {
                        output = (JSONObject)listAdapter.getItem(pos);
                        break;
                    }
                }
            }
            if (output != null || position < 0 || position >= listAdapter.getCount()) {
                return output;
            }
            return (JSONObject)listAdapter.getItem(position);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
            return null;
        }
    }
}

package com.looseboxes.idisc.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.looseboxes.idisc.common.activities.CategoriesActivity;
import com.looseboxes.idisc.common.activities.DisplayAdvertActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.PreferenceFeedsActivity;
import com.looseboxes.idisc.common.activities.SearchableActivity;
import com.looseboxes.idisc.common.activities.SourcesActivity;
import com.looseboxes.idisc.common.feedfilters.FeedFilterWithBundle;
import com.looseboxes.idisc.common.feedfilters.FilterByCategory;
import com.looseboxes.idisc.common.feedfilters.FilterByFeedSource;
import com.looseboxes.idisc.common.feedfilters.FilterByHeadlines;
import com.looseboxes.idisc.common.handlers.DefaultLinkHandler;
import com.looseboxes.idisc.common.handlers.LinkHandler;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.looseboxes.idisc.common.handlers.MenuHandlerMain;
import com.looseboxes.idisc.common.handlers.MenuHandlerPreferenceFeeds;
import com.looseboxes.idisc.common.listeners.NoMoreAdvertOnClickListener;
import com.looseboxes.idisc.common.service.ServiceScheduler;
import com.looseboxes.idisc.common.util.AdvertManager;

public class DefaultApplication extends Application {
    private boolean dualPaneMode;
    private boolean dualPaneVisible;

    public void onCreate() {
        super.onCreate();
        App.setContext(getApplicationContext());
        ServiceScheduler.scheduleNetworkService(this);
    }

    public String getAppKey() {
        throw new UnsupportedOperationException("Please get the app key from the Google Play Developer Console: https://play.google.com/apps/publish");
    }

    public AdvertManager createAdvertManager(Activity activity) {
        return new AdvertManager(activity);
    }

    public void onWelcomeProcessFinished(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    public FeedFilterWithBundle createFeedFilter(SearchableActivity context) {
        Bundle bundle = context.getIntent().getBundleExtra("app_data");
        if (bundle == null) {
            return null;
        }
        FeedFilterWithBundle feedFilter;
        String className = bundle.getString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME);
        if (FilterByCategory.class.getName().equals(className)) {
            feedFilter = new FilterByCategory(context);
        } else if (FilterByFeedSource.class.getName().equals(className)) {
            feedFilter = new FilterByFeedSource(context);
        } else if (FilterByHeadlines.class.getName().equals(className)) {
            feedFilter = new FilterByHeadlines(context);
        } else {
            feedFilter = null;
        }
        if (feedFilter == null) {
            return null;
        }
        feedFilter.setBundle(bundle);
        return feedFilter;
    }

    public MenuHandler createMenuHandler(Context context) {
        if ((context instanceof MainActivity) || (context instanceof SourcesActivity) || (context instanceof CategoriesActivity) || (context instanceof HeadlinesActivity)) {
            return new MenuHandlerMain();
        }
        if (context instanceof PreferenceFeedsActivity) {
            return new MenuHandlerPreferenceFeeds();
        }
        return null;
    }

    public LinkHandler createLinkHandler(Context context) {
        return new DefaultLinkHandler(context);
    }

    public OnClickListener createOnClickListener(View view) {
        Context context = view.getContext();
        if (!(context instanceof DisplayAdvertActivity)) {
            return null;
        }
        if (view.getId() != R.id.advertview_nomoreadverts_button) {
            return null;
        }
        OnClickListener listener = new NoMoreAdvertOnClickListener((DisplayAdvertActivity) context);
        view.setOnClickListener(listener);
        return listener;
    }

    public boolean isDualPaneMode() {
        return this.dualPaneMode;
    }

    public void setDualPaneMode(boolean dualPaneMode) {
        this.dualPaneMode = dualPaneMode;
    }

    public boolean isDualPaneVisible() {
        return this.dualPaneVisible;
    }

    public void setDualPaneVisible(boolean dualPaneVisible) {
        this.dualPaneVisible = dualPaneVisible;
    }
}

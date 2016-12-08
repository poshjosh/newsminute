package com.looseboxes.idisc.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.View.OnClickListener;

import com.bc.android.core.AbstractNewsminuteApplication;
import com.looseboxes.idisc.common.activities.CategoriesActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.PreferenceFeedsActivity;
import com.looseboxes.idisc.common.activities.SearchableActivity;
import com.looseboxes.idisc.common.activities.SourcesActivity;
import com.looseboxes.idisc.common.feedfilters.FeedFilter;
import com.looseboxes.idisc.common.feedfilters.FilterByCategory;
import com.looseboxes.idisc.common.feedfilters.FilterByFeedSource;
import com.looseboxes.idisc.common.feedfilters.FilterByHeadlines;
import com.looseboxes.idisc.common.fragments.DisplayAdvertFragment;
import com.looseboxes.idisc.common.handlers.DefaultLinkHandler;
import com.looseboxes.idisc.common.handlers.LinkHandler;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.looseboxes.idisc.common.handlers.MenuHandlerMain;
import com.looseboxes.idisc.common.handlers.MenuHandlerPreferenceFeeds;
import com.looseboxes.idisc.common.listeners.NoMoreAdvertOnClickListener;
import com.looseboxes.idisc.common.service.ServiceSchedulerImpl;
import com.looseboxes.idisc.common.util.AdvertManager;
import com.looseboxes.idisc.common.util.PropertiesManager;

public class DefaultApplication extends AbstractNewsminuteApplication {

    @Override
    protected void doOnCreate() {

        App.setContext(getApplicationContext());

        super.doOnCreate();

        new ServiceSchedulerImpl().scheduleNetworkService(this);
    }

    public String getAppKey() {
        throw new UnsupportedOperationException("Please get the app key from the Google Play Developer Console: https://play.google.com/apps/publish");
    }

    public int getMaxLogFiles() {
        return (int) PropertiesManager.PropertyName.logFileCount.getDefaultNumber();
    }

    public int getLogFilelimit() {
        return (int) PropertiesManager.PropertyName.logFileLimit.getDefaultNumber();
    }

    public @StyleRes
    int getPopupStyle(int defaultIfNone) {
        return R.style.DialogAppTheme;
    }

    public AdvertManager createAdvertManager(Activity activity) {
        return new AdvertManager(activity);
    }

    public void onWelcomeProcessFinished(Context context) { }

    public FeedFilter createFeedFilter(SearchableActivity context) {
        final Bundle bundle = context.getIntent().getBundleExtra("app_data");
        if (bundle == null) {
            return null;
        }
        FeedFilter feedFilter;
        String className = bundle.getString(SearchableActivity.EXTRA_STRING_FEEDFILTER_CLASSNAME);
        if (FilterByCategory.class.getName().equals(className)) {
            feedFilter = new FilterByCategory(context, bundle);
        } else if (FilterByFeedSource.class.getName().equals(className)) {
            feedFilter = new FilterByFeedSource(context, bundle);
        } else if (FilterByHeadlines.class.getName().equals(className)) {
            feedFilter = new FilterByHeadlines(context, bundle);
        } else {
            feedFilter = null;
        }
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

    public OnClickListener initNoMoreAdvertsOnClickListener(DisplayAdvertFragment displayAdvertFragment) {
        final View nomoreadverts = displayAdvertFragment.findViewById(R.id.advertview_nomoreadverts_button);
        if (nomoreadverts == null) {
            return null;
        }else {
            OnClickListener listener = new NoMoreAdvertOnClickListener(displayAdvertFragment.getActivity());
            nomoreadverts.setOnClickListener(listener);
            return listener;
        }
    }
}

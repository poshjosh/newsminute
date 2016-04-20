package com.looseboxes.idisc.googleservicesextras;

import android.app.Activity;
import android.content.Context;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.activities.CategoriesActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.SourcesActivity;
import com.looseboxes.idisc.common.handlers.LinkHandler;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.looseboxes.idisc.common.util.AdvertManager;

public class ApplicationWithGoogleServices extends DefaultApplication {
    public AdvertManager createAdvertManager(Activity activity) {
        return new GoogleAdsManager(activity);
    }

    public MenuHandler createMenuHandler(Context context) {
        if ((context instanceof MainActivity) || (context instanceof SourcesActivity) || (context instanceof CategoriesActivity) || (context instanceof HeadlinesActivity)) {
            return new MenuHandlerMainWithAppInvite();
        }
        return super.createMenuHandler(context);
    }

    public LinkHandler createLinkHandler(Context context) {
        return new LinkHandlerAppInvite(context);
    }
}

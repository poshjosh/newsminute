package com.looseboxes.idisc.appbilling;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import com.looseboxes.idisc.common.activities.CategoriesActivity;
import com.looseboxes.idisc.common.activities.DisplayAdvertActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.SourcesActivity;
import com.looseboxes.idisc.common.fragments.DisplayAdvertFragment;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.looseboxes.idisc.common.listeners.NoMoreAdvertOnClickListener;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.looseboxes.idisc.googleservicesextras.ApplicationWithGoogleServices;

public class ApplicationWithInAppPurchase extends ApplicationWithGoogleServices {

    class BillingManagerNoUI extends BillingManager {
        BillingManagerNoUI(Context x0, String x1) {
            super(x0, x1);
        }
        @Override
        protected void showWarning(String message) { }
    }

    @Override
    public void doOnCreate() {
        super.doOnCreate();
        setUpBilling();
    }

    public void setUpBilling() {
        if (NewsminuteUtil.isNetworkConnectedOrConnecting(this)) {
            new BillingManagerNoUI(this, getAppKey()).startSetup();
        }
    }

    public MenuHandler createMenuHandler(Context context) {
        if ((context instanceof MainActivity) || (context instanceof SourcesActivity) || (context instanceof CategoriesActivity) || (context instanceof HeadlinesActivity)) {
            return new MenuHandlerMainWithActivateSubscription();
        }
        return super.createMenuHandler(context);
    }

    public OnClickListener initNoMoreAdvertsOnClickListener(DisplayAdvertFragment displayAdvertFragment) {
        final View nomoreadverts = displayAdvertFragment.findViewById(com.looseboxes.idisc.common.R.id.advertview_nomoreadverts_button);
        if (nomoreadverts == null) {
            return null;
        }else {
            OnClickListener listener = new NoMoreAdvertOnClickListenerInAppPurchase(displayAdvertFragment.getActivity());
            nomoreadverts.setOnClickListener(listener);
            return listener;
        }
    }
}

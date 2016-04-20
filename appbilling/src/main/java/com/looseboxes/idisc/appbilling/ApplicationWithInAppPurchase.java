package com.looseboxes.idisc.appbilling;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import com.looseboxes.idisc.common.activities.CategoriesActivity;
import com.looseboxes.idisc.common.activities.DisplayAdvertActivity;
import com.looseboxes.idisc.common.activities.HeadlinesActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.activities.SourcesActivity;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.looseboxes.idisc.common.util.Util;
import com.looseboxes.idisc.googleservicesextras.ApplicationWithGoogleServices;

public class ApplicationWithInAppPurchase extends ApplicationWithGoogleServices {

    /* renamed from: com.looseboxes.idisc.appbilling.ApplicationWithInAppPurchase.1 */
    class AnonymousClass1 extends BillingManager {
        AnonymousClass1(Context x0, String x1) {
            super(x0, x1);
        }

        protected void showWarning(String message) {
        }
    }

    public void onCreate() {
        super.onCreate();
        setUpBilling();
    }

    public void setUpBilling() {
        if (Util.isNetworkConnectedOrConnecting(this)) {
            new AnonymousClass1(this, getAppKey()).startSetup();
        }
    }

    public MenuHandler createMenuHandler(Context context) {
        if ((context instanceof MainActivity) || (context instanceof SourcesActivity) || (context instanceof CategoriesActivity) || (context instanceof HeadlinesActivity)) {
            return new MenuHandlerMainWithActivateSubscription();
        }
        return super.createMenuHandler(context);
    }

    public OnClickListener createOnClickListener(View view) {
        Context context = view.getContext();
        if (!(context instanceof DisplayAdvertActivity)) {
            return super.createOnClickListener(view);
        }
        if (view.getId() != R.id.advertview_nomoreadverts_button) {
            return super.createOnClickListener(view);
        }
        OnClickListener listener = new NoMoreAdvertOnClickListenerInAppPurchase((DisplayAdvertActivity) context);
        view.setOnClickListener(listener);
        return listener;
    }
}

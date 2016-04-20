package com.looseboxes.idisc.appbilling;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabHelper.OnIabSetupFinishedListener;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.Util;

import java.util.concurrent.TimeUnit;

public class BillingManager extends IabHelper implements OnIabSetupFinishedListener {
    public static final int RC_REQUEST = 10001;
    OnConsumeFinishedListener mConsumeFinishedListener;
    QueryInventoryFinishedListener mGotInventoryListener;

    /* renamed from: com.looseboxes.idisc.appbilling.BillingManager.1 */
    class AnonymousClass1 extends AsyncTask {
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ OnIabPurchaseFinishedListener val$purchaseFinishedListener;
        final /* synthetic */ String val$sku;

        AnonymousClass1(Activity activity, String str, OnIabPurchaseFinishedListener onIabPurchaseFinishedListener) {
            this.val$activity = activity;
            this.val$sku = str;
            this.val$purchaseFinishedListener = onIabPurchaseFinishedListener;
        }

        protected Object doInBackground(Object[] params) {
            try {
                BillingManager.this.launchSubscriptionPurchaseFlow(this.val$activity, this.val$sku, BillingManager.RC_REQUEST, this.val$purchaseFinishedListener, BillingManager.this.generatePayload());
            } catch (Exception e) {
                Logx.log(getClass(), e);
                publishProgress(new Object[]{e});
            }
            return null;
        }

        protected void onProgressUpdate(Object[] values) {
            if (values[0] instanceof Exception) {
                Logx.log(getClass(), (Exception) values[0]);
            } else {
                Logx.log(4, getClass(), values[0]);
            }
        }
    }

    public BillingManager(Context context, String appKey) {
        super(context.getApplicationContext(), appKey);
        this.mGotInventoryListener = new QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                try {
                    Logx.debug(getClass(), "Query in-app inapppurchase inventory request returned");
                    if (result.isFailure()) {
                        BillingManager.this.showWarning(BillingManager.this.getContext().getString(R.string.err_subscription));
                        BillingManager.this.logWarn("Failed to query in-app inapppurchase inventory: " + result);
                        return;
                    }
                    Logx.debug(getClass(), "Query in-app inapppurchase successful");
                    boolean version2 = false;
                    if (!BillingManager.this.subscriptionsSupported()) {
                        version2 = BillingManager.this.updateSubscriptionInfoV2(inventory);
                    } else if (!BillingManager.this.updateSubscriptionInfoV3(inventory)) {
                        version2 = BillingManager.this.updateSubscriptionInfoV2(inventory);
                    }
                    boolean subscriptionActive = Pref.isSubscriptionActive(BillingManager.this.getContext());
                    String subscriptionType = Pref.getSubscriptionSku(BillingManager.this.getContext());
                    Logx.debug(getClass(), "User subscription active: {0}, type: {1}", Boolean.valueOf(subscriptionActive), subscriptionType);
                    if (!version2 || subscriptionType == null || subscriptionActive) {
                        Logx.debug(getClass(), "Initial inventory query completed");
                    } else {
                        BillingManager.this.consumeAsync(inventory.getPurchase(subscriptionType), BillingManager.this.mConsumeFinishedListener);
                    }
                } catch (Exception e) {
                    Logx.log(getClass(), e);
                }
            }
        };
        this.mConsumeFinishedListener = new OnConsumeFinishedListener() {
            public void onConsumeFinished(Purchase purchase, IabResult result) {
                try {
                    Logx.debug(getClass(), "Consumption of {0} completed. Result: {1}, original purchase: {2}", purchase.getSku(), result, purchase);
                    if (result.isSuccess()) {
                        Pref.clearSubscription(BillingManager.this.getContext());
                        Logx.debug(getClass(), "Consumption successful");
                    } else {
                        BillingManager.this.showWarning(BillingManager.this.getContext().getString(R.string.err_subscription));
                        BillingManager.this.logWarn("Error while consuming: " + result);
                    }
                    Logx.log(Log.VERBOSE, getClass(), "End consumption flow");
                } catch (Exception e) {
                    Logx.log(getClass(), e);
                }
            }
        };
        Logx.debug(getClass(), "Package Name: {0}", context.getApplicationContext().getPackageName());
        enableDebugLogging(Logx.isDebugMode());
    }

    public boolean launchSubscriptionPurchaseFlow(String sku, Activity activity, OnIabPurchaseFinishedListener purchaseFinishedListener) {
        Logx.debug(getClass(), "User clicked button to purchase: {0}", sku);
        String currentSubscription = Pref.getSubscriptionSku(activity);
        if (currentSubscription == null || !User.getInstance().isActivated(activity)) {
            Logx.debug(getClass(), "Launching purchase flow for: {0}", sku);
            new AnonymousClass1(activity, sku, purchaseFinishedListener).execute((Object[]) null);
            return true;
        }
        String msg = activity.getString(R.string.err_alreadysubscribed_s, new Object[]{currentSubscription});
        showWarning(msg);
        logWarn(msg);
        return false;
    }

    public void startSetup() {
        startSetup(this);
    }

    public void onIabSetupFinished(IabResult result) {
        try {
            Logx.debug(getClass(), "in-app purchase setup request returned");
            Pref.setSubscriptionSupported(getContext(), subscriptionsSupported());
            if (result.isSuccess()) {
                Logx.debug(getClass(), "In-app purchase setup successful. Now Querying inventory");
                queryInventoryAsync(this.mGotInventoryListener);
                return;
            }
            showWarning(getContext().getString(R.string.err_subscription));
            logWarn("Problem setting up subscription: " + result);
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private boolean updateSubscriptionInfoV3(Inventory inventory) {
        return updateSubscriptionInfo(inventory, SkuData.getSkus(true));
    }

    private boolean updateSubscriptionInfoV2(Inventory inventory) {
        return updateSubscriptionInfo(inventory, SkuData.getSkus(false));
    }

    private boolean updateSubscriptionInfo(Inventory inventory, String[] subscriptionTypes) {
        boolean available = false;
        for (String sku : subscriptionTypes) {
            Purchase purchase = inventory.getPurchase(sku);
            available = purchase != null;
            if (available) {
                boolean subscriptionActive;
                Pref.setSubscriptionSku(getContext(), sku);
                if (!verifyDeveloperPayload(purchase)) {
                    showWarning(getContext().getString(R.string.err_subscription));
                    logWarn("Error querying purchase data. Authenticity verification failed.");
                    subscriptionActive = false;
                } else if (!IabHelper.ITEM_TYPE_INAPP.equals(purchase.getItemType())) {
                    subscriptionActive = true;
                } else if (System.currentTimeMillis() - purchase.getPurchaseTime() < TimeUnit.DAYS.toMillis((long) SkuData.getMaxPeriodDays(sku))) {
                    subscriptionActive = true;
                } else {
                    subscriptionActive = false;
                }
                Pref.setSubscritpionActive(getContext(), subscriptionActive);
                return available;
            }
        }
        return available;
    }

    public boolean verifyDeveloperPayload(Purchase p) {

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return generatePayload().equals(p.getDeveloperPayload());
    }

    public String generatePayload() {
        // If you use emailAddress here then the user must be logged-in
//        String value = User.getInstance().getEmailAddress(this.getContext());
//        if(value == null) {
//            throw new NullPointerException();
//        }
        String value = "1mXa9u";
        String payload = Util.encrpyt(value, "vM3cEfe7GdHgmNVv", 128);
        return payload;

    }

    public boolean isSubscriptionsSupported() {
        return super.subscriptionsSupported();
    }

    protected void showWarning(String message) {
        Popup.alert(getContext(), message);
    }

    protected void logDebug(String msg) {
        if (isEnableDebugLogging()) {
            super.logDebug(msg);
            Logx.debug(getClass(), msg);
        }
    }

    protected void logError(String msg) {
        super.logError(msg);
        Logx.log(6, getClass(), msg);
    }

    protected void logWarn(String msg) {
        super.logWarn(msg);
        Logx.log(5, getClass(), msg);
    }
}

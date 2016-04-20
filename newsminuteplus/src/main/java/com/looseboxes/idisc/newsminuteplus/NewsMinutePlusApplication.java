package com.looseboxes.idisc.newsminuteplus;

import android.content.Context;
import android.content.Intent;

import com.looseboxes.idisc.appbilling.ApplicationWithInAppPurchase;
import com.looseboxes.idisc.appbilling.InAppPurchaseActivity;
import com.looseboxes.idisc.appbilling.SkuData;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;

/**
 * @author Chinomso Ikwuagwu on 11/18/2015.
 */
public class NewsMinutePlusApplication extends ApplicationWithInAppPurchase {

    //@todo
    /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    public final String getAppKey() {
        String s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAum+e3zz25ekGWqimaPNtLMsGnqQOfm4YyNMzyYdauvegjRPhTKgh58Aj0dbgLOXSTPmwV+qMos35IXfrzYcztE/fOHKlNE4rCXI3uL/V93yfsYJrwK7MiSCt9oyXB7u+RG7ARwOGKDg2UDNN0bNapazK+IL+pnIyVI8A8SvMFE3d/fmNINetNvu+ZBUEVaKc/3zcxFtq3/4EJt3TTwpxMaTlGRQ8A5e9EqoHPdCf70HlEt27SPZJKlncMVI03XHVT8fWius2Nk9w04pfaGr4XwcquR/GjzcwVhHBVWSGmLo77AcJJyU5VeCMPy7GHFNOaGls8aeMEkUsWd+4yW7tTQIDAQAB";
        return s.replaceAll("\\s", "");
    }

    public void setUpBilling() {
        // Order of method calls important
        this.grantInitialSubscription();
        super.setUpBilling();
    }

    private void grantInitialSubscription() {
        try {
            if(!App.isInstalled(this)) {
                // Purchasing the News Minute Plus app automatically grants the purchaser 12 months
                // subscription as a result of the amount paid for the app
                //
                // Subscriptions are now supported so no need for SkuData.SKU_12MONTHS_PURCHASE
                //
                Pref.setInitiallyGrantedSubscription(this, SkuData.SKU_12MONTHS_SUBSCRIPTION);
            }
        }catch(Exception e) {
            Logx.log(this.getClass(), e);
        }
    }

    @Override
    public void onWelcomeProcessFinished(Context context) {
        try {

            if(!Pref.isSubscriptionActive(context)) {

                String skuToActivate = Pref.getInitiallyGrantedSubscription(context);

                if(skuToActivate != null && Pref.getSubscriptionSku(context) == null) {

                    Intent intent = new Intent(context, InAppPurchaseActivity.class);
                    intent.putExtra(InAppPurchaseActivity.EXTRA_STRING_USER_SUBSCRIPTION_SKU, skuToActivate);
                    intent.putExtra(InAppPurchaseActivity.EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_SUCCESSFUL, true);
                    intent.putExtra(InAppPurchaseActivity.EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_FAILED, true);

                    context.startActivity(intent);

                    return;
                }
            }

            super.onWelcomeProcessFinished(context);

        }catch(Exception e) {
            Logx.log(this.getClass(), e);
        }
    }
}

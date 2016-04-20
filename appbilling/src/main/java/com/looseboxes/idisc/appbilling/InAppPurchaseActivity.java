package com.looseboxes.idisc.appbilling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import com.android.vending.billing.util.IabHelper.OnIabSetupFinishedListener;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.Util;

public class InAppPurchaseActivity extends Activity {
    public static final String EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_FAILED;
    public static final String EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_SUCCESSFUL;
    public static final String EXTRA_STRING_USER_SUBSCRIPTION_SKU;
    private TextView _msg;
    private BillingManager billingManager;
    OnIabPurchaseFinishedListener mPurchaseFinishedListener;

    private class InAppPurchaseClickedListener implements OnClickListener {
        private InAppPurchaseClickedListener() {
        }

        public void onClick(View v) {
            try {
                boolean subscriptionSupported = Pref.isSubscriptionSupported(InAppPurchaseActivity.this);
                String sku = null;
                for (int id : SkuData.getButtonIds()) {
                    if (((RadioButton) InAppPurchaseActivity.this.findViewById(id)).isChecked()) {
                        sku = SkuData.getSkuForButtonId(id, subscriptionSupported);
                        break;
                    }
                }
                if (sku != null) {
                    InAppPurchaseActivity.this.onPurchaseClicked(sku);
                } else {
                    Popup.show(v, R.string.err_nothingselected, 0);
                }
            } catch (Exception e) {
                InAppPurchaseActivity.this.logUnexpectedError(e);
            }
        }
    }

    public enum Progress {
        purchaseInitiated,
        responseReceived,
        handlingResponse,
        doneHandlingResponse,
        purchaseSuccessful,
        errorHandlingResponse
    }

    /* renamed from: com.looseboxes.idisc.appbilling.InAppPurchaseActivity.1 */
    class AnonymousClass1 implements OnIabSetupFinishedListener {
        final /* synthetic */ String val$sku;

        AnonymousClass1(String str) {
            this.val$sku = str;
        }

        public void onIabSetupFinished(IabResult result) {
            InAppPurchaseActivity.this.billingManager.onIabSetupFinished(result);
            if (result.isSuccess()) {
                InAppPurchaseActivity.this.launchSubscriptionWorkflow(this.val$sku);
            }
        }
    }

    public InAppPurchaseActivity() {
        this.mPurchaseFinishedListener = new OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                try {
                    InAppPurchaseActivity.this.updateProgress(Progress.doneHandlingResponse);
                    Logx.debug(getClass(), "Done Parsing response from server");
                    Logx.debug(getClass(), "Purchase of {0} completed. Result: {1}, original purchase: {2}", purchase.getSku(), result, purchase);
                    if (InAppPurchaseActivity.this.billingManager != null) {
                        if (result.isFailure()) {
                            InAppPurchaseActivity.this.showWarning("Error purchasing: " + result);
                            InAppPurchaseActivity.this.revertText();
                        } else if (InAppPurchaseActivity.this.billingManager.verifyDeveloperPayload(purchase)) {
                            InAppPurchaseActivity.this.updateProgress(Progress.purchaseSuccessful);
                            Logx.debug(getClass(), "Purchase successful");
                            Logx.debug(getClass(), "Purchase successful");
                            Pref.setSubscriptionSku(InAppPurchaseActivity.this, purchase.getSku());
                            Pref.setSubscritpionActive(InAppPurchaseActivity.this, true);
                            InAppPurchaseActivity.this.revertText();
                        } else {
                            InAppPurchaseActivity.this.showWarning("Error purchasing. Authenticity verification failed.");
                            InAppPurchaseActivity.this.revertText();
                        }
                    }
                } catch (Exception e) {
                    InAppPurchaseActivity.this.updateProgress(Progress.errorHandlingResponse);
                    Logx.log(getClass(), e);
                } finally {
                    InAppPurchaseActivity.this.revertText();
                }
            }
        };
    }

    static {
        EXTRA_STRING_USER_SUBSCRIPTION_SKU = InAppPurchaseActivity.class.getName() + ".subscription.sku";
        EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_SUCCESSFUL = InAppPurchaseActivity.class.getName() + ".subscription.finishOnInitiationSuccessful";
        EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_FAILED = InAppPurchaseActivity.class.getName() + ".subscription.finishOnInitiationFailed";
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.inapppurchase);
        boolean networkAvailable = Util.isNetworkConnectedOrConnecting(this);
        this.billingManager = createBillingManager();
        if (networkAvailable && !this.billingManager.isSetupDone()) {
            this.billingManager.startSetup();
        }
        for (int buttonId : SkuData.getButtonIds()) {
            ((Button) findViewById(buttonId)).setText(SkuData.getButtonText(this, buttonId));
        }
        Button button = (Button) findViewById(R.id.inapppurchase_ok_button);
        button.setEnabled(true);
        button.setOnClickListener(new InAppPurchaseClickedListener());
        if (networkAvailable) {
            String sku = getIntent().getStringExtra(EXTRA_STRING_USER_SUBSCRIPTION_SKU);
            if (sku != null) {
                ((TextView) findViewById(R.id.inapppurchase_message_view)).setText(getString(R.string.msg_activatingsubscription_s, new Object[]{SkuData.getLabel(this, sku)}));
                onPurchaseClicked(sku);
                return;
            }
            return;
        }
        Popup.show((Activity) this, R.string.err_noconnection, 0);
    }

    private BillingManager createBillingManager() {
        return new BillingManager(this, ((DefaultApplication) getApplication()).getAppKey());
    }

    private void revertText() {
        ((TextView) findViewById(R.id.inapppurchase_message_view)).setText(R.string.msg_select_subscription);
    }

    public void onPurchaseClicked(String sku) {
        try {
            Logx.debug(getClass(), "User clicked button to purchase: {0}", sku);
            if (!Util.isNetworkConnectedOrConnecting(this)) {
                Popup.show((Activity) this, R.string.err_noconnection, 0);
            } else if (this.billingManager.isSetupDone()) {
                launchSubscriptionWorkflow(sku);
            } else {
                this.billingManager.startSetup(new AnonymousClass1(sku));
            }
        } catch (Exception e) {
            logUnexpectedError(e);
        }
    }

    private boolean launchSubscriptionWorkflow(String sku) {
        boolean launchedSuccessfully = this.billingManager.launchSubscriptionPurchaseFlow(sku, this, this.mPurchaseFinishedListener);
        if (launchedSuccessfully) {
            updateProgress(Progress.purchaseInitiated);
            Logx.debug(getClass(), "Initiated subscription purchase flow");
            findViewById(R.id.inapppurchase_ok_button).setEnabled(false);
        }
        Intent intent = getIntent();
        if ((launchedSuccessfully && intent.hasExtra(EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_SUCCESSFUL)) || (!launchedSuccessfully && intent.hasExtra(EXTRA_BOOLEAN_FINISH_PURCHASE_INITIATION_FAILED))) {
            finish();
        }
        return launchedSuccessfully;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        revertText();
        updateProgress(Progress.responseReceived);
        Logx.debug(getClass(), "Received response from server");
        try {
            Logx.debug(getClass(), "#onActivityResult. Request code: {0}, result code: {1}, intent: {2}", Integer.valueOf(requestCode), Integer.valueOf(resultCode), data);
            if (this.billingManager != null) {
                updateProgress(Progress.handlingResponse);
                Logx.debug(getClass(), "Parsing response from server");
                if (this.billingManager.handleActivityResult(requestCode, resultCode, data)) {
                    Logx.log(Log.VERBOSE, getClass(), "onActivityResult handled by IABUtil.");
                    return;
                }
                Logx.debug(getClass(), "onActivityResult NOT handled by IABUtil.");
                super.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            logUnexpectedError(e);
        }
    }

    private void logUnexpectedError(Exception e) {
        Logx.log(getClass(), e);
        if (this.mPurchaseFinishedListener != null) {
            this.mPurchaseFinishedListener.onIabPurchaseFinished(new IabResult(IabHelper.IABHELPER_UNKNOWN_ERROR, "Unexpected Error"), null);
        }
    }

    public void onDestroy() {
        Logx.debug(getClass(), "Destroying in-app inapppurchase helper");
        if (this.billingManager != null) {
            try {
                this.billingManager.dispose();
                this.billingManager = null;
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
        super.onDestroy();
    }

    private void updateProgress(Progress progress) {
        getMessageView().setText(getLabel(progress));
    }

    private String getLabel(Progress progress) {
        StringBuilder builder = new StringBuilder();
        int ordinal = progress.ordinal();
        for (int i = 0; i < ordinal + 1; i++) {
            builder.append('O').append('O');
        }
        return builder.toString();
    }

    private TextView getMessageView() {
        if (this._msg == null) {
            this._msg = (TextView) findViewById(R.id.inapppurchase_message_view);
        }
        return this._msg;
    }

    protected void showWarning(String message) {
        Logx.log(5, getClass(), message);
        Popup.alert(this, message);
    }
}

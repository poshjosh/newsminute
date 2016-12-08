package com.looseboxes.idisc.appbilling;

import android.content.Context;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

import java.util.Arrays;

public class SkuData {

/////////////////////////////////////////////////////////////////////
//
// Both 'NewMinute Free' and 'News Minute Plus' use these product IDs
//
/////////////////////////////////////////////////////////////////////

    public static final String SKU_12MONTHS_PURCHASE = "com.looseboxes.idisc.newsminuteplus.purchase.12months";
    public static final String SKU_12MONTHS_SUBSCRIPTION = "com.looseboxes.idisc.newsminuteplus.subscription.12months";
    public static final String SKU_1MONTH_PURCHASE = "com.looseboxes.idisc.newsminuteplus.purchase.1month";
    public static final String SKU_1MONTH_SUBSCRIPTION = "com.looseboxes.idisc.newsminuteplus.subscription.1month";
    public static final String SKU_3MONTHS_PURCHASE = "com.looseboxes.idisc.newsminuteplus.purchase.3months";
    public static final String SKU_3MONTHS_SUBSCRIPTION = "com.looseboxes.idisc.newsminuteplus.subscription.3months";
    public static final String SKU_6MONTHS_PURCHASE = "com.looseboxes.idisc.newsminuteplus.purchase.6months";
    public static final String SKU_6MONTHS_SUBSCRIPTION = "com.looseboxes.idisc.newsminuteplus.subscription.6months";

    public static int[] getButtonIds() {
        return new int[]{R.id.inapppurchase_button_12months, R.id.inapppurchase_button_6months, R.id.inapppurchase_button_3months, R.id.inapppurchase_button_1month};
    }

    public static String getButtonText(Context context, int buttonResourceId) {
        PropertiesManager pm = App.getPropertiesManager(context);
        String buttonText;
        if (buttonResourceId == R.id.inapppurchase_button_12months) {
            buttonText = context.getString(R.string.msg_12months_subscription_s, new Object[]{Double.valueOf(pm.getDouble(PropertyName.price12MonthsNaira))});
            return Logx.getInstance().isDebugMode() ? buttonText + "\nOR android.test.purchased" : buttonText;
        } else if (buttonResourceId == R.id.inapppurchase_button_6months) {
            buttonText = context.getString(R.string.msg_6months_subscription_s, new Object[]{Double.valueOf(pm.getDouble(PropertyName.price6MonthsNaira))});
            if (Logx.getInstance().isDebugMode()) {
                return buttonText + "\nOR android.test.canceled";
            }
            return buttonText;
        } else if (buttonResourceId == R.id.inapppurchase_button_3months) {
            buttonText = context.getString(R.string.msg_3months_subscription_s, new Object[]{Double.valueOf(pm.getDouble(PropertyName.price3MonthsNaira))});
            if (Logx.getInstance().isDebugMode()) {
                return buttonText + "\nOR android.test.refunded";
            }
            return buttonText;
        } else if (buttonResourceId == R.id.inapppurchase_button_1month) {
            buttonText = context.getString(R.string.msg_1month_subscription_s, new Object[]{Double.valueOf(pm.getDouble(PropertyName.price1MonthNaira))});
            if (Logx.getInstance().isDebugMode()) {
                return buttonText + "\nOR android.test.item_unavailable";
            }
            return buttonText;
        } else {
            throw new IllegalArgumentException("Unexpected button resource id: " + buttonResourceId);
        }
    }

    public static String[] getSkus(boolean inappv3enabled) {
        if (Logx.getInstance().isDebugMode()) {
            return new String[]{"android.test.item_unavailable", "android.test.refunded", "android.test.canceled", "android.test.purchased"};
        } else if (inappv3enabled) {
            return new String[]{SKU_1MONTH_SUBSCRIPTION, SKU_3MONTHS_SUBSCRIPTION, SKU_6MONTHS_SUBSCRIPTION, SKU_12MONTHS_SUBSCRIPTION};
        } else {
            return new String[]{SKU_1MONTH_PURCHASE, SKU_3MONTHS_PURCHASE, SKU_6MONTHS_PURCHASE, SKU_12MONTHS_PURCHASE};
        }
    }

    public static String getSkuForButtonId(int id, boolean inappv3enabled) {
        if (id == R.id.inapppurchase_button_12months) {
            if (Logx.getInstance().isDebugMode()) {
                return "android.test.purchased";
            }
            return inappv3enabled ? SKU_12MONTHS_SUBSCRIPTION : SKU_12MONTHS_PURCHASE;
        } else if (id == R.id.inapppurchase_button_6months) {
            if (Logx.getInstance().isDebugMode()) {
                return "android.test.canceled";
            }
            return inappv3enabled ? SKU_6MONTHS_SUBSCRIPTION : SKU_6MONTHS_PURCHASE;
        } else if (id == R.id.inapppurchase_button_3months) {
            if (Logx.getInstance().isDebugMode()) {
                return "android.test.refunded";
            }
            return inappv3enabled ? SKU_3MONTHS_SUBSCRIPTION : SKU_3MONTHS_PURCHASE;
        } else if (id != R.id.inapppurchase_button_1month) {
            throw new IllegalArgumentException();
        } else if (Logx.getInstance().isDebugMode()) {
            return "android.test.item_unavailable";
        } else {
            return inappv3enabled ? SKU_1MONTH_SUBSCRIPTION : SKU_1MONTH_PURCHASE;
        }
    }

    public static int getMaxPeriodDays(String sku) {
        int maxPeriod;
        switch (sku) {
// https://developer.android.com/google/play/billing/billing_testing.html
            case "android.test.purchased":
            case SKU_12MONTHS_SUBSCRIPTION:
            case SKU_12MONTHS_PURCHASE:
                maxPeriod = 366;
                break;
            case "android.test.canceled":
            case SKU_6MONTHS_SUBSCRIPTION:
            case SKU_6MONTHS_PURCHASE:
                maxPeriod = 183;
                break;
            case "android.test.refunded":
            case SKU_3MONTHS_SUBSCRIPTION:
            case SKU_3MONTHS_PURCHASE:
                maxPeriod = 91;
                break;
            case "android.test.item_unavailable":
            case SKU_1MONTH_SUBSCRIPTION:
            case SKU_1MONTH_PURCHASE:
                maxPeriod = 31;
                break;
            default:
                maxPeriod = -1;
        }
        return maxPeriod;
    }

    public static String getLabel(Context context, String sku) {
        String label;
        switch (sku) {
// https://developer.android.com/google/play/billing/billing_testing.html
            case "android.test.purchased":
            case SKU_12MONTHS_SUBSCRIPTION:
            case SKU_12MONTHS_PURCHASE:
                label = getButtonText(context, R.id.inapppurchase_button_12months);
                break;
            case "android.test.canceled":
            case SKU_6MONTHS_SUBSCRIPTION:
            case SKU_6MONTHS_PURCHASE:
                label = getButtonText(context, R.id.inapppurchase_button_6months);
                break;
            case "android.test.refunded":
            case SKU_3MONTHS_SUBSCRIPTION:
            case SKU_3MONTHS_PURCHASE:
                label = getButtonText(context, R.id.inapppurchase_button_3months);
                break;
            case "android.test.item_unavailable":
            case SKU_1MONTH_SUBSCRIPTION:
            case SKU_1MONTH_PURCHASE:
                label = getButtonText(context, R.id.inapppurchase_button_1month);
                break;
            default:
                throw new IllegalArgumentException("Found: "+sku+", but expected any of: "+ Arrays.toString(getSkus(true))+", or "+Arrays.toString(getSkus(false)));
        }
        return label;
    }
}

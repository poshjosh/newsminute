package com.looseboxes.idisc.appbilling;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.listeners.NoMoreAdvertOnClickListener;

public class NoMoreAdvertOnClickListenerInAppPurchase extends NoMoreAdvertOnClickListener {
    public NoMoreAdvertOnClickListenerInAppPurchase(Activity activity) {
        super(activity);
    }

    public void onClick(View v) {
        try {
            getActivity().startActivity(new Intent(getActivity(), InAppPurchaseActivity.class));
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }
}

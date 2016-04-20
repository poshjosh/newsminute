package com.looseboxes.idisc.appbilling;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.looseboxes.idisc.common.listeners.NoMoreAdvertOnClickListener;
import com.looseboxes.idisc.common.util.Logx;

public class NoMoreAdvertOnClickListenerInAppPurchase extends NoMoreAdvertOnClickListener {
    public NoMoreAdvertOnClickListenerInAppPurchase(Activity activity) {
        super(activity);
    }

    public void onClick(View v) {
        try {
            getActivity().startActivity(new Intent(getActivity(), InAppPurchaseActivity.class));
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }
}

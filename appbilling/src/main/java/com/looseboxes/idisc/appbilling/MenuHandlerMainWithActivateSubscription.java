package com.looseboxes.idisc.appbilling;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.googleservicesextras.MenuHandlerMainWithAppInvite;

public class MenuHandlerMainWithActivateSubscription extends MenuHandlerMainWithAppInvite {
    public boolean isMenuItemToEnable(int id) {
        if (id == getActivateActionViewId()) {
            return true;
        }
        return super.isMenuItemToEnable(id);
    }

    public void onOptionsItemSelected(MenuItem item, Bundle bundle) {
        if (item.getItemId() == getActivateActionViewId()) {
            Activity activity = getActivity();
            if (!User.getInstance().isActivated(activity) || Pref.getSubscriptionSku(activity) == null) {
                activity.startActivity(new Intent(activity, InAppPurchaseActivity.class));
                return;
            }
            Popup.show(activity, activity.getString(R.string.err_alreadysubscribed_s, new Object[]{Pref.getSubscriptionSku(activity)}), 0);
            return;
        }
        super.onOptionsItemSelected(item, bundle);
    }
}

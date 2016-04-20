package com.looseboxes.idisc.common.listeners;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;

public class NoMoreAdvertOnClickListener implements ActivityOnClickListener {
    private Activity activity;

    public NoMoreAdvertOnClickListener(Activity activity) {
        this.activity = activity;
    }

    public void onClick(View v) {
        String url = App.getPropertiesManager(v.getContext()).getString(PropertyName.appPremiumVersionUrl);
        try {
            if (url == null) {
                Popup.show(this.activity, R.string.msg_nolink, 1);
            } else {
                App.startPlayStoreActivity(v.getContext(), Uri.parse(url));
            }
        } catch (Exception e) {
            Popup.show(this.activity, url == null ? "Error accessing URL of premium version" : "Error browsing: " + null, 1);
            Logx.debug(getClass(), e);
        }
    }

    public Activity getActivity() {
        return this.activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}

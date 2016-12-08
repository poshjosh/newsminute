package com.looseboxes.idisc.googleservicesextras;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.bc.android.core.util.Logx;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.looseboxes.idisc.common.handlers.DefaultLinkHandler;

public class LinkHandlerAppInvite extends DefaultLinkHandler {
    public LinkHandlerAppInvite(Context context) {
        super(context);
    }

    public boolean handleLink(Uri uri) {
        boolean handled = false;
        try {
            Context context = getContext();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                Intent intent = activity.getIntent();
                if (intent != null && AppInviteReferral.hasReferral(intent)) {
                    handled = true;
                    handleAppInviteDeepLink(activity, intent);
                }
            }
            if (!handled) {
                handled = super.handleLink(uri);
            }
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
        return handled;
    }

    private void handleAppInviteDeepLink(Activity activity, Intent intent) {
        try {
            Intent deepLinkIntent = new Intent(activity, DeepLinkActivity.class);
            deepLinkIntent.putExtra(DeepLinkActivity.EXTRA_STRING_INVITATIONID, AppInviteReferral.getInvitationId(intent));
            deepLinkIntent.putExtra(DeepLinkActivity.EXTRA_STRING_DEEPLINK, AppInviteReferral.getDeepLink(intent));
            activity.startActivity(deepLinkIntent);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
        }
    }
}

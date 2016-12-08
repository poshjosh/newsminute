package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.content.Intent;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;


public class NewsminuteUtil extends com.bc.android.core.util.Util {

    public static String getNetworkUnavailableMessage(Context context) {
        return context.getString(getNetworkUnavailableMessageResourceId(context));
    }

    public static int getNetworkUnavailableMessageResourceId(Context context) {
        if(Pref.isWifiOnly(context, false)) {
            return com.bc.android.core.R.string.err_wifiunavailable;
        }else{
            return com.bc.android.core.R.string.err_internetunavailable;
        }
    }

    public static boolean isPreferredNetworkConnectedOrConnecting(Context context) {
        final boolean wifiOnly = Pref.isWifiOnly(context, false);
        if(wifiOnly) {
            return isWifiConnectedOrConnecting(context);
        }else {
            return isNetworkConnectedOrConnecting(context);
        }
    }

    public static Intent createShareIntent(Context context, CharSequence content, String contentIntentType) {
        return createShareIntent(context, getDefaultSubjectForMessages(context), content, contentIntentType, true);
    }

    public static Intent createShareIntent(Context context, CharSequence subject, CharSequence content, String contentIntentType, boolean addDefaultMessageSuffix) {
        String messageSuffix;
        if(addDefaultMessageSuffix) {
            messageSuffix = getMessageSuffix(context);
        }else{
            messageSuffix = null;
        }
        return Util.createShareIntent(context, subject, content, contentIntentType, messageSuffix);
    }

    public static String getMessageSuffix(Context context) {
        String appName = context.getString(R.string.app_label);
        return context.getString(R.string.fmt_message_suffix, new Object[]{appName});
    }

    public static String getDefaultSubjectForMessages(Context context) {
        String username = User.getInstance().getUsername(context);
        if (username == null) {
            return context.getString(R.string.msg_share_subject_guest);
        }
        return context.getString(R.string.msg_share_subject_user, new Object[]{username});
    }
}

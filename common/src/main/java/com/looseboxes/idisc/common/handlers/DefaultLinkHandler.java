package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.DisplayFeedActivity;
import com.looseboxes.idisc.common.activities.DisplayLinkActivity;
import com.looseboxes.idisc.common.activities.MainActivity;
import com.looseboxes.idisc.common.io.StreamReader;
import com.looseboxes.idisc.common.jsonview.FeedhitNames;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.RemoteSession;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

public class DefaultLinkHandler implements LinkHandler {
    private final Context context;

    private static class ActivationTask extends AsyncTask<String, Void, String> {
        private final Context context;
        private final RemoteSession sess;

        private ActivationTask(Context context) {
            this.context = context;
            this.sess = new RemoteSession(context);
        }

        protected String doInBackground(String... params) {
            try {
                return new StreamReader().readContents(this.sess.getInputStream(new URL(params[0])));
            } catch (Exception e) {
                Logx.log(getClass(), e);
                return null;
            }
        }

        protected void onCancelled(String s) {
            end(s);
        }

        protected void onPostExecute(String s) {
            end(s);
        }

        private void end(String s) {
            try {
                Object err;
                if (this.sess == null) {
                    err = this.context.getString(R.string.err_activation);
                } else if (this.sess.getResponseCode() < 300) {
                    err = this.context.getString(R.string.msg_activationsuccess);
                } else {
                    err = this.context.getString(R.string.err_activation);
                }
                Popup.show(this.context, err, Toast.LENGTH_LONG);
            } catch (Exception e) {
                Logx.log(getClass(), e);
                Popup.show(this.context, this.context.getString(R.string.err_activation), Toast.LENGTH_LONG);
            }
        }
    }

    public DefaultLinkHandler(Context context) {
        this.context = context;
    }

    public boolean handleLink(Uri uri) {
        String urlString = null;
        boolean handled = false;
        if (uri != null) {
            urlString = uri.toString();
        }
        if (urlString == null) {
            return false;
        }
        try {
            Intent nextIntent;
            String sLower = urlString.toLowerCase();
            if (sLower.contains("activate")) {
                new ActivationTask(null).execute(new String[]{urlString});
                nextIntent = new Intent(this.context, MainActivity.class);
            } else if (sLower.startsWith("https://play.google.com/store")) {
                nextIntent = App.getPlayStoreIntent(this.context, uri);
            } else if (sLower.startsWith("app")) {
                nextIntent = getAppLinkIntent(urlString, sLower);
            } else {
                nextIntent = new Intent(this.context, DisplayLinkActivity.class);
                nextIntent.putExtra(DisplayLinkActivity.EXTRA_STRING_LINK_TO_DISPLAY, urlString);
            }
            if (nextIntent == null) {
                return false;
            }
            handled = true;
            this.context.startActivity(nextIntent);
            return true;
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return handled;
        }
    }

    public Context getContext() {
        return this.context;
    }

    private Intent getAppLinkIntent(String urlString, String sLower) {
        int a;
        int n1 = sLower.indexOf(58);
        int n2 = sLower.indexOf("//") + 1;
        if (n1 > n2) {
            a = n1;
        } else {
            a = n2;
        }
        if (a == -1) {
            Popup.show(this.context, this.context.getString(R.string.err_invalid_link_s, new Object[]{urlString}), 0);
            return null;
        }
        int end;
        String query;
        int start = a + 1;
        int b = sLower.indexOf(63, start);
        if (b == -1) {
            end = urlString.length();
        } else {
            end = b;
        }
        String activityName = urlString.substring(start, end);
        if (end < urlString.length()) {
            query = urlString.substring(end + 1);
        } else {
            query = null;
        }
        try {
            return createIntent(urlString, activityName, query);
        } catch (Exception e) {
            Object msg;
            if (e instanceof MalformedURLException) {
                msg = this.context.getString(R.string.err_invalid_link_s, new Object[]{urlString});
            } else if (e instanceof FileNotFoundException) {
                msg = this.context.getString(R.string.err_not_found_s, new Object[]{urlString});
            } else {
                msg = this.context.getString(R.string.err_error_displaying_s, new Object[]{urlString});
            }
            Popup.show(this.context, msg, 0);
            Logx.log(getClass(), e);
            return null;
        }
    }

    private Intent createIntent(String urlString, String activityName, String query) throws MalformedURLException, FileNotFoundException {
        Class<? extends Activity> activityClass = getActivityClass(activityName);
        if (activityClass == null) {
            throw new FileNotFoundException(urlString);
        }
        Intent intent = new Intent(this.context, activityClass);
        if (!(query == null || query.isEmpty())) {
            addExtras(urlString, activityClass, intent, query);
        }
        return intent;
    }

    private Class<? extends Activity> getActivityClass(String activityName) {
        Class<? extends Activity> output;
        switch (activityName) {
            case "displayfeed":
                output = DisplayFeedActivity.class;
                break;
            default:
                output = null;
        }
        return output;
    }

    private void addExtras(String urlString, Class<? extends Activity> activityClass, Intent intent, String query) throws MalformedURLException {
        if (activityClass == DisplayFeedActivity.class) {
            Long feedid = getFeedid(urlString, query);
            if (feedid == null) {
                throw new MalformedURLException(urlString);
            }
            intent.putExtra(DisplayFeedActivity.EXTRA_LONG_SELECTED_FEEDID, feedid.longValue());
        }
    }

    private Long getFeedid(String urlString, String query) throws MalformedURLException {
        Long l = null;
        int n = query.indexOf(FeedhitNames.feedid);
        if (n != -1) {
            n = query.indexOf(61, n);
            if (n != -1) {
                int start = n + 1;
                int end = query.indexOf(38, start);
                if (end == -1) {
                    end = query.length();
                }
                try {
                    l = Long.valueOf(Long.parseLong(query.substring(start, end)));
                } catch (NumberFormatException e) {
                    Logx.log(getClass(), e);
                    throw new MalformedURLException(urlString);
                }
            }
        }
        return l;
    }
}

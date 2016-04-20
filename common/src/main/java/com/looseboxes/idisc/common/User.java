package com.looseboxes.idisc.common;

import android.app.Activity;
import android.content.Context;

import com.looseboxes.idisc.common.asynctasks.Getuser;
import com.looseboxes.idisc.common.asynctasks.Logout;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.io.IOWrapper;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.CachedSet;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.RemoteSession;
import com.looseboxes.idisc.common.util.Util;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private static User user;
    private transient IOWrapper<Map> _dio;
    private CachedSet<Long> _iam_accessViaGetter;
    private transient IOWrapper<List<String>> _leio;

    private class UserDetailsDownloader extends Getuser {
        private final Activity activity;

        private UserDetailsDownloader(Activity activity, String emailAddress, String username, String password) {
            super(emailAddress, username, password, !User.this.isUserCreated(activity));
            this.activity = activity;
        }

        public void onSuccess(JSONObject download) {
            User.this.setUserCreated(this.activity, true);
            Logx.debug(getClass(), "Downloaded:\n{0}", download);
            User.getInstance().setDetails(this.activity, download);
            try {
                App.updateInstallationDetails(this.activity, download);
                Popup.show(this.activity, R.string.msg_login_success, 0);
            } catch (IOException e) {
                Logx.log(getClass(), e);
            } finally {
                this.activity.finish();
            }
        }

        public Context getContext() {
            return this.activity;
        }
    }

    private User() {
    }

    public static User getInstance() {
        if (user == null) {
            user = new User();
        }
        return user;
    }

    public boolean isActivated(Context ctx) {
        return Pref.isSubscriptionActive(ctx);
    }

    public boolean isAdmin(Context context) {
        try {
            String useremail;
            if (isLoggedIn(context)) {
                useremail = getEmailAddress(context);
            } else {
                List<String> localemails = getLocalEmails(context);
                useremail = (localemails == null || localemails.isEmpty()) ? null : (String) localemails.get(0);
            }
            if (useremail == null) {
                return false;
            }
            List<String> emails = App.getPropertiesManager(context).getList(PropertyName.developerEmails);
            if (emails == null) {
                return false;
            }
            for (String email : emails) {
                if (email.equals(useremail)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Logx.log(getClass(), e);
            return false;
        }
    }

    public int addParameters(Context context, Map<String, String> params) {
        return App.addParameters(context, params);
    }

    public int addLoginCredentials(Context context, Map<String, String> params) {
        int added = 0;
        if (getEmailAddress(context) != null) {
            params.put(AuthuserNames.emailaddress, getEmailAddress(context));
            added = 0 + 1;
        }
        if (getUsername(context) != null) {
            params.put(AuthuserNames.username, getUsername(context));
            added++;
        }
        if (getPassword(context) == null) {
            return added;
        }
        params.put(FeeduserNames.password, getPassword(context));
        return added + 1;
    }

    public int addSubscriptionCredentials(Context context, Map<String, String> params) {
        params.put("activated", Boolean.toString(isActivated(context)));
        int added = 0 + 1;
        params.put("subscriptionSku", Pref.getSubscriptionSku(context));
        return added + 1;
    }

    public boolean isLoggedIn(Context context) {
        List<String> cookies = new RemoteSession(context).getCookies();
        return (cookies == null || cookies.isEmpty() || getEmailAddress(context) == null || getPassword(context) == null || getFeeduserid(context) == null) ? false : true;
    }

    public void logout(Context context) {
        if (isLoggedIn(context)) {
            new Logout(context).execute();
        } else {
            Popup.show((Context) null, (Object) "You are already logged out", 0);
        }
    }

    public void login(Activity activity, String emailAddress, String username, String password) {
        if (isLoggedIn(activity)) {
            Popup.show(activity, activity.getString(R.string.msg_already_loggedin), 0);
            return;
        }
        String toEncrypt = BuildConfig.FLAVOR;
        String key = emailAddress;
        if (password == null) {
            password = Util.encrpyt(toEncrypt, key, 6);
        }
        new UserDetailsDownloader(activity, emailAddress, username, password).execute();
    }

    public String getEmailAddress(Context context) {
        return getDetails(context) == null ? null : (String) getDetails(context).get(FeeduserNames.emailaddress);
    }

    public String getUsername(Context context) {
        return getDetails(context) == null ? null : (String) getDetails(context).get(AuthuserNames.username);
    }

    public String getPassword(Context context) {
        return getDetails(context) == null ? null : (String) getDetails(context).get(FeeduserNames.password);
    }

    public Object getFeeduserid(Context context) {
        return getDetails(context) == null ? null : getDetails(context).get(InstallationNames.installationid);
    }

    private <K> IOWrapper<K> getIOWrapper(Context context, Class<K> aClass, String fname) {
        IOWrapper<K> io;
        if (aClass == null) {
            io = IOWrapper.getObjectInstance();
        } else {
            io = IOWrapper.getObjectInstance(aClass);
        }
        io.setContext(context);
        io.setFilename(fname);
        return io;
    }

    public boolean addReadFeed(Context context, Long feedid) {
        CachedSet<Long> list = getReadFeedids(context);
        try {
            boolean add = list.add(feedid);
            return add;
        } finally {
            list.close();
        }
    }

    public boolean isReadFeed(Context context, Long feedid) {
        CachedSet<Long> list = getReadFeedids(context);
        return list == null ? false : list.contains(feedid);
    }

    public CachedSet<Long> getReadFeedids(Context context) {
        if (this._iam_accessViaGetter == null) {
            this._iam_accessViaGetter = new CachedSet(context, FileIO.getReadFeedidsFilename());
        }
        return this._iam_accessViaGetter;
    }

    public String getGender(Context context) {
        return Pref.getGender(context, null);
    }

    public void setGender(Context context, String gender) {
        if (gender == null || gender.isEmpty()) {
            throw new NullPointerException();
        }
        Pref.setGender(context, gender);
    }

    public String getScreenName(Context context) {
        return Pref.getString(context, getClass().getName() + ".screenName", null);
    }

    public void setScreenName(Context context, String screenName) {
        if (screenName == null || screenName.isEmpty()) {
            throw new NullPointerException();
        }
        Pref.setString(context, getClass().getName() + ".screenName", screenName);
    }

    public void setDetails(Context context, Map<String, Object> userDetails) {
        Logx.debug(getClass(), "Updating user details to:\n{0}", userDetails);
        getDetailsIO(context, true).setTarget(userDetails);
    }

    public Map<String, Object> getDetails(Context context) {
        IOWrapper<Map> io = getDetailsIO(context, false);
        return io == null ? Collections.EMPTY_MAP : (Map) io.getTarget();
    }

    public List<String> getLocalEmails(Context context) {
        IOWrapper<List<String>> io = getLocalEmailsIO(context, false);
        return io == null ? Collections.EMPTY_LIST : (List) io.getTarget();
    }

    private IOWrapper<List<String>> getLocalEmailsIO(Context context, boolean create) {
        if (this._leio == null && create) {
            this._leio = new IOWrapper(context, FileIO.getEmailsFilename());
        }
        return this._leio;
    }

    private IOWrapper<Map> getDetailsIO(Context context, boolean create) {
        if (this._dio == null && create) {
            this._dio = new IOWrapper(context, FileIO.getUserDetailsFilename());
        }
        return this._dio;
    }

    private void setUserCreated(Context context, boolean created) {
        Pref.setBoolean(context, getUserCreatedPreferenceKey(), created);
    }

    private boolean isUserCreated(Context context) {
        return Pref.getBoolean(context, getUserCreatedPreferenceKey(), false);
    }

    private String getUserCreatedPreferenceKey() {
        return User.class.getName() + ".created";
    }
}

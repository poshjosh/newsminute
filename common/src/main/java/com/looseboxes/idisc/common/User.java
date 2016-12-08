package com.looseboxes.idisc.common;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.bc.android.core.io.IOWrapper;
import com.bc.android.core.io.RemoteSession;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Geo;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.asynctasks.Getuser;
import com.looseboxes.idisc.common.asynctasks.Logout;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.jsonview.CountryNames;
import com.looseboxes.idisc.common.jsonview.FeeduserNames;
import com.looseboxes.idisc.common.jsonview.GenderNames;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.UserprofilePreferences;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User implements Serializable {

    private static User user;

    private transient IOWrapper<Map> _dio;

    private transient IOWrapper<List<String>> _leio;

    private UserprofilePreferences profilePreferences;

    public class UserDetailsDownloader extends Getuser {

        private final Activity activity;

        private final @StringRes int successMessage;

        public UserDetailsDownloader(Activity activity, @StringRes int errorMessage, @StringRes int successMessage,
                                      String emailAddress, String username, String password) {
            super(activity, activity.getString(errorMessage), emailAddress, username, password, !User.this.isUserCreated(activity));
            this.activity = activity;
            this.successMessage = successMessage;
        }

        public void onSuccess(JSONObject download) {

            Logx.getInstance().debug(getClass(), "Downloaded:\n{0}", download);

            User.this.setUserCreated(this.activity, true);

            User.this.setDetails(this.activity, download);
            try {
                App.updateInstallationDetails(this.activity, download);
                Popup.getInstance().show(this.activity, successMessage, Toast.LENGTH_SHORT);
            } catch (IOException e) {
                Logx.getInstance().log(getClass(), e);
            } finally {
                this.activity.finish();
            }
        }
    }

    private User() {
        this.profilePreferences = new UserprofilePreferences();
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
                useremail = (localemails == null || localemails.isEmpty()) ? null : localemails.get(0);
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
            Logx.getInstance().log(getClass(), e);
            return false;
        }
    }

    public Map<String, String> getOutputParameters(Context context) {
        Map<String, String> params = new LinkedHashMap<>();
        addParameters(context, params);
        return params;
    }

    public int addParameters(Context context, Map<String, String> params) {
        return App.addParameters(context, params);
    }

    public int addDetails(Context context, Map<String, String> params) {

        Map<String, Object> details = this.getDetails(context);

        final Set<String> keys = details.keySet();

        int added = 0;

        for(String key : keys) {

            Object detail = details.get(key);

            if(detail == null) {
                continue;
            }

            params.put(key, String.valueOf(detail));

            ++added;
        }

        return added;
    }

    public int addLoginCredentials(Context context, Map<String, String> params) {
        int added = 0;

        final String email = getEmailAddress(context);
        if (email != null) {
            params.put(AuthuserNames.emailaddress, email);
            params.put(FeeduserNames.emailAddress, email);
            added += 2;
        }

        if (getUsername(context) != null) {
            params.put(AuthuserNames.username, getUsername(context));
            ++added;
        }

        if (getPassword(context) != null) {
            params.put(FeeduserNames.password, getPassword(context));
            ++added;
        }
        return added;
    }

    public int addSubscriptionCredentials(Context context, Map<String, String> params) {
        int added = 0;
        params.put("activated", Boolean.toString(isActivated(context)));
        ++added;
        final String subscriptionSku = Pref.getSubscriptionSku(context);
        if(subscriptionSku != null) {
            params.put("subscriptionSku", subscriptionSku);
            ++added;
        }
        return added;
    }

    public boolean isLoggedIn(Context context) {
        return ! (getEmailAddress(context) == null || getPassword(context) == null);
    }

    public boolean isLoggedIntoCurrentSession(Context context) {
        List<String> cookies = RemoteSession.getGlobalCookies(context);
        return ! (cookies == null || cookies.isEmpty() || !this.isLoggedIn(context));
    }

    public void logout(Context context) {
        if (isLoggedIn(context)) {
            new Logout(context).execute();
        } else {
            Popup.getInstance().show(context, context.getString(R.string.msg_already_loggedout), 0);
        }
    }

    public void signup(Activity activity, String emailAddress, String username, String password) {
        if (isLoggedIn(activity)) {
            Popup.getInstance().show(activity, activity.getString(R.string.msg_already_loggedin), Toast.LENGTH_SHORT);
            return;
        }
        String toEncrypt = "";
        String key = emailAddress;
        if (password == null) {
            password = Util.encrpyt(toEncrypt, key, 6);
        }
        new UserDetailsDownloader(activity, R.string.err_signup, R.string.msg_signupsuccess, emailAddress, username, password).execute();
    }

    public void login(Activity activity, String emailAddress, String username, String password) {

        if (isLoggedIn(activity)) {
            Popup.getInstance().show(activity, activity.getString(R.string.msg_already_loggedin), Toast.LENGTH_SHORT);
            return;
        }

        Logx.getInstance().debug(this.getClass(), "Logging in");

        new UserDetailsDownloader(activity, R.string.err_login, R.string.msg_login_success, emailAddress, username, password).execute();
    }

    public String getEmailAddress(Context context) {
        String output;
        Map<String, Object> details = this.getDetails(context);
        if(details == null || details.isEmpty()) {
            output = null;
        }else{
            output = (String)details.get(FeeduserNames.emailAddress);
            if(output == null) {
                output = (String)details.get(AuthuserNames.emailaddress);
            }
        }
        return output;
    }

    public String getUsername(Context context) {
        return getDetails(context) == null ? null : (String) getDetails(context).get(AuthuserNames.username);
    }

    public String getPassword(Context context) {
        return getDetails(context) == null ? null : (String) getDetails(context).get(FeeduserNames.password);
    }

    public void setDetails(Context context, Map<String, Object> userDetails) {

        Logx.getInstance().debug(getClass(), "Updating user details to:\n{0}", userDetails);

        getDetailsIO(context).setTarget(userDetails);

//        Logx.getInstance().debug(getClass(), "AFTER Updating user details: {0}", this.getDetails(context));
//        Logx.getInstance().debug(getClass(), "AFTER Updating user details, is logged in: {0}", this.isLoggedIn(context));
    }

    public Map<String, Object> getDetails(Context context) {

        IOWrapper<Map> io = getDetailsIO(context);

        Map output = io == null ? Collections.EMPTY_MAP : io.getTarget();

        Logx.getInstance().log(Log.VERBOSE, getClass(), "User details:\n{0}", output);

        return output;
    }

    public List<String> getLocalEmails(Context context) {
        IOWrapper<List<String>> io = getLocalEmailsIO(context);
        return io == null ? Collections.EMPTY_LIST : (List) io.getTarget();
    }

    private static Map<String, String> cachedIso3To2CountryCode;
    public String getIso2CountryCode(Context context, String defaultValue) {
        String countryCode = getCountryCode(context, null);
        if(countryCode != null && countryCode.trim().length() == 3) {
            final String iso3code = countryCode;
            if(cachedIso3To2CountryCode == null) {
                final String iso2code = new Geo().getIso2ForIso3CountryCode(context, iso3code, null);
                if (iso2code == null) {
                    cachedIso3To2CountryCode = Collections.emptyMap();
                }else{
                    cachedIso3To2CountryCode = Collections.singletonMap(iso3code, iso2code);
                }
                countryCode = iso2code;
            }else{
                countryCode = cachedIso3To2CountryCode.get(iso3code);
            }
        }
        return countryCode == null ? defaultValue : countryCode;
    }

    public String getCountryCode(Context context, String defaultValue) {
        String output = this.profilePreferences.getCountryCode(context, defaultValue);
        if(output == defaultValue) {
            Object oval = this.getDetails(context).get(CountryNames.countryid);
            if(oval instanceof Map) {
                Map map = (Map)oval;
                output = (String)map.get(CountryNames.iso3code);
                if(output == null) {
                    output = (String)map.get(CountryNames.iso2code);
                }
            }
            if(output  == null) {
                output = new Geo().getCountryCode(context, defaultValue);
            }
        }
        return output == null ? defaultValue : output;
    }

    public String getScreenname(Context context, String defaultValue) {
        String output = this.profilePreferences.getScreenname(context, defaultValue);
        if(output == defaultValue) {
            output = (String)this.getDetails(context).get(InstallationNames.screenname);
        }
        return output == null ? defaultValue : output;
    }

    public String getGender(Context context, String defaultValue) {
        String output = this.profilePreferences.getGender(context, defaultValue);
        if(output == defaultValue) {
            Object oval = this.getDetails(context).get(GenderNames.genderid);
            if(oval instanceof Map) {
                Map map = (Map)oval;
                output = (String)map.get(GenderNames.gender);
            }
        }
        return output == null ? defaultValue : output;
    }

    public final UserprofilePreferences getProfilePreferences() {
        return profilePreferences;
    }

    private IOWrapper<List<String>> getLocalEmailsIO(Context context) {
        if (this._leio == null) {
            this._leio = new IOWrapper(context, FileIO.getEmailsFilename());
        }
        return this._leio;
    }

    private IOWrapper<Map> getDetailsIO(Context context) {
        if (this._dio == null) {
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

package com.looseboxes.idisc.common;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.jsonview.JsonView;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.DeviceID;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import com.looseboxes.idisc.common.util.PropertiesManager;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;

public final class App {

    public static final boolean SERVICE_IN_EXACT_REPEATING = false;
    private static long FIRST_INSTALLATION_TIME;
    private static String ID;
    private static int versionCode;
    private static PropertiesManager _ap_accessViaGetter;
    private static Map<AliasType, AliasesManager> _kam_accessViaGetter;
    private static TimeZone _tz;
    private static boolean appVisible;
    private static Context mContext;
    private static boolean useExternalFile;
    static {
        useExternalFile = false;
    }

    public static TimeZone getUserSelectedTimeZone() {
        if (_tz == null) {
            _tz = GregorianCalendar.getInstance().getTimeZone();
        }
        return _tz;
    }

    @Deprecated
    public static void setContext(Context context) {
        mContext = context;
    }

    @Deprecated
    public static Context getContext() {
        return mContext;
    }

    public static String getLabel(Context context) {
        return context.getString(R.string.app_label);
    }

    public static void startMarketActivity(Application context) {
        startPlayStoreActivity((Context) context, context.getPackageName());
    }

    public static void startMarketActivity(Context context, String appPackageName) {
        startPlayStoreActivity(context, appPackageName);
    }

    public static void startPlayStoreActivity(Application context) {
        startPlayStoreActivity((Context) context, context.getPackageName());
    }

    public static void startPlayStoreActivity(Context context, String appPackageName) {
        if (!startPlayStoreActivity(context, Uri.parse("market://details?id=" + appPackageName))) {
            startPlayStoreActivity(context, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
        }
    }

    public static boolean startPlayStoreActivity(Context context, Uri uri) {
        try {
            context.startActivity(getPlayStoreIntent(context, uri));
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public static Intent getPlayStoreIntent(Context context, Uri uri) {
        Intent goToMarket = new Intent("android.intent.action.VIEW", uri);
        if (uri.getScheme().startsWith("market")) {
            goToMarket.addFlags(1208483840);
        }
        return goToMarket;
    }

    public static void updateInstallationDetails(Context context, JSONObject download) throws IOException {
        String sval;
        File file;
        Logx.debug(App.class, "Updating installation details");
        context = context.getApplicationContext();
        updateScreenName(context, download);
        Object oval = download.get(InstallationNames.installationkey);
        if (oval != null) {
            sval = oval.toString().trim();
            if (!sval.isEmpty()) {
                ID = sval;
                if (useExternalFile) {
                    file = FileIO.getExternalPublicFile(context, FileIO.getInstallationIdFilename());
                } else {
                    file = FileIO.getFile(context, FileIO.getInstallationIdFilename());
                }
                FileIO.writeToFile(file, ID, false);
            }
        }
        oval = download.get(InstallationNames.firstinstallationdate);
        if (oval != null) {
            sval = oval.toString().trim();
            if (!sval.isEmpty()) {
                Date date = JsonView.parse(sval);
                if (date != null) {
                    FIRST_INSTALLATION_TIME = date.getTime();
                    if (useExternalFile) {
                        file = FileIO.getExternalPublicFile(context, FileIO.getFirstInstallationTimeFilename());
                    } else {
                        file = FileIO.getFile(context, FileIO.getFirstInstallationTimeFilename());
                    }
                    FileIO.writeToFile(file, Long.toString(FIRST_INSTALLATION_TIME), false);
                }
            }
        }
    }

    private static void updateScreenName(Context context, JSONObject download) {
        Logx.debug(App.class, "Updating installation details");
        context = context.getApplicationContext();
        try {
            Object screenname = download.get(InstallationNames.screenname);
            Logx.debug(App.class, "Screenname: {0}", screenname);
            if (screenname != null) {
                String remote = screenname.toString();
                if (!remote.isEmpty()) {
                    String local = User.getInstance().getScreenName(context);
                    if (!remote.equals(local)) {
                        if (local != null) {
                            Logx.log(5, App.class, "Local screenname: {0} not equal to remote: {1}. Using remote", local, remote);
                        }
                        User.getInstance().setScreenName(context, remote);
                    }
                }
            }
        } catch (Exception e) {
            Logx.log(App.class, e);
        }
    }

    public static int addParameters(Context context, Map<String, String> params) {
        User user = User.getInstance();
        int added = ((user.addLoginCredentials(context, params)) + user.addSubscriptionCredentials(context, params)) + addInstallationParameters(context, params);
        params.put("versionCode", String.valueOf(getVersionCode(context)));
        added++;
        params.put("format", "text/json");
        return added + 1;
    }

    public static int addInstallationParameters(Context context, Map<String, String> params) {
        int added = 0;
        context = context.getApplicationContext();
        try {
            if (isInstalled(context)) {
                params.put(InstallationNames.installationkey, getId(context));
                params.put(InstallationNames.firstinstallationdate, Long.toString(getFirstInstallationTime(context)));
                params.put(InstallationNames.lastinstallationdate, Long.toString(getLastInstallationTime(context)));
                added = 3;
                Object feeduserid = User.getInstance().getFeeduserid(context);
                if (feeduserid != null) {
                    params.put(InstallationNames.feeduserid, feeduserid.toString());
                    added = 3 + 1;
                }
                String screenname = User.getInstance().getScreenName(context);
                if (screenname != null) {
                    params.put(InstallationNames.screenname, screenname);
                    added++;
                }
                Logx.log(Log.VERBOSE, App.class, "Added installation parameters:\n{0}", params.keySet(), Integer.valueOf(1));
            }
        } catch (IOException e) {
            Logx.log(App.class, e);
        }
        return added;
    }

    public static void handleInstallationError(Context context, Exception e, int displaymsg_id) {
        handleInstallationError(context, e, context.getString(displaymsg_id));
    }

    public static void handleInstallationError(Context context, Exception e, String displayMessage) {
        RemoteLog.logInstallationError(context);
    }

    public static boolean isVisible() {
        return appVisible;
    }

    public static void setVisible(boolean appVisible) {
        appVisible = appVisible;
    }

    public static synchronized boolean isAcceptableVersion(Context context, int versionCode) {
        boolean accepted;
        synchronized (App.class) {
            accepted = false;
            try {
                accepted = VERSION.SDK_INT >= versionCode;
            } catch (Exception e) {
                Logx.log(App.class, e);
                try {
                    accepted = getVersionCode(context) >= versionCode;
                } catch (Exception e1) {
                    Logx.log(App.class, e1);
                }
            }
        }
        return accepted;
    }

    public static int getVersionCode(Context context) {
        context = context.getApplicationContext();
        if (versionCode == 0 && context != null) {
            try {
                versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (Exception e) {
                versionCode = -1;
                Logx.log(App.class, e);
            }
        }
        return versionCode;
    }

    public static synchronized void install(Context context, String screenName) throws IOException {
        synchronized (App.class) {
            getId(context, UUID.randomUUID());
            User.getInstance().setScreenName(context, screenName);
            long currTime = System.currentTimeMillis();
            getFirstInstallationTime(context, currTime);
            Pref.setLong(context, FileIO.getLastInstallationTimeFilename(), currTime);
            Popup.show((View) null, R.string.msg_installation_successful, 0);
        }
    }

    public static synchronized void init(Context context) {
        synchronized (App.class) {
            context = context.getApplicationContext();
            getPropertiesManager(context);
            for (AliasType aliasesAliasType : AliasType.values()) {
                getAliasesManager(context, aliasesAliasType);
            }
        }
    }

    public static PropertiesManager getPropertiesManager(Context context) {
        if (_ap_accessViaGetter == null) {
            _ap_accessViaGetter = new PropertiesManager(context, TimeUnit.HOURS.toMillis(6));
            _ap_accessViaGetter.setNoUI(false);
            _ap_accessViaGetter.update(false);
        }
        return _ap_accessViaGetter;
    }

    public static AliasesManager getAliasesManager(Context context, AliasType aliasType) {
        return getAliasesManager(context, TimeUnit.HOURS.toMillis(6), aliasType);
    }

    public static AliasesManager getAliasesManager(Context context, long updateIntervalMillis, AliasType aliasType) {
        if (_kam_accessViaGetter == null) {
            _kam_accessViaGetter = new EnumMap(AliasType.class);
        }
        AliasesManager output = (AliasesManager) _kam_accessViaGetter.get(aliasType);
        if (output != null) {
            return output;
        }
        output = new AliasesManager(context, updateIntervalMillis, aliasType);
        output.update(false);
        _kam_accessViaGetter.put(aliasType, output);
        return output;
    }

    public static PreferenceFeedsManager getPreferenceFeedsManager(Context context, PreferenceType type) {
        return new PreferenceFeedsManager(context, type);
    }

    public static boolean isInstalled(Context context) throws IOException {
        boolean installed;
        if (getId(context) == null || getFirstInstallationTime(context) <= 0) {
            installed = false;
        } else {
            installed = true;
        }
        Logx.log(Log.VERBOSE, App.class, "is installed: {0}", Boolean.valueOf(installed));
        return installed;
    }

    public static synchronized String getId(Context context) throws IOException {
        String str;
        synchronized (App.class) {
            if (Logx.isDebugMode()) {
                str = "abdb33ee-a09e-4d7d-b861-311ee7061325";
            } else {
                context = context.getApplicationContext();
                str = getId(context, DeviceID.getID(context));
            }
        }
        return str;
    }

    public static long getFirstInstallationTime(Context context) throws IOException {
        return getFirstInstallationTime(context, -1);
    }

    public static synchronized long getLastInstallationTime(Context context) {
        long j;
        synchronized (App.class) {
            j = Pref.getLong(context, FileIO.getLastInstallationTimeFilename(), -1);
        }
        return j;
    }

    private static String getId(Context context, Object defaultValue) throws IOException {
        String str = null;
        context = context.getApplicationContext();
        if (ID == null) {
            String installationIdFilename;
            if (useExternalFile) {
                installationIdFilename = FileIO.getInstallationIdFilename();
                if (defaultValue != null) {
                    str = defaultValue.toString();
                }
                ID = loadDoubleSavedFile(context, installationIdFilename, str);
            } else {
                installationIdFilename = FileIO.getInstallationIdFilename();
                if (defaultValue != null) {
                    str = defaultValue.toString();
                }
                ID = loadSavedFile(context, installationIdFilename, str);
            }
        }
        return ID;
    }

    private static long getFirstInstallationTime(Context context, long defaultValue) throws IOException {
        String str = null;
        context = context.getApplicationContext();
        if (FIRST_INSTALLATION_TIME <= 0) {
            String sval;
            String firstInstallationTimeFilename;
            if (useExternalFile) {
                firstInstallationTimeFilename = FileIO.getFirstInstallationTimeFilename();
                if (defaultValue >= 0) {
                    str = Long.toString(defaultValue);
                }
                sval = loadDoubleSavedFile(context, firstInstallationTimeFilename, str);
            } else {
                firstInstallationTimeFilename = FileIO.getFirstInstallationTimeFilename();
                if (defaultValue >= 0) {
                    str = Long.toString(defaultValue);
                }
                sval = loadSavedFile(context, firstInstallationTimeFilename, str);
            }
            if (sval != null) {
                FIRST_INSTALLATION_TIME = Long.parseLong(sval);
            }
        }
        return FIRST_INSTALLATION_TIME;
    }

    private static String loadSavedFile(Context context, String fname, String defaultValue) {
        File idFile = FileIO.getFile(context.getApplicationContext(), fname);
        try {
            if (idFile.exists()) {
                return FileIO.readFile(idFile);
            }
            if (defaultValue == null) {
                return null;
            }
            FileIO.writeToFile(idFile, defaultValue, false);
            return null;
        } catch (Exception e) {
            Logx.log(App.class, e);
            throw new RuntimeException(e);
        }
    }

    private static String loadDoubleSavedFile(Context context, String fname, String defaultValue) throws IOException {
        context = context.getApplicationContext();
        File publicFile = FileIO.getExternalPublicFile(context, fname);
        File privateFile = FileIO.getFile(context, fname);
        String output;
        if (privateFile.exists()) {
            output = FileIO.readFile(privateFile);
            if (!FileIO.isExternalStorageReadable()) {
                return output;
            }
            if (publicFile.exists()) {
                String publicValue = FileIO.readFile(publicFile);
                if (output.equals(publicValue) || !FileIO.isExternalStorageWritable()) {
                    return output;
                }
                FileIO.writeToFile(privateFile, publicValue, false);
                return publicValue;
            } else if (!FileIO.isExternalStorageWritable()) {
                return output;
            } else {
                FileIO.writeToFile(publicFile, output, false);
                return output;
            }
        } else if (FileIO.isExternalStorageReadable()) {
            if (publicFile.exists()) {
                output = FileIO.readFile(publicFile);
                FileIO.writeToFile(privateFile, output, false);
                return output;
            } else if (defaultValue == null) {
                return null;
            } else {
                FileIO.writeToFile(privateFile, defaultValue, false);
                if (FileIO.isExternalStorageWritable()) {
                    FileIO.writeToFile(publicFile, defaultValue, false);
                }
                return defaultValue;
            }
        } else if (defaultValue == null) {
            return null;
        } else {
            FileIO.writeToFile(privateFile, defaultValue, false);
            return null;
        }
    }

    public static boolean isUseExternalFile() {
        return useExternalFile;
    }
}

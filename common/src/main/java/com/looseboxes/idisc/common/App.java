package com.looseboxes.idisc.common;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.bc.android.core.AppCore;
import com.bc.android.core.DeviceIdManager;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.asynctasks.LogInstallationError;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.DateParser;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.Pref;
import com.looseboxes.idisc.common.util.PropertiesManager;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class App extends AppCore {

    public static final boolean SERVICE_IN_EXACT_REPEATING = false;
    private static long FIRST_INSTALLATION_TIME;
    private static String ID;
    private static PropertiesManager _ap_accessViaGetter;
    private static Map<AliasType, AliasesManager> _kam_accessViaGetter;
    private static boolean appVisible;
    private static Context mContext;
    private static boolean useExternalFile = false;

    public static int getNoticeIconResourceId(Context context) {
        return isAcceptableVersion(context, 21) ? R.drawable.notification_icon : R.mipmap.ic_launcher;
    }

    public static String getUrlScheme() {
        return "newsminute";
    }

    public static int getMemoryLimitedInt(Context context, PropertiesManager.PropertyName propertyName, int minValue) {

        final int value = App.getPropertiesManager(context).getInt(propertyName);

        final double memoryFactor = getMemoryFactor();

        final double memoryclassFactor = getMemoryclassFactor(context, 128);

        final double factor = memoryclassFactor * memoryFactor;

        int output = (int)(value * factor);

        output = output < minValue ? minValue : output;

        Logx.getInstance().log(Log.DEBUG, App.class,
                "Memory. Value: {0}, factor: {1}, minimum: {2}, output: {3}",
                value, factor, minValue, output);

        return output;
    }

    /**
     * @deprecated
     * @param context
     */
    @Deprecated
    public static void setContext(Context context) {
        mContext = context;
    }

    /**
     * @deprecated
     * @return
     */
    @Deprecated
    public static Context getContext() {
        return mContext;
    }

    public static String getLabel(Context context) {
        return context.getString(R.string.app_label);
    }

    public static void updateInstallationDetails(Context context, JSONObject download) throws IOException {
        String sval;
        File file;
        Logx.getInstance().debug(App.class, "Updating installation details");
        context = context.getApplicationContext();
        FileIO fileIO = new FileIO();
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
                Logx.getInstance().debug(App.class, "Writing to file {0} = {1}", InstallationNames.installationkey, ID);
                fileIO.writeToFile(file, ID, false);
            }
        }
        oval = download.get(InstallationNames.firstinstallationdate);
        if (oval != null) {
            sval = oval.toString().trim();
            if (!sval.isEmpty()) {
                Date date = new DateParser().parse(sval);
                if (date != null) {
                    FIRST_INSTALLATION_TIME = date.getTime();
                    if (useExternalFile) {
                        file = FileIO.getExternalPublicFile(context, FileIO.getFirstInstallationTimeFilename());
                    } else {
                        file = FileIO.getFile(context, FileIO.getFirstInstallationTimeFilename());
                    }
                    Logx.getInstance().debug(App.class, "Writing to file {0} = {1}",
                            InstallationNames.firstinstallationdate, FIRST_INSTALLATION_TIME);
                    fileIO.writeToFile(file, Long.toString(FIRST_INSTALLATION_TIME), false);
                }
            }
        }
    }

    public static Map<String, String> getOutputParameters(Context context) {
        Map<String, String> params = new LinkedHashMap<>();
        addParameters(context, params);
        return params;
    }

    public static int addParameters(Context context, Map<String, String> params) {
        final User user = User.getInstance();
        int added = user.addDetails(context, params) + user.addLoginCredentials(context, params) +
                user.addSubscriptionCredentials(context, params) + addInstallationParameters(context, params);
        params.put("versionCode", String.valueOf(getVersionCode(context)));
        ++added;
        params.put("format", "text/json");
        params.put("tidy", Logx.getInstance().isDebugMode() ? "true" : "false");
        return added + 2;
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
                String screenname = User.getInstance().getScreenname(context, null);
                if (screenname != null) {
                    params.put(InstallationNames.screenname, screenname);
                    ++added;
                }
                Logx.getInstance().log(Log.VERBOSE, App.class, "Added installation parameters: {0}", params.keySet());
            }
        } catch (IOException e) {
            Logx.getInstance().log(App.class, e);
        }
        return added;
    }

    public static void handleInstallationError(Context context, Exception e, int displaymsg_id) {
        handleInstallationError(context, e, context.getString(displaymsg_id));
    }

    public static void handleInstallationError(Context context, Exception e, String displayMessage) {
        new LogInstallationError(context).execute();
    }

    public static boolean isVisible() {
        return appVisible;
    }

    public static void setVisible(boolean appVisible) {
        App.appVisible = appVisible;
    }

    public static synchronized void install(Context context, String countryCode, String screenname, String gender) throws IOException {

        synchronized (App.class) {

            Util.requireNonNullOrEmpty(screenname, InstallationNames.screenname + " may not be null or an empty string");

            ID = getId(context, UUID.randomUUID());

            final long currTime = System.currentTimeMillis();

            getFirstInstallationTime(context, currTime);

            Pref.setLong(context, FileIO.getLastInstallationTimeFilename(), currTime);

            User.getInstance().getProfilePreferences().update(context, countryCode, screenname, gender);

            Popup.getInstance().show(context, R.string.msg_installation_successful, Toast.LENGTH_SHORT);
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

    public static boolean isInstalled(Context context) throws IOException {
        boolean installed;
        if (getId(context) == null || getFirstInstallationTime(context) <= 0) {
            installed = false;
        } else {
            installed = true;
        }
        Logx.getInstance().log(Log.VERBOSE, App.class, "is installed: {0}", Boolean.valueOf(installed));
        return installed;
    }

    public static synchronized String getId(Context context) throws IOException {
        String str;
        synchronized (App.class) {
//            if (Logx.getInstance().isDebugMode()) {
//                str = "abdb33ee-a09e-4d7d-b861-311ee7061325";
//            } else {
                context = context.getApplicationContext();
                final String defaultId = DeviceIdManager.getID(context, FileIO.getInstallationIdFilename());
                str = getId(context, defaultId);
//            }
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

                ID = new FileIO().loadDoubleSavedFile(
                        context,
                        FileIO.getExternalPublicDirName(),
                        installationIdFilename,
                        str);
            } else {
                installationIdFilename = FileIO.getInstallationIdFilename();
                if (defaultValue != null) {
                    str = defaultValue.toString();
                }
                ID = new FileIO().loadSavedFile(context, installationIdFilename, str);
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
                sval = new FileIO().loadDoubleSavedFile(
                        context,
                        FileIO.getExternalPublicDirName(),
                        firstInstallationTimeFilename,
                        str);
            } else {
                firstInstallationTimeFilename = FileIO.getFirstInstallationTimeFilename();
                if (defaultValue >= 0) {
                    str = Long.toString(defaultValue);
                }
                sval = new FileIO().loadSavedFile(context, firstInstallationTimeFilename, str);
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
                return new FileIO().readFile(idFile);
            }
            if (defaultValue == null) {
                return null;
            }
            new FileIO().writeToFile(idFile, defaultValue, false);
            return null;
        } catch (Exception e) {
            Logx.getInstance().log(App.class, e);
            throw new RuntimeException(e);
        }
    }

    private static String loadDoubleSavedFile(Context context, String fname, String defaultValue) throws IOException {
        context = context.getApplicationContext();
        File publicFile = FileIO.getExternalPublicFile(context, fname);
        File privateFile = FileIO.getFile(context, fname);
        String output;
        FileIO fileIO = new FileIO();
        if (privateFile.exists()) {
            output = fileIO.readFile(privateFile);
            if (!FileIO.isExternalStorageReadable()) {
                return output;
            }
            if (publicFile.exists()) {
                String publicValue = fileIO.readFile(publicFile);
                if (output.equals(publicValue) || !FileIO.isExternalStorageWritable()) {
                    return output;
                }
                fileIO.writeToFile(privateFile, publicValue, false);
                return publicValue;
            } else if (!FileIO.isExternalStorageWritable()) {
                return output;
            } else {
                fileIO.writeToFile(publicFile, output, false);
                return output;
            }
        } else if (FileIO.isExternalStorageReadable()) {
            if (publicFile.exists()) {
                output = fileIO.readFile(publicFile);
                fileIO.writeToFile(privateFile, output, false);
                return output;
            } else if (defaultValue == null) {
                return null;
            } else {
                fileIO.writeToFile(privateFile, defaultValue, false);
                if (fileIO.isExternalStorageWritable()) {
                    fileIO.writeToFile(publicFile, defaultValue, false);
                }
                return defaultValue;
            }
        } else if (defaultValue == null) {
            return null;
        } else {
            fileIO.writeToFile(privateFile, defaultValue, false);
            return null;
        }
    }
}

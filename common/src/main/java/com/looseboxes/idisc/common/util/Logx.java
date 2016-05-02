package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.util.Log;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.Feed;
import com.looseboxes.idisc.common.jsonview.FeedNames;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;

public final class Logx {

//    public static final String GIONEE_P4 = "6C86837707C64E24C7ADCF5EB2AD1E6D";
    public static final String SAMSUNG_GALAXY_S5 = "F681499FE32F2F73FDC6386B0DEA7BCE";
//    public static final String SAMSUNG_GALAXY_S7_EDGE = "7A17EB9F1ACA121443B8D8CFAD52619A";
    public static final String TEST_DEVICE_ID = SAMSUNG_GALAXY_S5;

    private static boolean productionMode = true;

    private static final int defaultPopupsDevelopmentMode = 0;

    private static int _lp_accessViaGetter;
    private static LogSettings _ls;
    private static volatile int logFileCount;
    private static List<String> tagsToAccept;

    public interface LogSettings {
        int getDefaultLogPriority();

        int getPopupRepeats();

        boolean isLogToFile();

        boolean isLogToLogCat();

        void setPopupRepeats(int i);
    }

    private static class DevelopmentSettings implements LogSettings {
        private int popups;

        private DevelopmentSettings() {
            this.popups = 0;
        }

        public int getDefaultLogPriority() {
            return Log.DEBUG;
        }

        public boolean isLogToLogCat() {
            return true;
        }

        public boolean isLogToFile() {
            return true;
        }

        public int getPopupRepeats() {
            return this.popups;
        }

        public void setPopupRepeats(int n) {
            this.popups = n;
        }
    }

    private static class ProductionSettings implements LogSettings {
        private ProductionSettings() {
        }

        public int getDefaultLogPriority() {
            return 4;
        }

        public boolean isLogToLogCat() {
            return true;
        }

        public boolean isLogToFile() {
            return false;
        }

        public int getPopupRepeats() {
            return 0;
        }

        public void setPopupRepeats(int n) {
        }
    }

    public static boolean isLoggable(int priority) {
        return priority >= getLogPriority();
    }

    public static void debug(Class aClass, String msg, Object... args) {
        log(3, aClass, msg, args);
    }

    public static void debug(Class aClass, Object msg) {
        log(Log.DEBUG, aClass, msg);
    }

    public static void log(Class aClass, Throwable t) {
        log(Log.WARN, aClass, t);
    }

    public static void log(int priority, Class aClass, String msg, Object... args) {
        log(priority, aClass, MessageFormat.format(msg, args));
    }

    public static void log(int priority, Class aClass, Object msg) {
        if (isLoggable(priority) && accept(priority, aClass.getName())) {
            if (msg instanceof Throwable) {
                msg = Log.getStackTraceString((Throwable) msg);
            }
            writeLog(priority, aClass, msg);
        }
    }

    private static void writeLog(int priority, Class aClass, Object msg) {
        try {
            String TAG = aClass.getName();
            if (accept(priority, TAG)) {
                StringBuilder builder = new StringBuilder();
                builder.append(getLogPriorityString(priority)).append(' ');
                builder.append(new Date()).append('\n');
                if (aClass != null) {
                    builder.append('@').append(TAG).append('\n');
                }
                builder.append(msg).append('\n').append('\n');
                String sval = builder.toString();
                if (getLogSettings().isLogToLogCat()) {
                    Log.println(priority, TAG, sval);
                }
                if (getLogSettings().isLogToFile()) {
                    File logFile = getLogFile();
                    if (logFile != null) {
                        try {
                            FileIO.writeToFile(logFile, sval, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static boolean accept(int priority, String tag) {
        return isLoggable(priority) && accept(tag);
    }

    public static boolean accept(Class aClass) {
        return accept(aClass.getName());
    }

    public static boolean accept(String tag) {
        try {
            if (tagsToAccept == null || tagsToAccept.isEmpty()) {
                return true;
            }
            boolean accept = tagsToAccept.contains(tag);
            if (accept) {
                return accept;
            }
            for (String tagToAccept : tagsToAccept) {
                if (tag.startsWith(tagToAccept)) {
                    return true;
                }
            }
            return accept;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void addTagToAccept(String tag) {
        if (tagsToAccept == null) {
            tagsToAccept = new ArrayList();
        }
        tagsToAccept.add(tag);
    }

    public static void removeTagToAccept(String tag) {
        if (tagsToAccept != null) {
            tagsToAccept.remove(tag);
        }
    }

    public static void clearTagsToAccept() {
        if (tagsToAccept != null) {
            tagsToAccept.clear();
        }
    }

    public static File getLogFile() {
        if (((long) logFileCount) == PropertyName.logFileCount.getDefaultNumber()) {
            while (logFileCount >= 0) {
                File toDelete = getLogFile(logFileCount);
                if (!(toDelete == null || !toDelete.exists() || toDelete.delete())) {
                    toDelete.deleteOnExit();
                }
                logFileCount--;
            }
        }
        long limit = PropertyName.logFileLimit.getDefaultNumber();
        File logFile = getLogFile(logFileCount);
        if (logFile == null || !logFile.exists() || logFile.length() < limit) {
            return logFile;
        }
        int i = logFileCount + 1;
        logFileCount = i;
        return getLogFile(i);
    }

    public static File getLogFile(int index) {
        return getLogFile("newsminute_log" + index + ".txt");
    }

    public static File getLogFile(String filename) {
        Context context = App.getContext();
        if (context == null) {
            return null;
        }
        if (App.isUseExternalFile()) {
            return FileIO.getExternalPublicFile(context, filename);
        }
        return FileIO.getFile(context, filename);
    }

    public static String getLogPriorityString(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "VERBOSE";
            case Log.DEBUG:
                return "DEBUG";
            case Log.INFO:
                return "INFO";
            case Log.WARN:
                return "WARN";
            case Log.ERROR:
                return "ERROR";
            case Log.ASSERT:
                return "ASSERT";
            default:
                throw new IllegalArgumentException("Unexpected Log priority: " + priority);
        }
    }

    public static int getLogPriority() {
        return _lp_accessViaGetter <= 0 ? getLogSettings().getDefaultLogPriority() : _lp_accessViaGetter;
    }

    public static void setLogPriority(int priority) {
        _lp_accessViaGetter = priority;
    }

    public static void logLatest(Class aClass, String key, List downloaded) {
        if (isLoggable(3) && downloaded != null && !downloaded.isEmpty()) {
            Feed feedView = new Feed();
            long newestTime = feedView.getLatestTime(downloaded);
            String timeElapsed = newestTime == -1 ? "null" : feedView.getTimeElapsed(new Date(newestTime));
            debug(aClass, "Newest feed in {0} {1} is {2} old", Integer.valueOf(downloaded.size()), key, timeElapsed);
        }
    }

    public static void logCategories(String key, Class aClass, List downloaded, String categories) {
        if (isLoggable(2) && downloaded != null && !downloaded.isEmpty()) {
            Feed feedView = new Feed();
            int statusesCount = 0;
            Date latest = null;
            for (Object o : downloaded) {
                feedView.setJsonData((JSONObject)o);
                String s = feedView.getCategories();
                if (s != null && s.toLowerCase().contains(categories.toLowerCase())) {
                    statusesCount++;
                    Date date = feedView.getDate(FeedNames.feeddate);
                    if (date != null) {
                        if (latest == null) {
                            latest = date;
                        } else if (date.after(latest)) {
                            latest = date;
                        }
                    }
                }
            }
            String latestDisplay = null;
            if (latest != null) {
                latestDisplay = feedView.getTimeElapsed(latest);
            }
            debug(aClass, key + ", found {0} in {1} feeds\nNewest is {2} old.\nCategories: {3}", Integer.valueOf(statusesCount), Integer.valueOf(downloaded.size()), latestDisplay, categories);
        }
    }

    public static boolean isDebugMode() {
        return !productionMode;
    }

    public static LogSettings getLogSettings() {
        if (_ls == null) {
            _ls = productionMode ? new ProductionSettings() : new DevelopmentSettings();
        }
        return _ls;
    }
}
